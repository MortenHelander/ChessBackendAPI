package app.controllers;

import app.dtos.users.*;
import app.exceptions.ApiException;
import app.mappers.UserMapper;
import app.security.AccessRoleName;
import app.security.TokenService;
import app.services.UserService;
import app.utils.EmailValidator;
import app.utils.NameValidator;
import app.utils.PasswordValidator;
import app.utils.UsernameValidator;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.post;

public class AuthController implements EndpointGroup {
    private final UserService userService;
    private final TokenService tokenService;

    public AuthController(UserService userService, TokenService tokenService){
        this.userService = userService;
        this.tokenService = tokenService;
    }

    @Override
    public void addEndpoints() {
        post("/api/v1/auth/register", this::register, AccessRoleName.ANYONE);
        post("/api/v1/auth/login", this::login, AccessRoleName.ANYONE);
        get("/api/v1/auth/healthcheck", this::healthCheck, AccessRoleName.ANYONE);
    }

    public void register(Context ctx){
        UserRegisterDTO request = ctx.bodyValidator(UserRegisterDTO.class).get();

        List<String> messages = new ArrayList<>();
        messages.addAll(UsernameValidator.validate(request.username()));
        messages.addAll(NameValidator.validateFirstName(request.firstName()));
        messages.addAll(NameValidator.validateLastName(request.lastName()));
        messages.addAll(EmailValidator.validate(request.email()));
        messages.addAll(PasswordValidator.validate(request.password(), request.passwordCheck()));
        if (!messages.isEmpty()){
            throw new ApiException(400, String.join(", ", messages));
        }

        UserResponseDTO created = userService.createUser(request);
        ctx.status(HttpStatus.CREATED); //201
        ctx.json(created);
    }

    public void login(Context ctx){

        UserLoginRequestDTO request = ctx.bodyValidator(UserLoginRequestDTO.class)
                .check(dto -> dto.username() != null && !dto.username().isBlank() , "Username is required")
                .check(dto -> dto.password() != null && !dto.password().isBlank() , "Password is required")
                .get();

        UserVerifiedPasswordDTO verified = userService.verifyUser(request);
        String token = tokenService.createToken(verified);
        UserLoginResponseDTO response = UserMapper.fromVerifiedDtoToResponseDto(verified, token);
        ctx.status(200);
        ctx.json(response);
    }

    public void healthCheck(@NotNull Context ctx) {
        ctx.status(200).json("{\"msg\": \"API is up and running\"}");
    }
}
