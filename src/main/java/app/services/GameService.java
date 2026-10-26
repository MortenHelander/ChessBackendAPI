package app.services;

import app.daos.*;
import app.dtos.games.GameCreateDTO;
import app.dtos.games.GameResponseDTO;
import app.dtos.games.MoveRequestDTO;
import app.dtos.games.MoveResponseDTO;
import app.entities.*;
import app.entities.enums.Color;
import app.entities.enums.GameMode;
import app.entities.enums.GameStatus;
import app.entities.enums.Result;
import app.exceptions.ApiException;
import app.gameengine.*;
import app.gameengine.game_logic.CheckChecker;
import app.gameengine.game_logic.MoveResult;
import app.gameengine.game_logic.Promotion;
import app.gameengine.gameservices.PieceFinder;
import app.gameengine.game_logic.PromotionHelper;
import app.gameengine.gameservices.PositionConverter;
import app.gameengine.pieces.Piece;
import app.mappers.GameMapper;
import app.mappers.MoveMapper;

import java.util.List;

public class GameService {
    private final UserService userService;
    private final PlayerDAO playerDAO;
    private final GameDAO gameDAO;
    private final MoveDAO moveDAO;
    private final UserDAO userDAO;
    private final UserStatsDAO userStatsDao;

    public GameService(UserService userService, PlayerDAO playerDAO, GameDAO gameDAO, MoveDAO moveDAO, UserDAO userDAO, UserStatsDAO userStatsDao) {
        this.userService = userService;
        this.playerDAO = playerDAO;
        this.gameDAO = gameDAO;
        this.moveDAO = moveDAO;
        this.userDAO = userDAO;
        this.userStatsDao = userStatsDao;
    }

    public GameResponseDTO createGame(GameCreateDTO gameCreateDTO) {
        User whiteUser = userDAO.getById(gameCreateDTO.whiteUserId());
        User blackUser = userDAO.getById(gameCreateDTO.blackUserId());

        Player whitePlayer = new Player(Color.WHITE, false);
        Player blackPlayer = new Player(Color.BLACK, false);
        whitePlayer.setUser(whiteUser);
        blackPlayer.setUser(blackUser);

        Game game = Game.newGame(GameMode.valueOf(gameCreateDTO.gameMode()), whitePlayer, blackPlayer);
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

    public MoveResponseDTO moveAndShiftTurn(Integer gameId, MoveRequestDTO moveRequest, Integer userId) {
        Game game = gameDAO.getById(gameId);
        if (game.getGameStatus() != GameStatus.IN_PROGRESS) {
            throw new ApiException(400, "Game is finished");
        }

        Player foundPlayer = game.getPlayers().stream()
                .filter(p -> p.getUser() != null && p.getUser().getId().equals(userId)).findFirst()
                .orElseThrow(() -> new ApiException(403, "You are not a player in this game"));

        boolean playerIsWhite = (foundPlayer.getColor() == Color.WHITE); //convert color enum to boolean
        if (playerIsWhite != game.isWhitesTurn()){
            throw new ApiException(400, "Not your turn");
        }

        Board board = ReplayHelper.rebuildBoard(game.getMoves());
        Position from = PositionConverter.fromString(moveRequest.from());
        Position to = PositionConverter.fromString(moveRequest.to());
        Piece piece = PieceFinder.findPiece(board, from);

        if (piece == null) {
            throw new ApiException(400, "No piece on selected square");
        } else if (piece.isWhite() != game.isWhitesTurn()) {
            throw new ApiException(400, "You can't move your opponent's pieces");
        }
        if (!MoveValidator.getLegalMoves(board, piece, from).contains(to)) {
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

        boolean isOpponentWhite = !game.isWhitesTurn();
        if (CheckChecker.isCheckMate(board, isOpponentWhite)) {
            game.finishGame(GameStatus.CHECKMATE, game.isWhitesTurn() ? Color.WHITE : Color.BLACK);
        } else if (CheckChecker.isStaleMate(board, isOpponentWhite)) {
            game.finishGame(GameStatus.DRAW, null);
        }

        String letter = result.isAwaitingPromotion() ? moveRequest.promotionLetter() : null;
        Move move = new Move(from, to, letter);
        boolean inCheck = CheckChecker.isSideInCheck(board, isOpponentWhite);
        game.addMoveAndShiftTurn(move);
        Game persistedGame = gameDAO.update(game);
        if (persistedGame.getGameStatus() != GameStatus.IN_PROGRESS){
            recordResult(persistedGame);
        }

        Move persistedMove = persistedGame.getMoves().getLast();
        return MoveMapper.toDto(persistedMove, inCheck);
    }

    public GameResponseDTO resign(Integer gameId, Integer userId){
        Game game = gameDAO.getById(gameId);
        if (game.getGameStatus() != GameStatus.IN_PROGRESS) {
            throw new ApiException(400, "Game is finished");
        }

        Player foundPlayer = game.getPlayers().stream()
                .filter(p -> p.getUser() != null && p.getUser().getId().equals(userId)).findFirst()
                .orElseThrow(() -> new ApiException(403, "You are not a player in this game"));

       game.resign(foundPlayer.getColor());
       Game updated = gameDAO.update(game);
       recordResult(updated);
       return GameMapper.toDTO(updated);
    }

    private void recordResult(Game game){
        for (Player player : game.getPlayers()) {
            //if player is ai
            if (player.getUser() == null){
                break;
            }
            Result result;
            if (game.getWinnerColor() == Color.NO_COLOR){
                result = Result.DRAW;
            }else if (player.getColor() == game.getWinnerColor()){
                result = Result.WIN;
            }else{
                result = Result.LOSS;
            }
            userStatsDao.recordResult(player.getUser().getId(), result);
        }
    }
}
