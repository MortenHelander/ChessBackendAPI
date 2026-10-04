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

        Javalin app = applicationConfig.createApp();
        app.start(7070);
    }
}
