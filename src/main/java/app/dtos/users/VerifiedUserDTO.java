package app.dtos.users;

import java.util.List;

public record VerifiedUserDTO(String username, List<String> roles) {
}
