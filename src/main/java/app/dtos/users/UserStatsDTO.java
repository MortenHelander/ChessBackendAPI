package app.dtos.users;

public record UserStatsDTO(Integer id, Integer gamesPlayed, Integer wins, Integer losses, Integer draws, double mmr) {
}
