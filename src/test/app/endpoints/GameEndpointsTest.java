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
    void getGame_illegalId_returns400(){
        Integer id = -5;

        given()
                .when()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/v1/games/{id}", id)
                .then()
                .log().all()
                .statusCode(400)
                .body("error", is("ID must be positive"));
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
    void moveAndShiftTurn_fromPositionNull_returns400(){
        Integer id = seed.get("game3").getId();

        move(id, null ,"e3", null)
                .statusCode(400)
                .body("error", is("From position is required"));
    }

    @Test
    void moveAndShiftTurn_toPositionNull_returns400(){
        Integer id = seed.get("game3").getId();

        move(id, "e3" ,null, null)
                .statusCode(400)
                .body("error", is("To position is required"));
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
    void moveAndShiftTurn_noPieceOnFromSquare_returns400() {
        Integer id = seed.get("game3").getId();

        move(id, "e5", "h5", null)
                .statusCode(400)
                .body("error", is("No piece on selected square"));
    }


    @Test
    void moveAndShiftTurn_legalMove_notValidPieceAndTurn_returns400() {
        Integer id = seed.get("game3").getId();

        move(id, "e7", "e5", null)
                .statusCode(400)
                .body("error", is("Not your turn"));
    }

    @Test
    void moveAndShiftTurn_illegalMove_returns400(){
        Integer id = seed.get("game3").getId();

        move(id, "e2", "e7", null)
                .statusCode(400)
                .body("error", is("Illegal move"));
    }

    @Test
    void moveAndShiftTurn_foolsMate_blackWins() {
        Integer id = seed.get("game3").getId();
        playAll(id, "f2f3", "e7e5", "g2g4", "d8h4");

        when().get("/api/v1/games/{id}", id)
                .then().log().all().statusCode(200)
                .body("gameStatus", is("CHECKMATE"))
                .body("winnerColor", is("BLACK"));

        move(id, "a2", "a3", null)
                .statusCode(400).body("error", is("Game is finished"));
    }

    @Test
    void moveAndShiftTurn_promotionAllConditionsMet_returns201(){
        Integer id = seed.get("game3").getId();

        playUpToPromotion(id);
        move(id, "a7", "b8", "q")
                .statusCode(201)
                .body("uci", is("a7b8q"));
        playAll(id, "a8b8", "e2e3");

        when().get("/api/v1/games/{id}/moves", id)
                .then().log().all()
                .body("size()", is(11))
                .body("uci", hasItem("a7b8q"));
    }

    @Test
    void moveAndShiftTurn_noSelectedPromotionPiece_returns400(){
        Integer id = seed.get("game3").getId();

        playUpToPromotion(id);
        move(id, "a7", "b8", null)
                .statusCode(400)
                .body("error", is("Promotion piece required"));
    }

    @Test
    void moveAndShiftTurn_wrongPromotionLetter_returns400() {
        Integer id = seed.get("game3").getId();
        String wrongLetter = "z";

        playUpToPromotion(id);
        move(id, "a7", "b8", wrongLetter)
                .statusCode(400)
                .body("error", is("Unknown promotion piece type: " + wrongLetter));
    }

    @Test
    void moveAndShiftTurn_promotionAllConditionsMet_illegalMove_returns400() {
        Integer id = seed.get("game3").getId();

        playUpToPromotion(id);

        //a8 is occupied by black rook, so white pawn can't move their legally
        move(id, "a7", "a8", "q")
                .statusCode(400)
                .body("error", is("Illegal move"));
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

    private void playAll(int gameId, String... ucis) {
        for (String uci : ucis) {
            String promotion = uci.length() == 5 ? uci.substring(4) : null; //if the uci (e2e4 fx) has 5 letters the fith is promotion, otherwise it's null
            move(gameId, uci.substring(0, 2), uci.substring(2, 4), promotion)
                    .statusCode(201);
        }
    }

    private void playUpToPromotion(int id) {
        playAll(id, "a2a4", "b7b5", "a4b5", "a7a6", "b5a6", "c8b7", "a6a7", "h7h6");
    }
}
