package app.dtos.games;

import app.entities.enums.PowerUpStatus;
import app.entities.enums.PowerUpType;

public record PowerUpResponseDTO(int id, PowerUpType type, PowerUpStatus status) {
}
