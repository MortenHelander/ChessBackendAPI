package app.dtos.games;

import app.entities.enums.Color;
import app.entities.enums.EndReason;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.List;

public record GameResponseDTO(int id, LocalDateTime startedAt, String gameMode, String gameStatus, Color winnerColor, EndReason endReason, boolean isWhitesTurn, List<PlayerResponseDTO> players) {
}
