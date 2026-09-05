package app.daos;

import app.config.HibernateTestConfig;
import app.entities.*;
import app.entities.enums.Color;
import app.entities.enums.GameMode;
import app.exceptions.ApiException;
import app.gameengine.Position;
import app.testutils.GameTestPopulator;
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
public class GameDAOTest {

    private final EntityManagerFactory emf = HibernateTestConfig.getEntityManagerFactory();

    private GameDAO gameDAO;
    private Map<String, Game> seeded;

    @BeforeEach
    void beforeEach(){
        seeded = GameTestPopulator.populate(emf);
        gameDAO = new GameDAO(emf);
    }

    @AfterAll
    void shutdown() {
        emf.close();
    }

    @Test
    void create() {
        Game game = Game.newGame(GameMode.FUN, new Player(Color.WHITE, false), new Player(Color.BLACK, false));

        Game created = gameDAO.create(game);

        assertThat(created.getId(), notNullValue());
        Game fetched = gameDAO.getById(created.getId());
        assertThat(fetched.getId(), is(created.getId()));
    }

    @Test
    void getById() {
        Game seed = seeded.get("game1");
        Game fetched = gameDAO.getById(seed.getId());
        assertThat(fetched.getId(), is(seed.getId()));
        assertThat(fetched.getGameMode(), is(seed.getGameMode()));
    }

    @Test
    void getAll() {
        List<Game> all = gameDAO.getAll();
        assertThat(all, hasSize(3));
        assertThat(all, containsInAnyOrder(seeded.get("game1"), seeded.get("game2"), seeded.get("game3")));
    }

    @Test
    void update() {
        Game seed = seeded.get("game2");
        Game updated = gameDAO.getById(seed.getId());
        updated.addMoveAndShiftTurn(new Move(Position.A1, Position.A3));

        Game result = gameDAO.update(updated);

        assertThat(result.getId(), is(seed.getId()));
        assertThat(result.isWhitesTurn(), is(false));
        assertThat(result.getMoves().size(), is(1));
    }

    @Test
    void delete() {
        Game seed = seeded.get("game3");

        boolean deleted = gameDAO.delete(seed.getId());

        assertThat(deleted, is(true));
        assertThrows(ApiException.class, () -> gameDAO.getById(seed.getId()));
    }

    @Test
    void create_withNullGame_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> gameDAO.create(null));
        assertThat(ex.getCode(), is(400));
    }

    @Test
    void getById_withNullId_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> gameDAO.getById(null));
        assertThat(ex.getCode(), is(400));
    }

    @Test
    void getById_withMissingId_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> gameDAO.getById(999_999));
        assertThat(ex.getCode(), is(404));
    }

    @Test
    void update_withNullGame_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> gameDAO.update(null));
        assertThat(ex.getCode(), is(400));
    }


    @Test
    void delete_withNullId_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> gameDAO.delete(null));
        assertThat(ex.getCode(), is(400));
    }

    @Test
    void delete_withMissingId_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> gameDAO.delete(999_999));
        assertThat(ex.getCode(), is(404));
    }
}
