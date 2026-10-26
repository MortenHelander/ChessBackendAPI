package app.controllers;

import app.dtos.users.UserResponseDTO;
import app.dtos.users.UserStatsDTO;
import app.dtos.users.UserUpdateDTO;
import app.dtos.users.UserVerifiedPasswordDTO;
import app.exceptions.ApiException;
import app.security.AccessRoleName;
import app.services.UserService;
import app.utils.EmailValidator;
import app.utils.NameValidator;
import app.utils.PasswordValidator;
import app.utils.UsernameValidator;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

import static io.javalin.apibuilder.ApiBuilder.post;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.put;
import static io.javalin.apibuilder.ApiBuilder.delete;

@Slf4j
public class UserController implements EndpointGroup {
    private UserService userService;


    public UserController (UserService userService){
        this.userService = userService;
    }

    @Override
    public void addEndpoints() {
        get("/api/v1/users", this::getAllUsers, AccessRoleName.ANYONE);
        get("/api/v1/users/{id}", this::getUserById, AccessRoleName.ANYONE);
        get("/api/v1/users/username/{username}", this::getUserByUsername, AccessRoleName.ANYONE);
        get("/api/v1/users/{id}/stats", this::getUserStatsById, AccessRoleName.USER);
        put("/api/v1/users/{id}", this::updateUser, AccessRoleName.USER, AccessRoleName.ADMIN);
        delete("/api/v1/users/{id}", this::deleteUser, AccessRoleName.ADMIN);
    }

    public void getAllUsers(Context ctx){
        List<UserResponseDTO> users = userService.getAllUsers();
        ctx.status(HttpStatus.OK);
        ctx.json(users);
    }

    public void getUserById(Context ctx){
        int id = ctx.pathParamAsClass("id", Integer.class)
                .check(value -> value > 0, "ID must be positive").get();
        UserResponseDTO userResponseDTO = userService.getUserById(id);
        ctx.status(HttpStatus.OK);
        ctx.json(userResponseDTO);
    }

    public void getUserByUsername(Context ctx){
        String username = ctx.pathParamAsClass("username", String.class)
                .check(name -> name != null && !name.isBlank(), "ID must be positive").get();
        UserResponseDTO userResponseDTO = userService.getUserByUsername(username);
        ctx.status(HttpStatus.OK);
        ctx.json(userResponseDTO);
    }

    public void getUserStatsById(Context ctx){
        int id = ctx.pathParamAsClass("id", Integer.class)
                .check(value -> value > 0, "ID must be positive").get();

        UserStatsDTO response = userService.getUserStatsById(id);
        ctx.status(HttpStatus.OK);
        ctx.json(response);
    }

    public void updateUser(Context ctx){
        int id = ctx.pathParamAsClass("id", Integer.class)
                .check(value -> value > 0, "ID must be positive").get();

        UserVerifiedPasswordDTO caller = ctx.attribute("user");
        UserResponseDTO target = userService.getUserById(id);

        //check if the original username matches the one in database or is admin, so users can only update themselves
        if (!caller.roles().contains(AccessRoleName.ADMIN.name()) && !caller.username().equals(target.username())){
            throw new ApiException(403, "You can only update your own profile");
        }

        UserUpdateDTO request = ctx.bodyValidator(UserUpdateDTO.class).get();

        List<String> messages = new ArrayList<>();
        messages.addAll(UsernameValidator.validate(request.username()));
        messages.addAll(NameValidator.validateFirstName(request.firstName()));
        messages.addAll(NameValidator.validateLastName(request.lastName()));
        messages.addAll(EmailValidator.validate(request.email()));
        if (!messages.isEmpty()){
            throw new ApiException(400, String.join(", ", messages));
        }

        UserResponseDTO updated = userService.updateUser(id, request);

        ctx.status(HttpStatus.OK);
        ctx.json(updated);
    }


    public void deleteUser(Context ctx){
        int id = ctx.pathParamAsClass("id", Integer.class)
                .check(value -> value > 0, "ID must be positive").get();

        userService.deleteUser(id);
        ctx.status(HttpStatus.NO_CONTENT);
    }
}
