package app.dtos.games;

import app.entities.enums.Color;

import java.util.List;

public record PlayerWithPowerUpResponseDTO (int id, Color color, boolean isAi, List<PowerUpResponseDTO> powerUps){
}
