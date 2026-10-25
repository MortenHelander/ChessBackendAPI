package app.dtos.puzzles;

import java.time.LocalDate;
import java.util.List;

public record PuzzleAttemptResponseDTO(LocalDate puzzleDate, String status, int attemptsLeft, LocalDate startedAt, LocalDate finishedAt, List<String> playedMoves) {
}
