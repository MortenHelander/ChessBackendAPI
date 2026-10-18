package app.dtos.games;

import app.entities.enums.Color;

import java.util.List;

public record PlayerResponseDTO(int id, String username, Color color, boolean isAi) {

}
