package app.daos;

import app.config.HibernateTestConfig;
import app.entities.User;
import app.exceptions.ApiException;
import app.testutils.UserTestPopulator;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertThrows;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserDAOTest {


    private final EntityManagerFactory emf = HibernateTestConfig.getEntityManagerFactory();

    private UserDAO userDAO;
    private Map<String, User> seeded;

    @BeforeEach
    void beforeEach() {
        seeded = UserTestPopulator.populate(emf);
        userDAO = new UserDAO(emf);
    }

    @Test
    void create() {
        User user = new User("Simon", "Hansen", "simon@mail.dk", "simonsen", "simon123");

        User created = userDAO.create(user);

        assertThat(created.getId(), notNullValue());
        User fetched = userDAO.getById(created.getId());
        assertThat(fetched, is(created));
    }

    @Test
    void createWithExistingUsername_shouldReturn409() {
        User user = new User("sheriffen", "simonhansen@hotmail.com", "Simon", "Jensen", "simon123");

        User existing = seeded.get("user1");
        assertThat(user.getUsername(), is(existing.getUsername())); //same username

        ApiException ex = assertThrows(ApiException.class, () -> userDAO.create(user));
        assertThat(ex.getCode(), is(409));
        assertThat(ex.getMessage(), is("Username is taken"));
    }

    @Test
    void createWithExistingEmail_shouldReturn409() {
        User user = new User("simonsen", "morten@hotmail.com", "Simon", "Jensen", "simon123");

        User existing = seeded.get("user1");

        assertThat(user.getEmail(), is(existing.getEmail())); //same email

        ApiException ex = assertThrows(ApiException.class, () -> userDAO.create(user));
        assertThat(ex.getCode(), is(409));
        assertThat(ex.getMessage(), is("Email is taken"));
    }


    @Test
    void getById() {
        User seed = seeded.get("user1");
        User fetched = userDAO.getById(seed.getId());
        assertThat(fetched.getId(), is(seed.getId()));
        assertThat(fetched.getEmail(), is(seed.getEmail()));
    }

    @Test
    void getByUsername(){
        User seed = seeded.get("user1");
        User fetched = userDAO.getByUsername(seed.getUsername());
        assertThat(fetched.getId(), is(seed.getId()));
        assertThat(fetched.getUsername(), is(fetched.getUsername()));
    }

    @Test
    void getByUsername_noMatch_returnsNull(){
        User fetched = userDAO.getByUsername("not-existing");

        assertThat(fetched, is(nullValue()));
    }

    @Test
    void getAll() {
        List<User> all = userDAO.getAll();
        assertThat(all, hasSize(3));
        assertThat(all, containsInAnyOrder(seeded.get("user1"), seeded.get("user2"), seeded.get("user3")));
    }

    @Test
    void update() {
        User seed = seeded.get("user2");
        User persisted = userDAO.getById(seed.getId());
        persisted.updateDetails("updated-username", persisted.getEmail(), persisted.getFirstName(), persisted.getLastName());

        User updated = userDAO.update(persisted);

        assertThat(updated.getId(), is(seed.getId()));
        assertThat(updated.getUsername(), is("updated-username"));
        assertThat(updated.getEmail(), is(seed.getEmail()));
        assertThat(updated.getFirstName(), is(seed.getFirstName()));
        assertThat(updated.getLastName(), is(seed.getLastName()));
    }

    @Test
    void delete() {
        User seed = seeded.get("user3");

        boolean deleted = userDAO.delete(seed.getId());

        assertThat(deleted, is(true));
        assertThrows(ApiException.class, () -> userDAO.getById(seed.getId()));
    }

    @Test
    void create_withNullUser_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> userDAO.create(null));
        assertThat(ex.getCode(), is(400));
    }

    @Test
    void getById_withNullId_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> userDAO.getById(null));
        assertThat(ex.getCode(), is(400));
    }

    @Test
    void getById_withMissingId_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> userDAO.getById(999_999));
        assertThat(ex.getCode(), is(404));
    }

    @Test
    void update_withNullUser_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> userDAO.update(null));
        assertThat(ex.getCode(), is(400));
    }

    @Test
    void update_withMissingId_throwsApiException() {
        User missing = User.builder()
                .id(999_999)
                .firstName("Missing")
                .lastName("Missing")
                .email("Missing")
                .password("Missing")
                .username("Missing")
                .build();

        ApiException ex = assertThrows(ApiException.class, () -> userDAO.update(missing));
        assertThat(ex.getCode(), is(404));
    }

    @Test
    void delete_withNullId_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> userDAO.delete(null));
        assertThat(ex.getCode(), is(400));
    }

    @Test
    void delete_withMissingId_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> userDAO.delete(999_999));
        assertThat(ex.getCode(), is(404));
    }
}
