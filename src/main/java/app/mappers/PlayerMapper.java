package app.mappers;

import app.dtos.games.PlayerResponseDTO;
import app.entities.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PlayerMapper {

    public static List<PlayerResponseDTO> entityToDtoAsList(Set<Player> players){
        List<PlayerResponseDTO> playerResponseDTOS = new ArrayList<>();
        for (Player player : players) {
            PlayerResponseDTO playerDto = new PlayerResponseDTO(player.getId(), player.getColor(), player.isAi());
            playerResponseDTOS.add(playerDto);
        }
        return playerResponseDTOS;
    }
}
