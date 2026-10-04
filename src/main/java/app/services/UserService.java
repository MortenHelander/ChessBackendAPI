package app.services;

import app.daos.UserDAO;
import app.dtos.users.UserResponseDTO;
import io.javalin.http.Context;

import java.util.List;

public class UserService {
    private final UserDAO userDAO;

    public UserService(UserDAO userDAO){
        this.userDAO = userDAO;
    }

//    public List<UserResponseDTO> getAllUsers(){
//
//    }
}
