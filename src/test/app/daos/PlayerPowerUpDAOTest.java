package app.daos;

import app.config.HibernateTestConfig;
import app.entities.Game;
import app.entities.Move;
import app.entities.Player;
import app.entities.PlayerPowerUp;
import app.entities.enums.PowerUpStatus;
import app.entities.enums.PowerUpType;
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
import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PlayerPowerUpDAOTest {

    private final EntityManagerFactory emf = HibernateTestConfig.getEntityManagerFactory();

    private PlayerPowerUpDAO playerPowerUpDAO;
    private Map<String, Game> seeded;
    private List<PlayerPowerUp> powerUps;

    @BeforeEach
    void beforeEach(){
        seeded = GameTestPopulator.populate(emf);
        Game game = seeded.get("game1");
        List<Player> players = game.getPlayers().stream().toList();
        Player player1 = players.getFirst();
        powerUps = player1.getPlayerPowerUps().stream().toList();
        playerPowerUpDAO = new PlayerPowerUpDAO(emf);
    }

    @AfterAll
    void shutdown() {
        emf.close();
    }

    @Test
    void create() {
        PlayerPowerUp actual = new PlayerPowerUp(PowerUpType.EVIL_AI, PowerUpStatus.AUTOMATICALLY_APPLIED);

        PlayerPowerUp created = playerPowerUpDAO.create(actual);
        assertThat(created.getId(), notNullValue());
        assertThat(created.getStatus(), is(actual.getStatus()));
        assertThat(created.getType(), is(actual.getType()));
    }

    @Test
    void getById() {
        PlayerPowerUp seed = powerUps.getFirst();

        PlayerPowerUp fetched = playerPowerUpDAO.getById(seed.getId());
        assertThat(fetched, samePropertyValuesAs(seed));
    }

    @Test
    void update() {
        PlayerPowerUp seed = powerUps.getFirst();

        PlayerPowerUp updated = playerPowerUpDAO.getById(seed.getId());
        updated.updateStatus();

        PlayerPowerUp result = playerPowerUpDAO.update(updated);

        assertThat(result.getId(), is(seed.getId()));
        assertThat(result.getStatus(), not(seed.getStatus()));
        assertThat(result.getType(), is(seed.getType()));
    }

    @Test
    void getAllPowerUpsByGameId() {
        List<PlayerPowerUp> expected = powerUps;

        List<PlayerPowerUp> fetched = playerPowerUpDAO.getAllPowerUpsByGameId(1);

        assertThat(fetched, hasSize(3));
        assertThat(fetched, containsInAnyOrder(
                expected.get(0),
                expected.get(1),
                expected.get(2)
        ));
    }
}