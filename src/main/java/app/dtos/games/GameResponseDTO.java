package app.dtos.games;

import java.time.LocalDateTime;
import java.util.List;

public record GameResponseDTO(int id, LocalDateTime startedAt, String gameMode, String gameStatus, String winningColor, boolean isWhitesTurn, List<PlayerResponseDTO> players) {
}
