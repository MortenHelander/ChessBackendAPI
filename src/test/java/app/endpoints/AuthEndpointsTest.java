package app.endpoints;

import app.config.ApplicationConfig;
import app.config.HibernateTestConfig;
import app.dtos.users.UserVerifiedPasswordDTO;
import app.entities.User;
import app.security.TokenService;
import app.testutils.UserTestPopulator;
import app.utils.Utils;
import io.javalin.Javalin;
import io.javalin.http.ContentType;
import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AuthEndpointsTest {

    EntityManagerFactory emf = HibernateTestConfig.getEntityManagerFactory();
    ApplicationConfig applicationConfig;
    Javalin app;
    TokenService tokenService;
    Map<String, User> seed;

    static final String PASSWORD = "Str0ng!Passw0rd";
    static final String WRONG_PASSWORD = "Wr0ng!Passw0rd";

    @BeforeAll
    void init() {
        applicationConfig = new ApplicationConfig(emf);
        app = applicationConfig.createApp();
        app.start(0);
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = app.port();

        // same issuer/secret/expiry as the app
        tokenService = new TokenService(
                Utils.getPropertyValue("ISSUER", "config.properties"),
                Long.parseLong(Utils.getPropertyValue("TOKEN_EXPIRE_TIME", "config.properties")),
                Utils.getPropertyValue("SECRET_KEY", "config.properties"));
    }

    @AfterAll
    void shutDown() {
        app.stop();
    }

    @BeforeEach
    void setUp() {
        //seeded passwords are not hashed so login tests will be build with new users
        seed = UserTestPopulator.populate(emf);
    }

    @AfterEach
    void tearDown() {
        seed.clear();
    }


    // REGISTER
    @Test
    void register_validUser_returns201() {
        register("newuser", PASSWORD, PASSWORD)
                .statusCode(201)
                .body("id", notNullValue())
                .body("username", is("newuser"))
                .body("email", is("newuser@example.com"))
                .body("firstName", is("Test"));
    }

    @Test
    void register_validUser_hasDefaultStats_returns201() {
        register("newuser", PASSWORD, PASSWORD)
                .statusCode(201)
                .body("userStatsDTO.gamesPlayed", is(0))
                .body("userStatsDTO.wins", is(0))
                .body("userStatsDTO.losses", is(0))
                .body("userStatsDTO.draws", is(0))
                .body("userStatsDTO.mmr", is(500.0f));
    }

    @Test
    void register_validUser_passwordNotInResponse_returns201() {
        register("newuser", PASSWORD, PASSWORD)
                .statusCode(201)
                .body("password", nullValue())
                .body("passwordCheck", nullValue());
    }

    @Test
    void register_mixedCaseUsername_isSavedLowercase_returns201() {
        register("NewUser", PASSWORD, PASSWORD)
                .statusCode(201)
                .body("username", is("newuser"))
                .body("email", is("newuser@example.com"));
    }

    @Test
    void register_usernameTaken_returns409() {
        String takenUsername = seed.get("user1").getUsername();

        register(takenUsername, PASSWORD, PASSWORD)
                .statusCode(409)
                .body("error", is("Username is taken"));
    }

    @Test
    void register_emailTaken_returns409() {
        String takenEmail = seed.get("user1").getEmail();

        register("newuser", takenEmail, PASSWORD, PASSWORD)
                .statusCode(409)
                .body("error", is("Email is taken"));
    }

    @Test
    void register_sameUserTwice_returns409() {
        register("newuser", PASSWORD, PASSWORD).statusCode(201);

        register("newuser", PASSWORD, PASSWORD)
                .statusCode(409)
                .body("error", is("Username is taken"));
    }

    @Test
    void register_emptyEmail_returns400() {
        register("newuser", "", PASSWORD, PASSWORD)
                .statusCode(400)
                .body("error", is("Email is empty"));
    }

    @Test
    void register_invalidEmail_returns400() {
        register("newuser", "not-an-email", PASSWORD, PASSWORD)
                .statusCode(400)
                .body("error", is("Email must consist of: (your-email)@(email-provider).(address)"));
    }

    @Test
    void register_emptyPassword_returns400() {
        register("newuser", "", "")
                .statusCode(400)
                .body("error", is("Password is empty"));
    }

    @Test
    void register_passwordTooShort_returns400() {
        register("newuser", "Sh0rt!", "Sh0rt!")
                .statusCode(400)
                .body("error", is("Password must be at least 8 characters"));
    }

    @Test
    void register_passwordTooLong_returns400() {
        String tooLong = "Aa1!" + "a".repeat(70); // 74 bytes

        register("newuser", tooLong, tooLong)
                .statusCode(400)
                .body("error", is("Password must be at most 72 UTF8 bytes"));
    }

    @Test
    void register_passwordNoUppercase_returns400() {
        register("newuser", "str0ng!passw0rd", "str0ng!passw0rd")
                .statusCode(400)
                .body("error", is("Password must contain a mixture of upper and lower case letters"));
    }

    @Test
    void register_passwordNoDigit_returns400() {
        register("newuser", "Strong!Password", "Strong!Password")
                .statusCode(400)
                .body("error", is("Password must contain at least one number 0-9"));
    }

    @Test
    void register_passwordNoSpecialCharacter_returns400() {
        register("newuser", "Str0ngPassw0rd", "Str0ngPassw0rd")
                .statusCode(400)
                .body("error", is("Password must contain at least one special character"));
    }

    @Test
    void register_passwordsDontMatch_returns400() {
        register("newuser", PASSWORD, WRONG_PASSWORD)
                .statusCode(400)
                .body("error", is("Passwords don't match"));
    }

    @Test
    void register_multipleErrors_allMessagesJoined_returns400() {
        register("newuser", "not-an-email", PASSWORD, WRONG_PASSWORD)
                .statusCode(400)
                .body("error", is("Email must consist of: (your-email)@(email-provider).(address), Passwords don't match"));
    }

    // LOGIN
    @Test
    void login_validCredentials_returns200() {
        register("newuser", PASSWORD, PASSWORD).statusCode(201);

        login("newuser", PASSWORD)
                .statusCode(200)
                .body("username", is("newuser"))
                .body("token", not(emptyOrNullString()))
                .body("roles", notNullValue());
    }

    @Test
    void login_validCredentials_returnsVerifiableToken() {
        register("newuser", PASSWORD, PASSWORD).statusCode(201);

        String token = login("newuser", PASSWORD)
                .statusCode(200)
                .extract().path("token");

        UserVerifiedPasswordDTO verified = tokenService.verify(token);
        assertEquals("newuser", verified.username());
    }

    @Test
    void login_wrongPassword_returns401() {
        register("newuser", PASSWORD, PASSWORD).statusCode(201);

        login("newuser", WRONG_PASSWORD)
                .statusCode(401)
                .body("error", is("Invalid username or password"));
    }

    @Test
    void login_unknownUsername_returns401() {
        login("doesnotexist", PASSWORD)
                .statusCode(401)
                .body("error", is("Invalid username or password"));
    }

    @Test
    void login_missingUsername_returns400() {
        login(null, PASSWORD)
                .statusCode(400)
                .body("error", is("Username is required"));
    }

    @Test
    void login_blankUsername_returns400() {
        login("   ", PASSWORD)
                .statusCode(400)
                .body("error", is("Username is required"));
    }

    @Test
    void login_missingPassword_returns400() {
        login("newuser", null)
                .statusCode(400)
                .body("error", is("Password is required"));
    }

    @Test
    void login_blankPassword_returns400() {
        login("newuser", "")
                .statusCode(400)
                .body("error", is("Password is required"));
    }


    // HELPERS
    private Map<String, String> registerBody(String username, String email, String password, String passwordCheck) {
        Map<String, String> body = new HashMap<>();
        body.put("username", username);
        body.put("email", email);
        body.put("firstName", "Test");
        body.put("lastName", "Person");
        body.put("password", password);
        body.put("passwordCheck", passwordCheck);
        return body;
    }

    private ValidatableResponse register(String username, String password, String passwordCheck) {
        return register(username, username + "@example.com", password, passwordCheck);
    }

    private ValidatableResponse register(String username, String email, String password, String passwordCheck) {
        return post("/api/v1/auth/register", registerBody(username, email, password, passwordCheck));
    }

    private ValidatableResponse login(String username, String password) {
        Map<String, String> body = new HashMap<>();
        body.put("username", username);
        body.put("password", password);
        return post("/api/v1/auth/login", body);
    }

    private ValidatableResponse post(String path, Map<String, String> body) {
        return given().contentType(ContentType.JSON).body(body)
                .when().post(path)
                .then().log().all();
    }
}