package app.dtos.users;

import java.util.Set;

public record UserLoginResponseDTO (String username, String token, Set<String> roles){
}
