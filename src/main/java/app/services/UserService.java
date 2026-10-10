package app.services;

import app.daos.UserDAO;
import app.dtos.users.UserLoginRequestDTO;
import app.dtos.users.UserRegisterDTO;
import app.dtos.users.UserResponseDTO;
import app.dtos.users.VerifiedUserDTO;
import app.entities.User;
import app.exceptions.ApiException;
import app.mappers.UserMapper;
import app.utils.PasswordHasher;

public class UserService {
    private final UserDAO userDAO;

    public UserService(UserDAO userDAO){
        this.userDAO = userDAO;
    }

    public UserResponseDTO createUser(UserRegisterDTO registerDTO){
        String hashedPw = PasswordHasher.hashPassword(registerDTO.password());
        User user = UserMapper.fromRegisterDTO(registerDTO, hashedPw);
        User created = userDAO.create(user);
        return UserMapper.responseDTOFromEntity(created);
    }

    public VerifiedUserDTO verifyUser(UserLoginRequestDTO userLoginRequestDTO){
        User user = userDAO.getByUsername(userLoginRequestDTO.username());
        if (user == null || !PasswordHasher.checkPassword(userLoginRequestDTO.password(), user.getPassword())){
            throw new ApiException(401, "Invalid username or password");
        }
        return UserMapper.verifiedDTOFromEntity(user);
    }

//    public List<UserResponseDTO> getAllUsers(){
//
//    }
}
