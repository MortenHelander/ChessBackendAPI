package app.dtos.games;

public record GameCreateDTO(Integer whiteUserId, Integer blackUserId, String gameMode) {
}
