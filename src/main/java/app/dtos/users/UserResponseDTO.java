package app.dtos.users;

import java.util.List;

public record UserResponseDTO (Integer id, String username, String email, String firstName, UserStatsDTO userStatsDTO) {
}
