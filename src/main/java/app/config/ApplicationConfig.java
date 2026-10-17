package app.config;

import app.controllers.AuthController;
import app.controllers.GameController;
//import app.controllers.UserController;
import app.controllers.GameSocketController;
import app.controllers.UserController;
import app.daos.GameDAO;
import app.daos.MoveDAO;
import app.daos.PlayerDAO;
import app.daos.UserDAO;
import app.exceptions.ApiException;
import app.gameengine.exceptions.InvalidGameActionException;
import app.handlers.AccessHandler;
import app.handlers.ExceptionHandler;
import app.security.TokenService;
import app.services.GameService;
import app.services.UserService;
import app.utils.Utils;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.javalin.Javalin;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.config.JavalinConfig;
import io.javalin.json.JavalinJackson;
import jakarta.persistence.EntityManagerFactory;


public class ApplicationConfig implements EndpointGroup {
    private final AccessHandler accessHandler;
    private final ExceptionHandler exceptionHandler;
    private final UserController userController;
    private final GameController gameController;
    private final AuthController authController;
    private final GameSocketController gameSocketController;



    public ApplicationConfig(EntityManagerFactory emf){

        //daos
        UserDAO userDAO = new UserDAO(emf);
        GameDAO gameDAO = new GameDAO(emf);
        PlayerDAO playerDAO = new PlayerDAO(emf);
        MoveDAO moveDAO = new MoveDAO(emf);

        //services
        TokenService tokenService = new TokenService(Utils.getPropertyValue("ISSUER", "config.properties"),
                Long.parseLong(Utils.getPropertyValue("TOKEN_EXPIRE_TIME", "config.properties")),
                Utils.getPropertyValue("SECRET_KEY", "config.properties"));

        UserService userService = new UserService(userDAO);
        GameService gameService = new GameService(userService, playerDAO, gameDAO, moveDAO);

        //handlers
        this.exceptionHandler = new ExceptionHandler();
        this.accessHandler = new AccessHandler(tokenService);


        //controllers
        this.authController = new AuthController(userService, tokenService);
        this.userController = new UserController(userService);
        this.gameController = new GameController(gameService);
        this.gameSocketController = new GameSocketController(tokenService);
    }

    @Override
    public void addEndpoints() {
        userController.addEndpoints();
        authController.addEndpoints();
        gameController.addEndpoints();
    }

    private void configuration(JavalinConfig config){
        config.jsonMapper(new JavalinJackson().updateMapper(mapper -> {
            mapper.registerModule(new JavaTimeModule());
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
