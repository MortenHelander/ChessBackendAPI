package app.dtos.users;

import java.util.Set;

public record VerifiedUserDTO(String username, Set<String> roles) {
}
