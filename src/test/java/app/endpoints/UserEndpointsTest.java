package app.endpoints;

import app.config.ApplicationConfig;
import app.config.HibernateTestConfig;
import app.entities.User;
import app.testutils.UserTestPopulator;
import io.javalin.Javalin;
import io.javalin.http.ContentType;
import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import java.util.HashMap;
import java.util.Map;

import static app.testutils.TestTokens.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserEndpointsTest {

    static final String AUTH = "Authorization";
    static final String USERS = "/api/v1/users";

    EntityManagerFactory emf = HibernateTestConfig.getEntityManagerFactory();
    Javalin app;
    Map<String, User> seed;

    String userToken = userToken();
    String adminToken = adminToken();
    String ownerToken;

    @BeforeAll
    void init() {
        app = new ApplicationConfig(emf).createApp(0);
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = app.port();
    }

    @AfterAll
    void shutDown() {
        app.stop();
    }

    @BeforeEach
    void setUp() {
        seed = UserTestPopulator.populate(emf);
        ownerToken = tokenFor(seed.get("user1").getUsername());
    }

    @AfterEach
    void tearDown() {
        seed.clear();
    }

    @Test
    void getAllUsers_hasSize3_returns200() {
        given()
                .when().get(USERS)
                .then().statusCode(200)
                .body("size()", is(3))
                .body("username", containsInAnyOrder("sheriffen", "svg-man", "frontjoe"));
    }

    @Test
    void getAllUsers_passwordNotInResponse() {
        given()
                .when().get(USERS)
                .then().statusCode(200)
                .body("password", everyItem(nullValue()));
    }

    @Test
    void getUserById_existing_returns200() {
        User user1 = seed.get("user1");

        given()
                .when().get(USERS + "/{id}", user1.getId())
                .then().statusCode(200)
                .body("id", is(user1.getId()))
                .body("username", is("sheriffen"))
                .body("email", is("morten@hotmail.com"))
                .body("firstName", is("Morten"))
                .body("userStatsDTO.mmr", is(1000.0f));
    }

    @Test
    void getUserById_notExisting_returns404() {
        given()
                .when().get(USERS + "/{id}", 9999)
                .then().statusCode(404)
                .body("error", is("User with id 9999 not found"));
    }

    @Test
    void getUserById_negativeId_returns400() {
        given()
                .when().get(USERS + "/{id}", -5)
                .then().statusCode(400)
                .body("error", is("ID must be positive"));
    }

    @Test
    void getUserById_notANumber_returns400() {
        given()
                .when().get(USERS + "/{id}", "abc")
                .then().statusCode(400);
    }

    @Test
    void getUserByUsername_existing_returns200() {
        given()
                .when().get(USERS + "/username/{username}", "sheriffen")
                .then().statusCode(200)
                .body("id", is(seed.get("user1").getId()))
                .body("username", is("sheriffen"));
    }

    @Test
    void getUserByUsername_notExisting_returns404() {
        given()
                .when().get(USERS + "/username/{username}", "doesnotexist")
                .then().statusCode(404)
                .body("error", is("User with username doesnotexist not found"));
    }

    // UPDATE
    @Test
    void updateUser_validData_returns200() {
        Integer id = seed.get("user1").getId();

        update(id, "newname", "Jens", "Hansen", "jens@example.com", ownerToken)
                .statusCode(200)
                .body("id", is(id))
                .body("username", is("newname"))
                .body("firstName", is("Jens"))
                .body("email", is("jens@example.com"));
    }

    @Test
    void updateUser_changesArePersisted() {
        Integer id = seed.get("user1").getId();

        update(id, "newname", "Jens", "Hansen", "jens@example.com", ownerToken).statusCode(200);

        given()
                .when().get(USERS + "/{id}", id)
                .then().statusCode(200)
                .body("username", is("newname"))
                .body("firstName", is("Jens"));
    }

    @Test
    void updateUser_keepOwnUsernameAndEmail_returns200() {
        User user1 = seed.get("user1");

        update(user1.getId(), user1.getUsername(), "Morten", "Helander", user1.getEmail(), ownerToken)
                .statusCode(200);
    }

    @Test
    void updateUser_usernameTakenByOther_returns409() {
        Integer id = seed.get("user1").getId();
        String takenUsername = seed.get("user3").getUsername(); // "frontjoe"

        update(id, takenUsername, "Morten", "Helander", "morten@hotmail.com", ownerToken)
                .statusCode(409)
                .body("error", is("Username is taken"));
    }

    @Test
    void updateUser_emailTakenByOther_returns409() {
        Integer id = seed.get("user1").getId();
        String takenEmail = seed.get("user2").getEmail();

        update(id, "sheriffen", "Morten", "Helander", takenEmail, ownerToken)
                .statusCode(409)
                .body("error", is("Email is taken"));
    }

    @Test
    void updateUser_notExisting_returns404() {
        update(9999, "newname", "Jens", "Hansen", "jens@example.com", ownerToken)
                .statusCode(404)
                .body("error", is("User with id 9999 not found"));
    }

    @Test
    void updateUser_invalidFirstName_returns400() {
        Integer id = seed.get("user1").getId();

        update(id, "sheriffen", "M0rten", "Helander", "morten@hotmail.com", ownerToken)
                .statusCode(400)
                .body("error", is("First name must consist of only letters"));
    }

    @Test
    void updateUser_emptyLastName_returns400() {
        Integer id = seed.get("user1").getId();

        update(id, "sheriffen", "Morten", "", "morten@hotmail.com", ownerToken)
                .statusCode(400)
                .body("error", is("Last name is empty"));
    }

    @Test
    void updateUser_invalidUsername_returns400() {
        Integer id = seed.get("user1").getId();

        update(id, "ab", "Morten", "Helander", "morten@hotmail.com", ownerToken)
                .statusCode(400)
                .body("error", is("Username must be between 3 and 16 characters long"));
    }

    @Test
    void updateUser_multipleErrors_allMessagesJoinedInOrder_returns400() {
        Integer id = seed.get("user1").getId();

        // order matches the controller: username, first name, last name, email
        update(id, "", "1", "Helander", "not-an-email", ownerToken)
                .statusCode(400)
                .body("error", is("Username is empty, "
                        + "First name must be between 2 and 50 characters long, "
                        + "First name must consist of only letters, "
                        + "Email must consist of: (your-email)@(email-provider).(address)"));
    }

    @Test
    void updateUser_invalidData_isNotPersisted() {
        Integer id = seed.get("user1").getId();

        update(id, "newname", "M0rten", "Helander", "morten@hotmail.com", ownerToken).statusCode(400);

        given()
                .when().get(USERS + "/{id}", id)
                .then().statusCode(200)
                .body("username", is("sheriffen"));
    }

    @Test
    void updateUser_otherUsersProfile_returns403_andIsNotPersisted() {
        Integer id = seed.get("user1").getId();
        String otherUsersToken = tokenFor(seed.get("user2").getUsername()); // "svg-man" tries to edit "sheriffen"

        update(id, "hacked", "Jens", "Hansen", "jens@example.com", otherUsersToken)
                .statusCode(403)
                .body("error", is("You can only update your own profile"));

        given()
                .when().get(USERS + "/{id}", id)
                .then().statusCode(200)
                .body("username", is("sheriffen"));
    }

    @Test
    void updateUser_noToken_returns401() {
        Integer id = seed.get("user1").getId();

        update(id, "newname", "Jens", "Hansen", "jens@example.com", null)
                .statusCode(401)
                .body("error", is("Unauthorized"));
    }

    @Test
    void updateUser_adminToken_returns200() {
        Integer id = seed.get("user1").getId();

        update(id, "newname", "Jens", "Hansen", "jens@example.com", adminToken())
                .statusCode(200);
    }

    // DELETE
    @Test
    void deleteUser_adminToken_returns204_andUserIsGone() {
        Integer id = seed.get("user1").getId();

        given().header(AUTH, bearer(adminToken))
                .when().delete(USERS + "/{id}", id)
                .then().statusCode(204)
                .body(is(""));

        given()
                .when().get(USERS + "/{id}", id)
                .then().statusCode(404);
    }

    @Test
    void deleteUser_userToken_returns403_andUserStillExists() {
        Integer id = seed.get("user1").getId();

        given().header(AUTH, bearer(userToken))
                .when().delete(USERS + "/{id}", id)
                .then().statusCode(403)
                .body("error", is("Forbidden"));

        given()
                .when().get(USERS + "/{id}", id)
                .then().statusCode(200);
    }

    @Test
    void deleteUser_noToken_returns401() {
        given()
                .when().delete(USERS + "/{id}", seed.get("user1").getId())
                .then().statusCode(401);
    }

    @Test
    void deleteUser_notExisting_returns404() {
        given().header(AUTH, bearer(adminToken))
                .when().delete(USERS + "/{id}", 9999)
                .then().statusCode(404)
                .body("error", is("User with id 9999 not found"));
    }

    @Test
    void deleteUser_negativeId_returns400() {
        given().header(AUTH, bearer(adminToken))
                .when().delete(USERS + "/{id}", -1)
                .then().statusCode(400)
                .body("error", is("ID must be positive"));
    }

    // HELPERS
    private ValidatableResponse update(int id, String username, String firstName, String lastName,
                                       String email, String token) {
        Map<String, String> body = new HashMap<>();
        body.put("username", username);
        body.put("firstName", firstName);
        body.put("lastName", lastName);
        body.put("email", email);

        var request = given().contentType(ContentType.JSON).body(body);
        if (token != null) {
            request.header(AUTH, bearer(token));
        }
        return request.when().put(USERS + "/{id}", id)
                .then().log().all();
    }
}