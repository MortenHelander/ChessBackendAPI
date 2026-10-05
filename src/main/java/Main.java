import app.config.ApplicationConfig;
import app.config.HibernateConfig;
import io.javalin.Javalin;
import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {

    public static void main(String[] args) {

        EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
        ApplicationConfig applicationConfig = new ApplicationConfig(emf);

        Javalin app = applicationConfig.createApp();
        app.start(7070);
    }
}
