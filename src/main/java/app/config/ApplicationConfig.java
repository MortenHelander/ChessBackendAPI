package app.config;

import app.controllers.UserController;
import app.daos.UserDAO;
import app.services.UserService;
import io.javalin.apibuilder.EndpointGroup;
import jakarta.persistence.EntityManagerFactory;

public class ApplicationConfig implements EndpointGroup {
    private final UserController userController;

    public ApplicationConfig(EntityManagerFactory emf){

        UserDAO userDAO = new UserDAO(emf);
        UserService userService = new UserService(userDAO);
        this.userController = new UserController(userService);
    }


    @Override
    public void addEndpoints() {
        userController.addEndpoints();
    }
}
