package app.daos;

import app.config.HibernateTestConfig;
import app.entities.DailyPuzzle;
import app.entities.DailyPuzzleAttempt;
import app.entities.User;
import app.entities.enums.PuzzleStatus;
import app.exceptions.ApiException;
import app.exceptions.DuplicatePuzzleException;
import app.testutils.DailyPuzzleAttemptTestPopulator;
import app.testutils.DailyPuzzleTestPopulator;
import app.testutils.Seeded;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DailyPuzzleAttemptDAOTest {

    private final EntityManagerFactory emf = HibernateTestConfig.getEntityManagerFactory();

    private DailyPuzzleAttemptDAO dailyPuzzleDao;
    private Map<String, User> seededUsers;
    private Map<String, DailyPuzzle> seededPuzzles;
    private Map<String, DailyPuzzleAttempt> seededAttempts;


    @BeforeEach
    void beforeEach(){
        Seeded seeded = DailyPuzzleAttemptTestPopulator.populate(emf);
        seededUsers = seeded.users();
        seededPuzzles = seeded.puzzles();
        seededAttempts = seeded.attempts();
        dailyPuzzleDao = new DailyPuzzleAttemptDAO(emf);
    }


    // ---------- create ----------

    @Test
    void create(){
        // user3 has no attempts yet, so this doesn't collide with the unique constraint
        User user = seededUsers.get("user3");
        DailyPuzzle puzzle = seededPuzzles.get("whiteToMove");
        DailyPuzzleAttempt dailyPuzzleAttempt = DailyPuzzleAttempt.startNewPuzzle(puzzle, user);

        DailyPuzzleAttempt created = dailyPuzzleDao.create(dailyPuzzleAttempt);
        Optional<DailyPuzzleAttempt> fetched = dailyPuzzleDao.getByUserIdAndPuzzleDate(user.getId(), puzzle.getId());

        assertThat(created.getId(), is(notNullValue()));
        assertThat(fetched.isPresent(), is(true));
        assertThat(fetched.get().getId(), is(created.getId()));
        assertThat(fetched.get().getStatus(), is(PuzzleStatus.IN_PROGRESS));
        assertThat(fetched.get().getMoveIndex(), is(0));
        assertThat(fetched.get().getAttemptsLeft(), is(10));
        assertThat(fetched.get().getStartedAt(), is(notNullValue()));
        assertThat(fetched.get().getFinishedAt(), is(nullValue()));
    }

    @Test
    void create_nullAttempt_throws400(){
        ApiException ex = assertThrows(ApiException.class, () -> dailyPuzzleDao.create(null));
        assertThat(ex.getCode(), is(400));
    }

    @Test
    void create_userAlreadyHasAttemptOnPuzzle_throwsDuplicatePuzzleException(){
        // user1 already has the "inProgress" attempt on the black puzzle
        DailyPuzzleAttempt duplicate = DailyPuzzleAttempt.startNewPuzzle(seededPuzzles.get("blackToMove"), seededUsers.get("user1"));

        DuplicatePuzzleException ex = assertThrows(DuplicatePuzzleException.class, () -> dailyPuzzleDao.create(duplicate));
        assertThat(ex.getCode(), is(409));

        // the original attempt is untouched
        DailyPuzzleAttempt original = dailyPuzzleDao.getByUserIdAndPuzzleDate(
                seededUsers.get("user1").getId(), DailyPuzzleTestPopulator.BLACK_PUZZLE_DATE).get();
        assertThat(original.getId(), is(seededAttempts.get("inProgress").getId()));
        assertThat(original.getMoveIndex(), is(2));
    }

    @Test
    void create_sameUserDifferentPuzzle_isAllowed(){
        // user2 only has an attempt on the white puzzle, so the black one is free
        DailyPuzzleAttempt attempt = DailyPuzzleAttempt.startNewPuzzle(seededPuzzles.get("blackToMove"), seededUsers.get("user2"));

        DailyPuzzleAttempt created = dailyPuzzleDao.create(attempt);

        assertThat(created.getId(), is(notNullValue()));
        assertThat(dailyPuzzleDao.getAllPuzzlesByUserId(seededUsers.get("user2").getId()), hasSize(2));
    }


    // ---------- getByUserIdAndPuzzleDate ----------

    @Test
    void getByUserIdAndPuzzleDate(){
        DailyPuzzleAttempt seed = seededAttempts.get("inProgress");

        Optional<DailyPuzzleAttempt> fetched = dailyPuzzleDao.getByUserIdAndPuzzleDate(
                seededUsers.get("user1").getId(), DailyPuzzleTestPopulator.BLACK_PUZZLE_DATE);

        assertThat(fetched.isPresent(), is(true));
        assertThat(fetched.get().getId(), is(seed.getId()));
        assertThat(fetched.get().getStatus(), is(PuzzleStatus.IN_PROGRESS));
        assertThat(fetched.get().getMoveIndex(), is(2));
        assertThat(fetched.get().getAttemptsLeft(), is(9));
        // the EAGER relations are loaded, so these work after the EntityManager is closed
        assertThat(fetched.get().getUser().getUsername(), is("sheriffen"));
        assertThat(fetched.get().getDailyPuzzle().getSolution(), hasSize(9));
    }

    @Test
    void getByUserIdAndPuzzleDate_solvedAttempt_isStoredCorrectly(){
        Optional<DailyPuzzleAttempt> fetched = dailyPuzzleDao.getByUserIdAndPuzzleDate(
                seededUsers.get("user1").getId(), DailyPuzzleTestPopulator.WHITE_PUZZLE_DATE);

        assertThat(fetched.isPresent(), is(true));
        assertThat(fetched.get().getStatus(), is(PuzzleStatus.SOLVED));
        assertThat(fetched.get().getMoveIndex(), is(3)); // markAsSolved sets it to the solution size
        assertThat(fetched.get().getFinishedAt(), is(notNullValue()));
    }

    @Test
    void getByUserIdAndPuzzleDate_failedAttempt_isStoredCorrectly(){
        Optional<DailyPuzzleAttempt> fetched = dailyPuzzleDao.getByUserIdAndPuzzleDate(
                seededUsers.get("user2").getId(), DailyPuzzleTestPopulator.WHITE_PUZZLE_DATE);

        assertThat(fetched.isPresent(), is(true));
        assertThat(fetched.get().getStatus(), is(PuzzleStatus.FAILED));
        assertThat(fetched.get().getAttemptsLeft(), is(0));
        assertThat(fetched.get().getFinishedAt(), is(notNullValue()));
    }

    @Test
    void getByUserIdAndPuzzleDate_userWithoutAttempt_returnsEmptyOptional(){
        Optional<DailyPuzzleAttempt> shouldBeEmpty = dailyPuzzleDao.getByUserIdAndPuzzleDate(
                seededUsers.get("user3").getId(), DailyPuzzleTestPopulator.WHITE_PUZZLE_DATE);

        assertThat(shouldBeEmpty, is(Optional.empty()));
    }

    @Test
    void getByUserIdAndPuzzleDate_dateWithoutPuzzle_returnsEmptyOptional(){
        Optional<DailyPuzzleAttempt> shouldBeEmpty = dailyPuzzleDao.getByUserIdAndPuzzleDate(
                seededUsers.get("user1").getId(), DailyPuzzleTestPopulator.NO_PUZZLE_DATE);

        assertThat(shouldBeEmpty, is(Optional.empty()));
    }

    @Test
    void getByUserIdAndPuzzleDate_nullUserId_throws400(){
        ApiException ex = assertThrows(ApiException.class,
                () -> dailyPuzzleDao.getByUserIdAndPuzzleDate(null, DailyPuzzleTestPopulator.WHITE_PUZZLE_DATE));
        assertThat(ex.getCode(), is(400));
    }

    @Test
    void getByUserIdAndPuzzleDate_nullDate_throws400(){
        ApiException ex = assertThrows(ApiException.class,
                () -> dailyPuzzleDao.getByUserIdAndPuzzleDate(seededUsers.get("user1").getId(), null));
        assertThat(ex.getCode(), is(400));
    }


    // ---------- getAllPuzzlesByUserId ----------

    @Test
    void getAllPuzzlesByUserId(){
        List<DailyPuzzleAttempt> attempts = dailyPuzzleDao.getAllPuzzlesByUserId(seededUsers.get("user1").getId());

        assertThat(attempts, hasSize(2));
        assertThat(attempts.stream().map(DailyPuzzleAttempt::getId).toList(),
                containsInAnyOrder(seededAttempts.get("inProgress").getId(), seededAttempts.get("solved").getId()));
    }

    @Test
    void getAllPuzzlesByUserId_userWithoutAttempts_returnsEmptyList(){
        List<DailyPuzzleAttempt> attempts = dailyPuzzleDao.getAllPuzzlesByUserId(seededUsers.get("user3").getId());

        assertThat(attempts, is(empty()));
    }

    @Test
    void getAllPuzzlesByUserId_nullUserId_throws400(){
        ApiException ex = assertThrows(ApiException.class, () -> dailyPuzzleDao.getAllPuzzlesByUserId(null));
        assertThat(ex.getCode(), is(400));
    }


    // ---------- update ----------

    @Test
    void update(){
        DailyPuzzleAttempt attempt = dailyPuzzleDao.getByUserIdAndPuzzleDate(
                seededUsers.get("user1").getId(), DailyPuzzleTestPopulator.BLACK_PUZZLE_DATE).get();
        long versionBefore = attempt.getVersion();

        attempt.advance();
        dailyPuzzleDao.update(attempt);

        DailyPuzzleAttempt fetched = dailyPuzzleDao.getByUserIdAndPuzzleDate(
                seededUsers.get("user1").getId(), DailyPuzzleTestPopulator.BLACK_PUZZLE_DATE).get();
        assertThat(fetched.getMoveIndex(), is(4));
        assertThat(fetched.getVersion(), is(versionBefore + 1));
    }

    @Test
    void update_markAsSolved_isSaved(){
        DailyPuzzleAttempt attempt = dailyPuzzleDao.getByUserIdAndPuzzleDate(
                seededUsers.get("user1").getId(), DailyPuzzleTestPopulator.BLACK_PUZZLE_DATE).get();

        attempt.markAsSolved();
        dailyPuzzleDao.update(attempt);

        DailyPuzzleAttempt fetched = dailyPuzzleDao.getByUserIdAndPuzzleDate(
                seededUsers.get("user1").getId(), DailyPuzzleTestPopulator.BLACK_PUZZLE_DATE).get();
        assertThat(fetched.getStatus(), is(PuzzleStatus.SOLVED));
        assertThat(fetched.getMoveIndex(), is(9));
        assertThat(fetched.getFinishedAt(), is(notNullValue()));
    }

    @Test
    void update_nullAttempt_throws400(){
        ApiException ex = assertThrows(ApiException.class, () -> dailyPuzzleDao.update(null));
        assertThat(ex.getCode(), is(400));
    }

    @Test
    void update_attemptWithoutId_throws400(){
        // never saved, so it has no id
        DailyPuzzleAttempt notSaved = DailyPuzzleAttempt.startNewPuzzle(seededPuzzles.get("blackToMove"), seededUsers.get("user3"));

        ApiException ex = assertThrows(ApiException.class, () -> dailyPuzzleDao.update(notSaved));
        assertThat(ex.getCode(), is(400));
    }

    @Test
    void update_staleVersion_throwsOptimisticLockException(){
        // two requests load the same attempt at the same time
        Integer userId = seededUsers.get("user1").getId();
        DailyPuzzleAttempt firstRequest = dailyPuzzleDao.getByUserIdAndPuzzleDate(userId, DailyPuzzleTestPopulator.BLACK_PUZZLE_DATE).get();
        DailyPuzzleAttempt secondRequest = dailyPuzzleDao.getByUserIdAndPuzzleDate(userId, DailyPuzzleTestPopulator.BLACK_PUZZLE_DATE).get();

        // the first one saves -> version goes up
        firstRequest.advance();
        dailyPuzzleDao.update(firstRequest);

        // the second one still has the old version -> rejected
        secondRequest.advance();
        assertThrows(OptimisticLockException.class, () -> dailyPuzzleDao.update(secondRequest));

        // only the first request's change is saved (2 -> 4, not 6)
        DailyPuzzleAttempt fetched = dailyPuzzleDao.getByUserIdAndPuzzleDate(userId, DailyPuzzleTestPopulator.BLACK_PUZZLE_DATE).get();
        assertThat(fetched.getMoveIndex(), is(4));
    }
}