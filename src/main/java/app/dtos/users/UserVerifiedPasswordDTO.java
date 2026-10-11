package app.dtos.users;

import java.util.Set;

public record UserVerifiedPasswordDTO(String username, Set<String> roles) {
}
