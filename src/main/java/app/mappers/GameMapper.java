package app.mappers;

import app.dtos.games.GameResponseDTO;
import app.dtos.games.PlayerResponseDTO;
import app.entities.Game;
import app.entities.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class GameMapper {

    public static GameResponseDTO toDTO(Game game){

        List<PlayerResponseDTO> players = PlayerMapper.entityToDtoAsList(game.getPlayers());

        return new GameResponseDTO(game.getId(), game.getStartedAt(), game.getGameMode().name(), game.getGameStatus().name(), game.getWinnerColor().name(), game.isWhitesTurn(), players);
    }


    public static List<GameResponseDTO> toDTOFromList(List<Game> games){
        List<GameResponseDTO> dtos = new ArrayList<>();
        for (Game game : games) {
            dtos.add(toDTO(game));
        }
        return dtos;
    }
}
