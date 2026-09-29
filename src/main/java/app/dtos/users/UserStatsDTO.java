package app.dtos.users;

public record UserStatsDTO(Integer gamesPlayed, Integer wins, Integer losses, Integer draws, double mmr) {
}
