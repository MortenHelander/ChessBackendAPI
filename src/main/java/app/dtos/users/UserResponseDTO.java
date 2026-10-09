package app.dtos.users;

public record UserResponseDTO (Integer id, String username, String email, String firstName, UserStatsDTO userStatsDTO) {
}
