import app.config.ApplicationConfig;
import app.config.HibernateConfig;
import app.exceptions.ApiException;
import app.gameengine.exceptions.InvalidGameActionException;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.javalin.Javalin;
import io.javalin.json.JavalinJackson;
import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class Main {

    public static void main(String[] args) {

        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
        ApplicationConfig applicationConfig = new ApplicationConfig(emf);

        Javalin app = Javalin.create(config -> {
            config.jsonMapper(new JavalinJackson().updateMapper(mapper -> {
                mapper.registerModule(new JavaTimeModule());
            }));
            config.router.apiBuilder(applicationConfig);
        });

        app.exception(ApiException.class, (e, ctx) -> {
            if (e.getCode() >= 500) {
                log.error("Server error: {}", e.getMessage());
            } else {
                log.warn("Client error ({}): {}", e.getCode(), e.getMessage());
            }
            ctx.status(e.getCode()).json(Map.of("error", e.getMessage()));
        });

        app.exception(InvalidGameActionException.class, (e, ctx) -> {
            log.warn("Invalid game action: {}", e.getMessage());
            ctx.status(400).json(Map.of("error", e.getMessage()));
        });

        app.exception(Exception.class, (exception, ctx) -> {
            log.error("Unhandled exception", exception);
            ctx.status(500).json(Map.of("error", "Internal server error"));
        });

        app.start(7070);
    }
}
