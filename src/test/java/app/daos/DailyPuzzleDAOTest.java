package app.daos;

import app.config.HibernateTestConfig;
import app.entities.DailyPuzzle;
import app.exceptions.DuplicatePuzzleException;
import app.testutils.DailyPuzzleTestPopulator;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class DailyPuzzleDAOTest {

    private final EntityManagerFactory emf = HibernateTestConfig.getEntityManagerFactory();

    private DailyPuzzleDAO dailyPuzzleDao;
    private Map<String, DailyPuzzle> seeded;

    @BeforeEach
    void beforeEach(){
        seeded = DailyPuzzleTestPopulator.populate(emf);
        dailyPuzzleDao = new DailyPuzzleDAO(emf);
    }

    @Test
    void create(){
        DailyPuzzle dailyPuzzle = new DailyPuzzle(LocalDate.of(2025, 1, 31), "puzzleId", 2500, List.of("g4h5", "g6h5", "d7g4"),
                "8/3Q4/5qkp/3p4/1p2n1B1/1Pr4P/P1P2bP1/2R2K2 w - - 1 1", "a1c1");

        dailyPuzzleDao.create(dailyPuzzle);

        Optional<DailyPuzzle> created = dailyPuzzleDao.getByDate(dailyPuzzle.getId());

        assertThat(created.isPresent(), is(true));
        assertThat(created.get().getId(), is(LocalDate.of(2025, 1, 31)));
        assertThat(created.get().getLichessPuzzleId(), is("puzzleId"));
        assertThat(created.get().getRating(), is(2500));
        assertThat(created.get().getSolution(), contains("g4h5", "g6h5", "d7g4"));
        assertThat(created.get().getFen(), is("8/3Q4/5qkp/3p4/1p2n1B1/1Pr4P/P1P2bP1/2R2K2 w - - 1 1"));
        assertThat(created.get().getLastMove(), is("a1c1"));
    }

    @Test
    void getByDate(){
        LocalDate whitePuzzleDate = DailyPuzzleTestPopulator.WHITE_PUZZLE_DATE;

        Optional<DailyPuzzle> fetched = dailyPuzzleDao.getByDate(whitePuzzleDate);

        assertThat(fetched.isPresent(), is(true));
        assertThat(fetched.get().getId(), is(whitePuzzleDate));
        assertThat(fetched.get().getFen(), is("8/3Q4/5qkp/3p4/1p2n1B1/1Pr4P/P1P2bP1/2R2K2 w - - 1 1"));
    }

    @Test
    void getAll(){
        List<DailyPuzzle> dailyPuzzles = dailyPuzzleDao.getAll();

        assertThat(dailyPuzzles.size(), is(2));
        assertThat(dailyPuzzles, containsInAnyOrder(seeded.get("whiteToMove"), seeded.get("blackToMove")));
    }

    @Test
    void delete() {
        DailyPuzzle seed = seeded.get("whiteToMove");
        boolean deleted = dailyPuzzleDao.delete(seed.getId());

        Optional<DailyPuzzle> shouldBeEmpty = dailyPuzzleDao.getByDate(seed.getId());

        assertThat(deleted, is(true));
        assertThat(shouldBeEmpty, is(Optional.empty()));
    }

    @Test
    void createDailyPuzzle_sameDayTwice_throwsDuplicatePuzzleException(){
        DailyPuzzle dailyPuzzle = new DailyPuzzle(LocalDate.of(2025, 1, 31), "puzzleId", 2500, List.of("g4h5", "g6h5", "d7g4"),
                "8/3Q4/5qkp/3p4/1p2n1B1/1Pr4P/P1P2bP1/2R2K2 w - - 1 1", "a1c1");
        DailyPuzzle created = dailyPuzzleDao.create(dailyPuzzle);

        assertThat(created.getId(), is(LocalDate.of(2025, 1, 31)));

        //the daily puzzle create is called again same day
        DuplicatePuzzleException ex = assertThrows(DuplicatePuzzleException.class, () -> dailyPuzzleDao.create(dailyPuzzle));
        assertThat(ex.getCode(), is(409));
        assertThat(ex.getMessage(), is("Daily puzzle for " + dailyPuzzle.getId() + " already exists"));

        Optional<DailyPuzzle> fetched = dailyPuzzleDao.getByDate(dailyPuzzle.getId());
        assertThat(fetched.isPresent(), is(true));
        assertThat(fetched.get().getId(), is(dailyPuzzle.getId()));
    }

    @Test
    void getByDate_noPuzzleForThatDay_returnsEmptyOptional(){
        Optional<DailyPuzzle> shouldBeEmpty = dailyPuzzleDao.getByDate(LocalDate.of(2021, 3, 25));

        assertThat(shouldBeEmpty, is(Optional.empty()));
    }
}
