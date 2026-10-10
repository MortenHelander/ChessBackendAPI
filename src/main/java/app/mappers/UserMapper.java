package app.mappers;

import app.dtos.users.UserRegisterDTO;
import app.dtos.users.UserResponseDTO;
import app.dtos.users.VerifiedUserDTO;
import app.entities.AccessRole;
import app.entities.User;
import app.security.AccessRoleName;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class UserMapper {

    public static User fromRegisterDTO(UserRegisterDTO registerDTO, String hashedPw){
        return new User(registerDTO.username().toLowerCase().trim(), registerDTO.email().toLowerCase().trim(), registerDTO.firstName(), registerDTO.lastName(), hashedPw);
    }

    public static UserResponseDTO responseDTOFromEntity(User user){
        return new UserResponseDTO(user.getId(), user.getEmail(), user.getUsername(), user.getFirstName(), UserStatsMapper.fromEntity(user.getUserStats()));
    }

    public static VerifiedUserDTO verifiedDTOFromEntity(User user){
        Set<AccessRoleName> rolesNames = user.getRoles().stream().map(AccessRole::getRoleName).collect(Collectors.toSet());
        Set<String> rolesAsString = new HashSet<>();
        for (AccessRoleName rolesName : rolesNames) {
            String role = rolesName.name();
            rolesAsString.add(role);
        }
        return new VerifiedUserDTO(user.getUsername(), rolesAsString);
    }
}
