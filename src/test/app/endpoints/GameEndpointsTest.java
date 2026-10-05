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

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.*;

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
    void getGames_hasSize3_returns200() {

        given()
                .when()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/v1/games")
                .then().statusCode(200)
                .body("size()", is(3));

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

    @Test
    void getGame_notExisting_returns404() {
        Integer id = 9999;

        given()
                .when()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/v1/games/{id}", id)
                .then().statusCode(404)
                .body("error", is("Game with id "+ id + " not found"));
    }


    @Test
    void deleteGame_existing_returns204(){
        Integer id = seed.get("game1").getId();

        given()
                .when()
                .contentType(ContentType.JSON)
                .when()
                .delete("/api/v1/games/{id}", id)
                .then().statusCode(204)
                .body(is(""));
    }

    @Test
    void deleteGame_notExisting_returns404(){
        Integer id = 9999;

        given()
                .when()
                .contentType(ContentType.JSON)
                .when()
                .delete("/api/v1/games/{id}", id)
                .then().statusCode(404)
                .body("error", is("Game with id "+ id + " not found"));
    }

    @Test
    void getMovesByGameId_existing_returns200() {
        Integer id = seed.get("game1").getId();

        given()
                .when()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/v1/games/{id}/moves", id)
                .then().statusCode(200)
                .body("size()", is(4))
                .body("moveNumber", contains(1, 2, 3, 4));
    }

    @Test
    void getMovesByGameId_notExisting_returns404() {
        Integer id = 9999;

        given()
                .when()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/v1/games/{id}/moves", id)
                .then().log().all().statusCode(404)
                .body("error", is("Game with id "+ id + " not found"));
    }


    @Test
    void moveAndShiftTurn_legalMove_returns201() {
        Integer id = seed.get("game3").getId();

        move(id, "e2", "e4", null)
                .statusCode(201)
                .body("id", notNullValue())
                .body("moveNumber", is(1))
                .body("uci", is("e2e4"));
    }

    @Test
    void moveAndShiftTurn_illegalMove_returns201() {
        Integer id = seed.get("game3").getId();

        move(id, "e5", "h5", null)
                .statusCode(400)
                .body("error", is("No piece on selected square"))
                .body("moveNumber", is(1))
                .body("uci", is("e2e4"));
    }





    private ValidatableResponse move(int gameId, String from, String to, String promotion) {
        Map<String, String> body = new HashMap<>();
        body.put("from", from);
        body.put("to", to);
        if (promotion != null) body.put("promotionLetter", promotion);
        return given().contentType(ContentType.JSON).body(body)
                .when().post("/api/v1/games/{id}/moves", gameId)
                .then().log().all();
    }
}
