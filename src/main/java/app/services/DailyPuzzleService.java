package app.services;

import app.clients.LichessClient;
import app.daos.DailyPuzzleAttemptDAO;
import app.daos.DailyPuzzleDAO;
import app.daos.UserDAO;
import app.dtos.games.MoveRequestDTO;
import app.dtos.lichess.LichessDailyPuzzleDTO;
import app.dtos.puzzles.DailyPuzzleResponseDTO;
import app.dtos.puzzles.PuzzleMoveResponseDTO;
import app.entities.DailyPuzzle;
import app.entities.DailyPuzzleAttempt;
import app.entities.Move;
import app.entities.User;
import app.entities.enums.PuzzleStatus;
import app.exceptions.ApiException;
import app.exceptions.DuplicatePuzzleException;
import app.gameengine.Board;
import app.gameengine.FenGameState;
import app.gameengine.Position;
import app.gameengine.game_logic.CheckChecker;
import app.gameengine.game_logic.MoveResult;
import app.gameengine.game_logic.Promotion;
import app.gameengine.game_logic.PromotionHelper;
import app.gameengine.gameservices.FenParser;
import app.gameengine.gameservices.PositionConverter;
import app.gameengine.gameservices.UciParser;
import app.gameengine.pieces.Piece;
import app.mappers.DailyPuzzleMapper;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class DailyPuzzleService {
    private final Clock clock;
    private final DailyPuzzleDAO dailyPuzzleDAO;
    private final DailyPuzzleAttemptDAO puzzleAttemptDAO;
    private final UserDAO userDAO;
    private final LichessClient lichessClient;

    public DailyPuzzleService(Clock clock, DailyPuzzleDAO dailyPuzzleDAO, DailyPuzzleAttemptDAO puzzleAttemptDAO, UserDAO userDAO, LichessClient lichessClient){
        this.clock = clock;
        this.dailyPuzzleDAO = dailyPuzzleDAO;
        this.puzzleAttemptDAO = puzzleAttemptDAO;
        this.userDAO = userDAO;
        this.lichessClient = lichessClient;
    }

    public DailyPuzzleResponseDTO getTodaysPuzzle(){
        DailyPuzzle dailyPuzzle = getOrFetchTodaysPuzzle();
        List<String> solution = dailyPuzzle.getSolution();
        int movesToSolve = (solution.size() + 1)/2;
        boolean isWhitesTurn = FenParser.fromFen(dailyPuzzle.getFen()).isWhitesTurn();

        return DailyPuzzleMapper.toDto(dailyPuzzle, isWhitesTurn, movesToSolve);
    }

    public PuzzleMoveResponseDTO playMove(Integer userId, MoveRequestDTO move){
        DailyPuzzle puzzle = getOrFetchTodaysPuzzle();

        //finds users existing attempt or makes a new if none exist
        DailyPuzzleAttempt attempt = (puzzleAttemptDAO.getByUserIdAndPuzzleDate(userId, puzzle.getId()).orElseGet(() -> DailyPuzzleAttempt.startNewPuzzle(puzzle,
                userDAO.getById(userId))));
        if (attempt.getStatus() != PuzzleStatus.IN_PROGRESS){
            throw new ApiException(400, "Puzzle already " + attempt.getStatus());
        }

        List<String> solution = puzzle.getSolution();
        FenGameState state = FenParser.fromFen(puzzle.getFen());
        boolean isSolverWhite = state.isWhitesTurn();
        Board board = ReplayHelper.rebuildBoard(state.board(), UciParser.toMoves(solution.subList(0, attempt.getMoveIndex())));

        Position from = PositionConverter.fromString(move.from());
        Position to = PositionConverter.fromString(move.to());
        Piece piece = board.getAllPieces().get(from);
        if (piece == null){
            throw new ApiException(400, "No piece on selected square");
        }
        if (piece.isWhite() != isSolverWhite){
            throw new ApiException(400, "Not your piece");
        }
        if (!MoveValidator.getLegalMoves(board, piece, from).contains(to)){
            throw new ApiException(400, "Illegal move");
        }
        MoveResult result = board.move(piece, from, to);
        if (!result.success()) {
            throw new ApiException(400, "Illegal move");
        }
        String promotionLetter = "";
        if (result.isAwaitingPromotion()) {
            if (move.promotionLetter() == null) {
                throw new ApiException(400, "Promotion piece required");
            } else {
                promotionLetter = move.promotionLetter();
                board.promotion(new Promotion(PromotionHelper.getPromotionPieceType(move.promotionLetter()), to));
            }
        }

        String userUci = (from.name()+to.name()+promotionLetter).toLowerCase();
        String expected = solution.get(attempt.getMoveIndex());

        boolean isMate = CheckChecker.isCheckMate(board, !isSolverWhite); //using own game logic, if user finds a checkmate which wasn't the solution in the puzzle
        boolean correct = userUci.equals(expected) || isMate;
        String reply = null;

        if (correct){
            boolean isLastMove = attempt.getMoveIndex() + 1 >= solution.size();
            if (isMate || isLastMove) {
                attempt.markAsSolved();
            } else {
                reply = solution.get(attempt.getMoveIndex() + 1);
                attempt.advance();
            }
        } else {
            attempt.removeOneAttempt();
        }

        //if no id, it means a new puzzle attempt was created in the start if method
        DailyPuzzleAttempt saved = attempt.getId() == null ? puzzleAttemptDAO.create(attempt) : puzzleAttemptDAO.update(attempt);
        int movesLeft = (solution.size() - saved.getMoveIndex() + 1) / 2;
        return new PuzzleMoveResponseDTO(correct, reply, saved.getStatus().name(), saved.getAttemptsLeft(), movesLeft);
    }

    private DailyPuzzle getOrFetchTodaysPuzzle(){
        LocalDate today = LocalDate.now(clock);
        Optional<DailyPuzzle> dailyPuzzle = dailyPuzzleDAO.getByDate(today);
        return dailyPuzzle.orElseGet(() -> fetchAndSave(today));
    }

    private DailyPuzzle fetchAndSave(LocalDate today){
        LichessDailyPuzzleDTO dto = lichessClient.getDailyPuzzle();
        if (dto == null || dto.puzzle() == null) {
            throw new ApiException(502, "Lichess returned no puzzle");
        }
        if (dto.puzzle().solution() == null || dto.puzzle().solution().isEmpty()) {
            throw new ApiException(502, "Lichess returned a puzzle without a solution");
        }

        // validate the FEN before saving, so a broken puzzle never reaches the database
        try {
            FenParser.fromFen(dto.puzzle().fen());
        } catch (ApiException e) {
            throw new ApiException(502, "Lichess returned an invalid puzzle FEN: " + e.getMessage());
        }

        DailyPuzzle entity = DailyPuzzleMapper.toEntity(dto, today);

        try{
            return dailyPuzzleDAO.create(entity);
        }catch (DuplicatePuzzleException e){
            return dailyPuzzleDAO.getByDate(today).orElseThrow(() -> new ApiException(500, "Puzzle for " + today + " was a duplicate but could not be read"));
        }
    }
}
