package app.dtos.puzzles;

public record PuzzleMoveResponseDTO(boolean isCorrect, String opponentMove, String status, int attemptsLeft, int movesLeft){
}
