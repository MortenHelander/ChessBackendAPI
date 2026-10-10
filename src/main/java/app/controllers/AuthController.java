package app.controllers;

import app.dtos.users.UserLoginRequestDTO;
import app.dtos.users.UserRegisterDTO;
import app.dtos.users.UserResponseDTO;
import app.dtos.users.VerifiedUserDTO;
import app.exceptions.ApiException;
import app.security.JWTService;
import app.services.AuthService;
import app.services.UserService;
import app.utils.EmailValidator;
import app.utils.PasswordValidator;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.post;

@Slf4j
public class AuthController implements EndpointGroup {
    private final UserService userService;
    private final AuthService authService;

    public AuthController(UserService userService, AuthService authService){
        this.userService = userService;
        this.authService = authService;
    }

    @Override
    public void addEndpoints() {
        post("/api/v1/auth/register", this::register);
        post("/api/v1/auth/login", this::login);
    }

    public void register(Context ctx){
        UserRegisterDTO request = ctx.bodyValidator(UserRegisterDTO.class).get();

        List<String> messages = new ArrayList<>();
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

        UserLoginRequestDTO request = ctx.bodyValidator(UserLoginRequestDTO.class).get();

        VerifiedUserDTO verified = userService.verifyUser(request);
        String token = authService.createToken(verified);


    }
}
