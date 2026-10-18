package app.dtos.users;

import java.util.Set;

public record UserVerifiedPasswordDTO(Integer id, String username, Set<String> roles) {
}
