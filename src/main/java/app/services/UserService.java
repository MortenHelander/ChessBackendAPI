package app.services;

import app.daos.UserDAO;
import app.dtos.users.UserLoginDTO;
import app.dtos.users.UserRegisterDTO;
import app.dtos.users.UserResponseDTO;
import app.entities.User;
import app.mappers.UserMapper;
import app.utils.PasswordHasher;

public class UserService {
    private final UserDAO userDAO;

    public UserService(UserDAO userDAO){
        this.userDAO = userDAO;
    }

    public UserResponseDTO createUser(UserRegisterDTO registerDTO){
        String hashedPw = PasswordHasher.hashPassword(registerDTO.password());
        User user = UserMapper.fromDTO(registerDTO, hashedPw);
        User created = userDAO.create(user);
        return UserMapper.fromEntity(created);
    }

//    public List<UserResponseDTO> getAllUsers(){
//
//    }
}
