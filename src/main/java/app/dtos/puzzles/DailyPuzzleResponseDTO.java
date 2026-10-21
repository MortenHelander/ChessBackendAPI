package app.dtos.puzzles;

import java.time.LocalDate;

public record DailyPuzzleResponseDTO(LocalDate date, String fen, int rating, String lastMove, boolean isWhitesTurn, int movesToSolve) {
}
