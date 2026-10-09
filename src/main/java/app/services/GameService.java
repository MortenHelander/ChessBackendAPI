package app.services;

import app.daos.GameDAO;
import app.daos.MoveDAO;
import app.daos.PlayerDAO;
import app.dtos.games.GameCreateDTO;
import app.dtos.games.GameResponseDTO;
import app.dtos.games.MoveRequestDTO;
import app.dtos.games.MoveResponseDTO;
import app.entities.Game;
import app.entities.Move;
import app.entities.Player;
import app.entities.User;
import app.entities.enums.Color;
import app.entities.enums.GameMode;
import app.entities.enums.GameStatus;
import app.exceptions.ApiException;
import app.gameengine.*;
import app.gameengine.move_logic.CheckChecker;
import app.gameengine.move_logic.PieceFinder;
import app.gameengine.move_logic.PromotionHelper;
import app.gameengine.pieces.Piece;
import app.mappers.GameMapper;
import app.mappers.MoveMapper;

import java.util.List;

public class GameService {
    private final UserService userService;
    private final PlayerDAO playerDAO;
    private final GameDAO gameDAO;
    private final MoveDAO moveDAO;

    public GameService(UserService userService, PlayerDAO playerDAO, GameDAO gameDAO, MoveDAO moveDAO) {
        this.userService = userService;
        this.playerDAO = playerDAO;
        this.gameDAO = gameDAO;
        this.moveDAO = moveDAO;
    }

    public GameResponseDTO createGame(GameCreateDTO gameCreateDTO) {

        User whiteUser = userService.getUser();
        User blackUser = userService.getUser();


        Game game = Game.newGame(GameMode.valueOf(gameCreateDTO.gameMode()), white, black);
        Game saved = gameDAO.create(game);

        return GameMapper.toDTO(saved);
    }

    public List<GameResponseDTO> getAllGames() {
        return GameMapper.toDTOFromList(gameDAO.getAll());
    }

    public GameResponseDTO getGame(Integer id) {

        Game game = gameDAO.getById(id);
        return GameMapper.toDTO(game);
    }

    public void deleteGame(Integer id) {
        gameDAO.delete(id);
    }

    public List<MoveResponseDTO> getMoves(Integer id) {

        Game game = gameDAO.getById(id); //check existing game first for error check
        List<Move> moves = moveDAO.getAllMovesByGameId(game.getId());
        return MoveMapper.toDtosFromList(moves);
    }

    public MoveResponseDTO moveAndShiftTurn(Integer id, MoveRequestDTO moveRequest) {
        Game game = gameDAO.getById(id);
        if (game.getGameStatus() != GameStatus.IN_PROGRESS) {
            throw new ApiException(400, "Game is finished");
        }

        Board board = ReplayHelper.rebuildBoard(game.getMoves());
        Position from = PositionConverter.fromString(moveRequest.from());
        Position to = PositionConverter.fromString(moveRequest.to());
        Piece piece = PieceFinder.findPiece(board, from);

        if (piece == null) {
            throw new ApiException(400, "No piece on selected square");
        }

        if (piece.isWhite() != game.isWhitesTurn()) {
            throw new ApiException(400, "Not your turn");
        }

        if (!MoveValidator.getLegalMoves(board, piece, from).contains(to)){
            throw new ApiException(400, "Illegal move");
        }

        MoveResult result = board.move(piece, from, to);
        if (!result.success()) {
            throw new ApiException(400, "Illegal move");
        }
        if (result.isAwaitingPromotion()) {
            if (moveRequest.promotionLetter() == null) {
                throw new ApiException(400, "Promotion piece required");
            } else {
                board.promotion(new Promotion(PromotionHelper.getPromotionPieceType(moveRequest.promotionLetter()), to));
            }
        }

        boolean opponentIsWhite = !game.isWhitesTurn();

        if (CheckChecker.isCheckMate(board, opponentIsWhite)) {
            game.finishGame(GameStatus.CHECKMATE, game.isWhitesTurn() ? Color.WHITE : Color.BLACK);
        } else if (CheckChecker.isStaleMate(board, opponentIsWhite)) {
            game.finishGame(GameStatus.DRAW, null);
        }

        String letter = result.isAwaitingPromotion() ? moveRequest.promotionLetter() : null;
        Move move = new Move(from, to, letter);
        game.addMoveAndShiftTurn(move);
        Game persistedGame = gameDAO.update(game);
        Move persistedMove = persistedGame.getMoves().getLast();
        return MoveMapper.toDto(persistedMove);
    }
}
