package app.dtos.users;

import java.util.Set;

public record UserLoginResponseDTO (Integer id, String username, String token, Set<String> roles){
}
