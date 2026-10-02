package app.mappers;

import app.dtos.games.MoveRequestDTO;
import app.dtos.games.MoveResponseDTO;
import app.entities.Move;

import java.util.ArrayList;
import java.util.List;

public class MoveMapper {

    public static MoveResponseDTO toDto (Move move){
        return new MoveResponseDTO(move.getId(), move.getMoveNumber(), move.getUci(), move.getPlayedAt(), move.getFrom().name(), move.getTo().name());
    }

    public static List<MoveResponseDTO> toDtosFromList (List<Move> moves){
        List<MoveResponseDTO> dtos = new ArrayList<>();
        for (Move move : moves) {
            dtos.add(toDto(move));
        }
        return dtos;
    }
}
