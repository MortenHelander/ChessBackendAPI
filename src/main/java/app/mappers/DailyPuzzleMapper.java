package app.mappers;

import app.dtos.puzzles.DailyPuzzleResponseDTO;
import app.dtos.lichess.LichessDailyPuzzleDTO;
import app.entities.DailyPuzzle;

import java.time.LocalDate;

public class DailyPuzzleMapper {

    public static DailyPuzzle toEntity(LichessDailyPuzzleDTO dto, LocalDate date){
        return new DailyPuzzle(date, dto.puzzle().id(), dto.puzzle().rating(), dto.puzzle().solution(), dto.puzzle().fen(), dto.puzzle().lastMove());
    }

    public static DailyPuzzleResponseDTO toDto(DailyPuzzle dailyPuzzle, boolean isWhitesTurn, int movesToSolve){
        return new DailyPuzzleResponseDTO(dailyPuzzle.getId(), dailyPuzzle.getFen(), dailyPuzzle.getRating(), dailyPuzzle.getLastMove(), isWhitesTurn, movesToSolve);
    }
}
