package app.services;

import app.daos.GameDAO;
import app.daos.MoveDAO;
import app.daos.PlayerDAO;
import app.dtos.games.GameResponseDTO;
import app.dtos.games.MoveRequestDTO;
import app.dtos.games.MoveResponseDTO;
import app.entities.Game;
import app.entities.Move;
import app.entities.Player;
import app.entities.enums.GameMode;
import app.gameengine.Position;
import app.gameengine.PositionConverter;
import app.mappers.GameMapper;
import app.mappers.MoveMapper;

import java.util.List;

public class GameService {
    private final PlayerDAO playerDAO;
    private final GameDAO gameDAO;
    private final MoveDAO moveDAO;

    public GameService(PlayerDAO playerDAO, GameDAO gameDAO, MoveDAO moveDAO){
        this.playerDAO = playerDAO;
        this.gameDAO = gameDAO;
        this.moveDAO = moveDAO;
    }

    public GameResponseDTO createGame(GameMode gameMode, Integer whitePlayerId, Integer blackPlayerId){
        Player white = playerDAO.getById(whitePlayerId);
        Player black = playerDAO.getById(blackPlayerId);

        Game game = Game.newGame(gameMode, white, black);
        Game saved = gameDAO.create(game);

        return GameMapper.toDTO(saved);
    }

    public List<GameResponseDTO> getAllGames(){
        return GameMapper.toDTOFromList(gameDAO.getAll());
    }

    public GameResponseDTO getGame(Integer id){

        Game game = gameDAO.getById(id);
        return GameMapper.toDTO(game);
    }

    public void deleteGame(Integer id){
        gameDAO.delete(id);
    }

    public List<MoveResponseDTO> getMoves(Integer id){

        List<Move> moves = moveDAO.getAllMovesByGameId(id);
        return MoveMapper.toDtosFromList(moves);
    }

    public MoveResponseDTO moveAndShiftTurn(Integer id, MoveRequestDTO moveRequest){
        Game game = gameDAO.getById(id);

        Position from = PositionConverter.fromString(moveRequest.from());
        Position to = PositionConverter.fromString(moveRequest.to());
        Move move = new Move(from, to);

        game.addMoveAndShiftTurn(move);

        gameDAO.update(game);
        return MoveMapper.toDto(move);
    }







}
