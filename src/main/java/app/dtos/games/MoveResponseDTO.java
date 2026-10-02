package app.dtos.games;

import java.time.LocalDateTime;

public record MoveResponseDTO(int id, int moveNumber, String uci, LocalDateTime playedAt, String from, String to) {
}
