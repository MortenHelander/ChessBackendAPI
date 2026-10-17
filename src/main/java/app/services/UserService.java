package app.services;

import app.daos.UserDAO;
import app.dtos.users.*;
import app.entities.User;
import app.exceptions.ApiException;
import app.mappers.UserMapper;
import app.utils.PasswordHasher;

import java.util.ArrayList;
import java.util.List;

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

    public List<UserResponseDTO> getAllUsers(){
        List<User> users = userDAO.getAll();
        List<UserResponseDTO> userDtos = new ArrayList<>();
        for (User user : users) {
            userDtos.add(UserMapper.responseDTOFromEntity(user));
        }
        return userDtos;
    }

    public UserResponseDTO getUserById(Integer id){
        return UserMapper.responseDTOFromEntity(userDAO.getById(id));
    }

    public UserResponseDTO getUserByUsername(String username){
        if (username == null || username.isBlank()){
            throw new ApiException(404, "User with username: " + username + " not found");
        }
        User user = userDAO.getByUsername(username);
        if (user == null){
            throw new ApiException(404, "User with username " + username + " not found");
        }
        return UserMapper.responseDTOFromEntity(user);
    }

    public UserResponseDTO updateUser(Integer id, UserUpdateDTO request){
        User existing = userDAO.getById(id);
        existing.updateDetails(request.username(), request.email(), request.firstName(), request.lastName());
        return UserMapper.responseDTOFromEntity(userDAO.update(existing));
    }

    public void deleteUser(Integer id){
        userDAO.delete(id);
    }

    public UserVerifiedPasswordDTO verifyUser(UserLoginRequestDTO userLoginRequestDTO){
        User user = userDAO.getByUsername(userLoginRequestDTO.username());
        if (user == null || !PasswordHasher.checkPassword(userLoginRequestDTO.password(), user.getPassword())){
            throw new ApiException(401, "Invalid username or password");
        }
        return UserMapper.verifiedDTOFromEntity(user);
    }
}
