package app.daos;

import app.config.HibernateTestConfig;
import app.entities.Result;
import app.entities.User;
import app.entities.UserStats;
import app.exceptions.ApiException;
import app.testutils.UserTestPopulator;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserStatsDAOTest {

    private final EntityManagerFactory emf = HibernateTestConfig.getEntityManagerFactory();

    private UserStatsDAO userStatsDAO;
    private Map<String, User> seeded;

    @BeforeEach
    void beforeEach(){
        seeded = UserTestPopulator.populate(emf);
        userStatsDAO = new UserStatsDAO(emf);
    }

    @AfterAll
    void shutdown() {
        emf.close();
    }

    @Test
    void getById(){
        User seed = seeded.get("user1");
        UserStats fetched = userStatsDAO.getById(seed.getId());
        assertThat(fetched.getId(), is(seed.getId()));
        assertThat(fetched.getWins(), is(seed.getUserStats().getWins()));
    }

    @Test
    void getAll() {
        List<UserStats> all = userStatsDAO.getAll();
        assertThat(all, hasSize(3));
        assertThat(all, containsInAnyOrder(seeded.get("user1").getUserStats(), seeded.get("user2").getUserStats(), seeded.get("user3").getUserStats()));
    }

    @Test
    void recordResult() {
        UserStats seed = seeded.get("user1").getUserStats();

        UserStats updated = userStatsDAO.recordResult(seed.getId(), Result.WIN);

        assertThat(seed.getWins(), is(updated.getWins()-1));
        assertThat(seed.getGamesPlayed(), is(updated.getGamesPlayed()-1));
        assertThat(seed.getLosses(), is(updated.getLosses()));
        assertThat(seed.getDraws(), is(updated.getDraws()));

    }

    @Test
    void getById_withNullId_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> userStatsDAO.getById(null));
        assertThat(ex.getCode(), is(400));
    }

    @Test
    void getById_withMissingId_throwsApiException() {
        ApiException ex = assertThrows(ApiException.class, () -> userStatsDAO.getById(999_999));
        assertThat(ex.getCode(), is(404));
    }

    @Test
    void recordResult_withNullResult_throwsNullPointerException() {
        NullPointerException ex = assertThrows(NullPointerException.class, () -> userStatsDAO.recordResult(seeded.get("user1").getId(), null));
        assertThat(ex.getMessage(), is("Provided result is null"));
    }

    @Test
    void recordResult_withMissingId_throwsApiException() {
        Integer missing = 99999;

        ApiException ex = assertThrows(ApiException.class, () -> userStatsDAO.recordResult(missing, Result.WIN));
        assertThat(ex.getCode(), is(404));
    }
}
