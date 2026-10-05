package app.config;

import app.controllers.GameController;
//import app.controllers.UserController;
import app.daos.GameDAO;
import app.daos.MoveDAO;
import app.daos.PlayerDAO;
import app.daos.UserDAO;
import app.exceptions.ApiException;
import app.gameengine.exceptions.InvalidGameActionException;
import app.services.GameService;
import app.services.UserService;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.javalin.Javalin;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.json.JavalinJackson;
import io.javalin.validation.ValidationError;
import io.javalin.validation.ValidationException;
import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class ApplicationConfig implements EndpointGroup {
//    private final UserController userController;
    private final GameController gameController;

    public ApplicationConfig(EntityManagerFactory emf){

        UserDAO userDAO = new UserDAO(emf);
        UserService userService = new UserService(userDAO);
//        this.userController = new UserController(userService);

        GameDAO gameDAO = new GameDAO(emf);
        PlayerDAO playerDAO = new PlayerDAO(emf);
        MoveDAO moveDAO = new MoveDAO(emf);
        GameService gameService = new GameService(playerDAO, gameDAO, moveDAO);
        this.gameController = new GameController(gameService);
    }


    @Override
    public void addEndpoints() {
//        userController.addEndpoints();
        gameController.addEndpoints();
    }


    public Javalin createApp(){
        Javalin app = Javalin.create(config -> {
            config.jsonMapper(new JavalinJackson().updateMapper(mapper -> {
                mapper.registerModule(new JavaTimeModule());
            }));
            config.router.apiBuilder(this);
        });

        app.exception(ApiException.class, (e, ctx) -> {
            if (e.getCode() >= 500) {
                log.error("Server error: {}", e.getMessage());
            } else {
                log.warn("Client error ({}): {}", e.getCode(), e.getMessage());
            }
            ctx.status(e.getCode()).json(Map.of("error", e.getMessage()));
        });

        app.exception(ValidationException.class, (e, ctx) -> {
            String messages = e.getErrors().values().stream()
                    .flatMap(List::stream)
                    .map(ValidationError::getMessage)
                    .collect(Collectors.joining(", "));
            log.warn("Validation failed: {}", messages);
            ctx.status(400).json(Map.of("error", messages));
        });

        app.exception(InvalidGameActionException.class, (e, ctx) -> {
            log.warn("Invalid game action: {}", e.getMessage());
            ctx.status(400).json(Map.of("error", e.getMessage()));
        });

        app.exception(Exception.class, (exception, ctx) -> {
            log.error("Unhandled exception", exception);
            ctx.status(500).json(Map.of("error", "Internal server error"));
        });
        return app;
    }
}
