package app.daos;

import app.config.HibernateTestConfig;
import app.entities.Game;
import app.entities.Move;
import app.gameengine.Position;
import app.testutils.GameTestPopulator;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MoveDAOTest {

    private final EntityManagerFactory emf = HibernateTestConfig.getEntityManagerFactory();

    private MoveDAO moveDAO;
    private Map<String, Game> gameSeeded;

    @BeforeEach
    void beforeEach(){
        gameSeeded = GameTestPopulator.populate(emf);
        moveDAO = new MoveDAO(emf);

    }

    @AfterAll
    void shutdown() {
        emf.close();
    }

    @Test
    void create() {

        Move move = new Move(Position.D7, Position.D6);

        Move created = moveDAO.create(move);
        assertThat(created.getId(), notNullValue());
        assertThat(created.getFrom(), is(Position.D7));
        assertThat(created.getTo(), is(Position.D6));
    }

    @Test
    void getById() {
        Game game = gameSeeded.get("game1");
        Move seed = game.getMoves().getFirst();

        Move fetched = moveDAO.getById(seed.getId());
        assertThat(fetched, samePropertyValuesAs(seed));
    }

    @Test
    void getAllMovesByGameId() {

    }
}