package app.config;

import app.clients.LichessClient;
import app.controllers.*;
import app.daos.*;
import app.handlers.AccessHandler;
import app.handlers.ExceptionHandler;
import app.security.TokenService;
import app.services.DailyPuzzleService;
import app.services.GameService;
import app.services.UserService;
import app.utils.Utils;
import app.websocket.GameConnectionRegistry;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.javalin.Javalin;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.config.JavalinConfig;
import io.javalin.json.JavalinJackson;
import jakarta.persistence.EntityManagerFactory;

import java.time.Clock;


public class ApplicationConfig implements EndpointGroup {
    private final AccessHandler accessHandler;
    private final ExceptionHandler exceptionHandler;
    private final UserController userController;
    private final GameController gameController;
    private final DailyPuzzleController dailyPuzzleController;
    private final AuthController authController;
    private final GameSocketController gameSocketController;


    public ApplicationConfig(EntityManagerFactory emf){

        //daos
        UserDAO userDAO = new UserDAO(emf);
        GameDAO gameDAO = new GameDAO(emf);
        PlayerDAO playerDAO = new PlayerDAO(emf);
        MoveDAO moveDAO = new MoveDAO(emf);
        DailyPuzzleDAO dailyPuzzleDAO = new DailyPuzzleDAO(emf);

        //services
        TokenService tokenService = new TokenService(Utils.getPropertyValue("ISSUER", "config.properties"),
                Long.parseLong(Utils.getPropertyValue("TOKEN_EXPIRE_TIME", "config.properties")),
                Utils.getPropertyValue("SECRET_KEY", "config.properties"));

        UserService userService = new UserService(userDAO);
        GameService gameService = new GameService(userService, playerDAO, gameDAO, moveDAO, userDAO);
        DailyPuzzleService dailyPuzzleService = new DailyPuzzleService(Clock.systemUTC(), dailyPuzzleDAO, new LichessClient());

        //handlers
        this.exceptionHandler = new ExceptionHandler();
        this.accessHandler = new AccessHandler(tokenService);

        //game registry
        GameConnectionRegistry gameConnectionRegistry = new GameConnectionRegistry();


        //controllers
        this.authController = new AuthController(userService, tokenService);
        this.userController = new UserController(userService);
        this.gameController = new GameController(gameService, gameConnectionRegistry);
        this.dailyPuzzleController = new DailyPuzzleController(dailyPuzzleService);
        this.gameSocketController = new GameSocketController(tokenService, gameConnectionRegistry);
    }

    @Override
    public void addEndpoints() {
        userController.addEndpoints();
        authController.addEndpoints();
        gameController.addEndpoints();
        dailyPuzzleController.addEndpoints();
        gameSocketController.addEndpoints();
    }

    private void configuration(JavalinConfig config){
        config.jsonMapper(new JavalinJackson().updateMapper(mapper -> {
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); //stops dates from being turned into arrays
        }));
        config.routes.apiBuilder(this);
        config.routes.beforeMatched(accessHandler::handle);
        exceptionHandler.configureExceptionHandling(config);
    }


    public Javalin createApp(int port){
        Javalin app = Javalin.create(this::configuration);
        app.start(port);
        return app;
    }
}
