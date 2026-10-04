package app.endpoints;

import app.config.ApplicationConfig;
import app.config.HibernateConfig;
import app.config.HibernateTestConfig;
import app.entities.Game;
import app.entities.Player;
import app.entities.enums.GameMode;
import app.testutils.GameTestPopulator;
import io.javalin.Javalin;
import io.javalin.http.ContentType;
import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GameEndpointsTest {

    EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
    ApplicationConfig applicationConfig;
    Javalin app;
    Map<String, Game> seed;

    @BeforeAll
    void init() {
        applicationConfig = new ApplicationConfig(emf);
        app = applicationConfig.createApp();
        app.start(0);
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = app.port();
    }

    @AfterAll
    void shutDown() {
        app.stop();
    }

    @BeforeEach
    void setUp() {
        seed = GameTestPopulator.populate(emf);
    }

    @AfterEach
    void tearDown(){
        seed.clear();
    }

    @Test
    void getGame_existing_returns200() {
        Integer id = seed.get("game1").getId();

        given()
                .when()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/v1/games/{id}", id)
                .then().statusCode(200)
                .body("id", is(id))
                .body("gameStatus", is("IN_PROGRESS"));
    }


    private ValidatableResponse move(int gameId, String from, String to) {
        return given().contentType(ContentType.JSON)
                .body(Map.of("from", from, "to", to))
                .when().post("/api/v1/games/{id}/moves", gameId)
                .then();
    }
}
