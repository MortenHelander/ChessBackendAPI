package app.endpoints;

import app.config.ApplicationConfig;
import app.config.HibernateTestConfig;
import app.entities.Game;
import app.entities.Player;
import app.entities.User;
import app.entities.enums.Color;
import app.testutils.GameTestPopulator;
import app.testutils.TestTokens;
import io.javalin.Javalin;
import io.javalin.http.ContentType;
import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static app.testutils.TestTokens.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GameEndpointsTest {

    EntityManagerFactory emf = HibernateTestConfig.getEntityManagerFactory();
    ApplicationConfig applicationConfig;
    Javalin app;
    Map<String, Game> seed;

    String userToken = TestTokens.userToken();
    String adminToken = TestTokens.adminToken();

    // Tokens for the two real players in game3 – created after seeding
    String whiteToken;
    String blackToken;

    @BeforeAll
    void init() {
        applicationConfig = new ApplicationConfig(emf);
        app = applicationConfig.createApp(0);
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

        Game game3 = seed.get("game3");
        whiteToken = tokenForPlayer(game3, Color.WHITE);
        blackToken = tokenForPlayer(game3, Color.BLACK);
    }

    @AfterEach
    void tearDown() {
        seed.clear();
    }

    @Test
    void getGames_hasSize3_returns200() {

        given()
                .header("Authorization", bearer(userToken))
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
                .header("Authorization", bearer(userToken))
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
                .header("Authorization", bearer(userToken))
                .when()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/v1/games/{id}", id)
                .then().statusCode(404)
                .body("error", is("Game with id " + id + " not found"));
    }

    @Test
    void getGame_illegalId_returns400() {
        Integer id = -5;

        given()
                .header("Authorization", bearer(userToken))
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
    void deleteGame_existing_returns204() {
        Integer id = seed.get("game1").getId();

        given()
                .header("Authorization", bearer(adminToken))
                .when()
                .contentType(ContentType.JSON)
                .when()
                .delete("/api/v1/games/{id}", id)
                .then().statusCode(204)
                .body(is(""));
    }

    @Test
    void deleteGame_notExisting_returns404() {
        Integer id = 9999;

        given()
                .header("Authorization", bearer(adminToken))
                .when()
                .contentType(ContentType.JSON)
                .when()
                .delete("/api/v1/games/{id}", id)
                .then().statusCode(404)
                .body("error", is("Game with id " + id + " not found"));
    }

    @Test
    void getMovesByGameId_existing_returns200() {
        Integer id = seed.get("game1").getId();

        given()
                .header("Authorization", bearer(userToken))
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
                .header("Authorization", bearer(userToken))
                .when()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/v1/games/{id}/moves", id)
                .then().log().all().statusCode(404)
                .body("error", is("Game with id " + id + " not found"));
    }

    @Test
    void moveAndShiftTurn_fromPositionNull_returns400() {
        Integer id = seed.get("game3").getId();

        move(whiteToken, id, null, "e3", null)
                .statusCode(400)
                .body("error", is("From position is required"));
    }

    @Test
    void moveAndShiftTurn_toPositionNull_returns400() {
        Integer id = seed.get("game3").getId();

        move(whiteToken, id, "e3", null, null)
                .statusCode(400)
                .body("error", is("To position is required"));
    }


    @Test
    void moveAndShiftTurn_legalMove_returns201() {
        Integer id = seed.get("game3").getId();

        move(whiteToken, id, "e2", "e4", null)
                .statusCode(201)
                .body("id", notNullValue())
                .body("moveNumber", is(1))
                .body("uci", is("e2e4"));
    }

    @Test
    void moveAndShiftTurn_noPieceOnFromSquare_returns400() {
        Integer id = seed.get("game3").getId();

        move(whiteToken, id, "e5", "h5", null)
                .statusCode(400)
                .body("error", is("No piece on selected square"));
    }

    //Who-is-moving tests

    @Test
    void moveAndShiftTurn_blackPlayerOnWhitesTurn_returns400() {
        Integer id = seed.get("game3").getId();

        // Black tries to move one of their own pieces, but it's white's turn
        move(blackToken, id, "e7", "e5", null)
                .statusCode(400)
                .body("error", is("Not your turn"));
    }

    @Test
    void moveAndShiftTurn_whitePlayerMovesOpponentsPiece_returns400() {
        Integer id = seed.get("game3").getId();

        // It is white's turn, but white grabs a black pawn.
        move(whiteToken, id, "e7", "e5", null)
                .statusCode(400)
                .body("error", is("You can't move your opponent's pieces"));
    }

    @Test
    void moveAndShiftTurn_userNotInGame_returns403() {
        Integer id = seed.get("game3").getId();

        // userToken's user (id 4) is not a player in game3
        move(userToken, id, "e2", "e4", null)
                .statusCode(403)
                .body("error", is("You are not a player in this game"));
    }

    // -----------------------------------------------

    @Test
    void moveAndShiftTurn_illegalMove_returns400() {
        Integer id = seed.get("game3").getId();

        move(whiteToken, id, "e2", "e7", null)
                .statusCode(400)
                .body("error", is("Illegal move"));
    }

    @Test
    void moveAndShiftTurn_foolsMate_blackWins() {
        String userWithStatsToken = TestTokens.userWithStatsToken();
        Integer id = seed.get("game3").getId();
        playAll(id, true, "f2f3", "e7e5", "g2g4", "d8h4");

        given().header("Authorization", bearer(userWithStatsToken))
                .when().get("/api/v1/games/{id}", id)
                .then().log().all().statusCode(200)
                .body("gameStatus", is("CHECKMATE"))
                .body("winnerColor", is("BLACK"));

        // After black's mating move it's white's turn
        move(whiteToken, id, "a2", "a3", null)
                .statusCode(400).body("error", is("Game is finished"));
    }

    @Test
    void moveAndShiftTurn_promotionAllConditionsMet_returns201() {
        Integer id = seed.get("game3").getId();

        playUpToPromotion(id);
        move(whiteToken, id, "a7", "b8", "q")
                .statusCode(201)
                .body("uci", is("a7b8q"));
        // After white's promotion it is black's turn
        playAll(id, false, "a8b8", "e2e3");

        given().header("Authorization", bearer(userToken))
                .when().get("/api/v1/games/{id}/moves", id)
                .then().log().all()
                .body("size()", is(11))
                .body("uci", hasItem("a7b8q"));
    }

    @Test
    void moveAndShiftTurn_noSelectedPromotionPiece_returns400() {
        Integer id = seed.get("game3").getId();

        playUpToPromotion(id);
        move(whiteToken, id, "a7", "b8", null)
                .statusCode(400)
                .body("error", is("Promotion piece required"));
    }

    @Test
    void moveAndShiftTurn_wrongPromotionLetter_returns400() {
        Integer id = seed.get("game3").getId();
        String wrongLetter = "z";

        playUpToPromotion(id);
        move(whiteToken, id, "a7", "b8", wrongLetter)
                .statusCode(400)
                .body("error", is("Unknown promotion piece type: " + wrongLetter));
    }

    @Test
    void moveAndShiftTurn_promotionAllConditionsMet_illegalMove_returns400() {
        Integer id = seed.get("game3").getId();

        playUpToPromotion(id);

        //a8 is occupied by black rook, so white pawn can't move their legally
        move(whiteToken, id, "a7", "a8", "q")
                .statusCode(400)
                .body("error", is("Illegal move"));
    }

    @Test
    void whitePlayerResigns_bothPlayersStatsChange() {

        Game game = seed.get("game3");
        Integer id = game.getId();

        User wPlayer = game.getPlayers().stream()
                .filter(p -> p.getColor() == Color.WHITE)
                .findFirst()
                .orElseThrow()
                .getUser();

        User bPlayer = game.getPlayers().stream()
                .filter(p -> p.getColor() == Color.BLACK)
                .findFirst()
                .orElseThrow()
                .getUser();

        //wPlayer has zero stats so far
        given().header("Authorization", bearer(whiteToken))
                .when().get("/api/v1/users/{id}/stats", wPlayer.getId())
                .then().log().all()
                .statusCode(200)
                .body("wins", is(0))
                .body("losses", is(0))
                .body("gamesPlayed", is(0));

        //bPlayer has some stats already
        given().header("Authorization", bearer(blackToken))
                .when().get("/api/v1/users/{id}/stats", bPlayer.getId())
                .then().log().all()
                .statusCode(200)
                .body("wins", is(12))
                .body("losses", is(13))
                .body("gamesPlayed", is(25));

        //white player resigns game
        given().header("Authorization", bearer(whiteToken))
                .when().post("/api/v1/games/{id}/resign", id)
                .then().log().all()
                .statusCode(200)
                .body("gameStatus", is("RESIGNED"))
                .body("winnerColor", is("BLACK"));

        //wplayers losses and gameplayed should increment by 1
        given().header("Authorization", bearer(whiteToken))
                .when().get("/api/v1/users/{id}/stats", wPlayer.getId())
                .then().log().all()
                .statusCode(200)
                .body("wins", is(0))
                .body("losses", is(1))
                .body("gamesPlayed", is(1));

        //bplayers losses and gameplayed should increment by 1
        given().header("Authorization", bearer(blackToken))
                .when().get("/api/v1/users/{id}/stats", bPlayer.getId())
                .then().log().all()
                .statusCode(200)
                .body("wins", is(13))
                .body("losses", is(13))
                .body("gamesPlayed", is(26));
    }

    // ---------- Helpers ----------

    private ValidatableResponse move(String token, int gameId, String from, String to, String promotion) {
        Map<String, String> body = new HashMap<>();
        body.put("from", from);
        body.put("to", to);
        if (promotion != null) body.put("promotionLetter", promotion);
        return given().header("Authorization", bearer(token))
                .contentType(ContentType.JSON).body(body)
                .when().post("/api/v1/games/{id}/moves", gameId)
                .then().log().all();
    }

    private void playAll(int gameId, boolean whiteStarts, String... ucis) {
        boolean whitesTurn = whiteStarts;
        for (String uci : ucis) {
            String promotion = uci.length() == 5 ? uci.substring(4) : null; //if the uci (e2e4 fx) has 5 letters the fith is promotion, otherwise it's null
            String token = whitesTurn ? whiteToken : blackToken;
            move(token, gameId, uci.substring(0, 2), uci.substring(2, 4), promotion)
                    .statusCode(201);
            whitesTurn = !whitesTurn;
        }
    }

    private void playUpToPromotion(int id) {
        playAll(id, true, "a2a4", "b7b5", "a4b5", "a7a6", "b5a6", "c8b7", "a6a7", "h7h6");
    }

    //creates a token for user who plays the givne color in the seeded game
    private String tokenForPlayer(Game game, Color color) {
        User user = game.getPlayers().stream()
                .filter(p -> p.getColor() == color)
                .findFirst()
                .orElseThrow()
                .getUser();
        return tokenFor(user.getId(), user.getUsername());
    }
}