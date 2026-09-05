package app.daos;

import app.config.HibernateTestConfig;
import app.entities.enums.Color;
import app.entities.Player;
import app.exceptions.ApiException;
import app.testutils.PlayerTestPopulator;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PlayerDAOTest {

    private final EntityManagerFactory emf = HibernateTestConfig.getEntityManagerFactory();

    private PlayerDAO playerDAO;
    private Map<String, Player> seeded;

    @BeforeEach
    void beforeEach(){
        seeded = PlayerTestPopulator.populate(emf);
        playerDAO = new PlayerDAO(emf);
    }

    @AfterAll
    void shutdown() {
        emf.close();
    }

    @Test
    void create() {
        Player player = new Player(Color.WHITE, false);

        Player created = playerDAO.create(player);

        assertThat(created.getId(), notNullValue());
        Player fetched = playerDAO.getById(created.getId());
        assertThat(fetched.getId(), is(created.getId()));
    }

    @Test
    void getById() {
        Player seed = seeded.get("player1");
        Player fetched = playerDAO.getById(seed.getId());
        assertThat(fetched.getId(), is(seed.getId()));
        assertThat(fetched.getColor(), is(seed.getColor()));
    }

    @Test
    void getAll() {
        List<Player> all = playerDAO.getAll();
        assertThat(all, hasSize(3));
        assertThat(all, containsInAnyOrder(seeded.get("player1"), seeded.get("player2"), seeded.get("player3")));
    }

    @Test
    void update() {
        Player seed = seeded.get("player1");
        Player updated = Player.builder()
                .id(seed.getId())
                .isAi(true)
                .build();

        Player result = playerDAO.update(updated);

        assertThat(result.getId(), is(seed.getId()));
        assertThat(result.isAi(), is(true));
    }

    @Test
    void delete() {
        Player seed = seeded.get("player3");

        boolean deleted = playerDAO.delete(seed.getId());

        assertThat(deleted, is(true));
        assertThrows(ApiException.class, () -> playerDAO.getById(seed.getId()));
    }

    @Test
    void create_withNullPlayer_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> playerDAO.create(null));
        assertThat(ex.getCode(), is(400));
    }

    @Test
    void getById_withNullId_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> playerDAO.getById(null));
        assertThat(ex.getCode(), is(400));
    }

    @Test
    void getById_withMissingId_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> playerDAO.getById(999_999));
        assertThat(ex.getCode(), is(404));
    }

    @Test
    void update_withNullPlayer_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> playerDAO.update(null));
        assertThat(ex.getCode(), is(400));
    }

    @Test
    void update_withMissingId_throwsApiException() {
        Player missing = Player.builder()
                .id(999_999)
                .isAi(false)
                .build();

        ApiException ex = assertThrows(ApiException.class, () -> playerDAO.update(missing));
        assertThat(ex.getCode(), is(404));
    }

    @Test
    void delete_withNullId_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> playerDAO.delete(null));
        assertThat(ex.getCode(), is(400));
    }

    @Test
    void delete_withMissingId_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> playerDAO.delete(999_999));
        assertThat(ex.getCode(), is(404));
    }
}
