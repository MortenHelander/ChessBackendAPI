package app.mappers;

import app.dtos.users.UserRegisterDTO;
import app.dtos.users.UserResponseDTO;
import app.entities.User;
import org.mindrot.jbcrypt.BCrypt;

public class UserMapper {

    public static User fromDTO(UserRegisterDTO registerDTO, String hashedPw){
        return new User(registerDTO.username(), registerDTO.email(), registerDTO.firstName(), registerDTO.lastName(), hashedPw);
    }

    public static UserResponseDTO fromEntity(User user){
        return new UserResponseDTO(user.getId(), user.getEmail(), user.getUsername(), user.getFirstName(), UserStatsMapper.fromEntity(user.getUserStats()));
    }
}
