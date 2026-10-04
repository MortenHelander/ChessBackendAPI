package app.controllers;

import app.dtos.users.UserCreateDTO;
import app.dtos.users.UserResponseDTO;
import app.dtos.users.UserUpdateDTO;
import app.services.UserService;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import static io.javalin.apibuilder.ApiBuilder.*;

//@Slf4j
//public class UserController implements EndpointGroup {
//    private UserService userService;


//    public UserController (UserService userService){
//        this.userService = userService;
//    }
//
//    @Override
//    public void addEndpoints() {
//        post("/api/v1/users", this::createNewUser);
//        get("/api/v1/users", this::getAllUsers);
//        get("/api/v1/users/{id}", this::getUser);
//        put("/api/v1/users/{id}", this::updateUser);
//        delete("/api/v1/users/{id}", this::deleteUser);
//    }
//
//    public void getAllUsers(Context ctx){
//        List<UserResponseDTO> users = userService.getAllUsers();
//        ctx.status(HttpStatus.OK);
//        ctx.json(users);
//    }
//
//    public void getUser(Context ctx){
//        int id = ctx.pathParamAsClass("id", Integer.class)
//                .check(value -> value > 0, "ID must be positive").get();
//        UserResponseDTO userResponseDTO = userService.getUser(id);
//        ctx.status(HttpStatus.OK); //200
//        ctx.json(userResponseDTO);
//    }
//
//    public void createNewUser(Context ctx){
//        UserCreateDTO request = ctx.bodyValidator(UserCreateDTO.class)
//                .check(dto -> dto.firstName() != null && !dto.firstName().isBlank(), "First name is empty")
//                .check(dto -> dto.lastName()  != null && !dto.lastName().isBlank(),  "Last name is empty")
//                .check(dto -> dto.email()     != null && !dto.email().isBlank(),     "Email is empty")
//                .check(dto -> dto.username()  != null && !dto.username().isBlank(),  "Username is empty")
//                .check(dto -> dto.password()  != null && dto.password().length() >= 8, "Password too short")
//                .get();
//
//        UserResponseDTO created = userService.createUser(request);
//        ctx.status(HttpStatus.CREATED); //201
//        ctx.json(created);
//    }
//
//    public void updateUser(Context ctx){
//        int id = ctx.pathParamAsClass("id", Integer.class)
//                .check(value -> value > 0, "ID must be positive").get();
//
//        UserUpdateDTO request = ctx.bodyValidator(UserUpdateDTO.class)
//                .check(dto -> dto.firstName() != null && !dto.firstName().isBlank(), "First name is empty")
//                .check(dto -> dto.lastName()  != null && !dto.lastName().isBlank(),  "Last name is empty")
//                .check(dto -> dto.email()     != null && !dto.email().isBlank(),     "Email is empty")
//                .check(dto -> dto.username()  != null && !dto.username().isBlank(),  "Username is empty")
//                .get();
//
//        UserResponseDTO updated = userService.updateUser(id, request);
//
//        ctx.status(HttpStatus.OK); //200
//        ctx.json(updated);
//    }
//
//
//    public void deleteUser(Context ctx){
//        int id = ctx.pathParamAsClass("id", Integer.class)
//                .check(value -> value > 0, "ID must be positive").get();
//
//        userService.deleteUser(id);
//        ctx.status(HttpStatus.NO_CONTENT); //204
//    }
//}
