package app.endpoints;

import app.config.ApplicationConfig;
import app.config.HibernateTestConfig;
import app.daos.DailyPuzzleDAO;
import app.dtos.lichess.LichessDailyPuzzleDTO;
import app.dtos.lichess.Puzzle;
import app.entities.DailyPuzzle;
import app.exceptions.ApiException;
import app.testutils.DailyPuzzleTestPopulator;
import app.testutils.FakeLichessClient;
import app.testutils.TestTokens;
import io.javalin.Javalin;
import io.javalin.http.ContentType;
import io.restassured.RestAssured;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

import static app.testutils.TestTokens.bearer;
import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DailyPuzzleEndpointsTest {

    // "today" for the whole test class. The populator seeds the 20th and 21st, so the 22nd starts empty
    private static final LocalDate TODAY = DailyPuzzleTestPopulator.NO_PUZZLE_DATE;
    private static final String DAILY_URL = "/api/v1/puzzles/daily";

    // real Lichess daily puzzle, black to move, mate in 5
    private static final String VALID_FEN = "2r1b3/1p2kpQ1/p3p3/4r3/3R4/P2B4/1P3PPP/2R3K1 b - - 0 1";
    private static final List<String> VALID_SOLUTION =
            List.of("c8c1", "d3f1", "c1f1", "g1f1", "e8b5", "d4d3", "b5d3", "f1g1", "e5e1");

    EntityManagerFactory emf = HibernateTestConfig.getEntityManagerFactory();
    ApplicationConfig applicationConfig;
    Javalin app;
    FakeLichessClient fakeLichessClient;
    DailyPuzzleDAO dailyPuzzleDAO;

    String userToken = TestTokens.userToken();

    @BeforeAll
    void init() {
        Clock fixedClock = Clock.fixed(TODAY.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneOffset.UTC);
        fakeLichessClient = new FakeLichessClient(validLichessPuzzle());
        dailyPuzzleDAO = new DailyPuzzleDAO(emf);

        applicationConfig = new ApplicationConfig(emf, fixedClock, fakeLichessClient);
        app = applicationConfig.createApp(0);
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = app.port();
    }

    @AfterAll
    void shutDown() {
        app.stop();
    }

    @BeforeEach
    void setUp() {
        DailyPuzzleTestPopulator.populate(emf);
        fakeLichessClient.reset();
        fakeLichessClient.setPuzzleToReturn(validLichessPuzzle());
    }


    // ---------- puzzle already saved ----------

    @Test
    void getDailyPuzzle_alreadySavedToday_returns200AndDoesNotCallLichess() {
        dailyPuzzleDAO.create(new DailyPuzzle(TODAY, "W4lEb", 1964, VALID_SOLUTION, VALID_FEN, "a1c1"));

        given()
                .header("Authorization", bearer(userToken))
                .contentType(ContentType.JSON)
                .when()
                .get(DAILY_URL)
                .then().statusCode(200)
                .body("date", is(TODAY.toString()))
                .body("fen", is(VALID_FEN))
                .body("rating", is(1964))
                .body("lastMove", is("a1c1"))
                .body("isWhitesTurn", is(false))
                .body("movesToSolve", is(5));

        assertThat(fakeLichessClient.getCalls(), is(0));
    }

    @Test
    void getDailyPuzzle_responseNeverContainsSolution() {
        given()
                .header("Authorization", bearer(userToken))
                .when()
                .get(DAILY_URL)
                .then().statusCode(200)
                .body("$", not(hasKey("solution")));
    }


    // ---------- no puzzle yet, fetch from Lichess ----------

    @Test
    void getDailyPuzzle_noPuzzleToday_fetchesFromLichessAndSaves() {
        given()
                .header("Authorization", bearer(userToken))
                .when()
                .get(DAILY_URL)
                .then().statusCode(200)
                .body("date", is(TODAY.toString()))
                .body("fen", is(VALID_FEN))
                .body("isWhitesTurn", is(false));

        assertThat(fakeLichessClient.getCalls(), is(1));

        Optional<DailyPuzzle> saved = dailyPuzzleDAO.getByDate(TODAY);
        assertThat(saved.isPresent(), is(true));
        assertThat(saved.get().getLichessPuzzleId(), is("W4lEb"));
        assertThat(saved.get().getSolution(), contains(VALID_SOLUTION.toArray()));
    }

    @Test
    void getDailyPuzzle_calledTwiceSameDay_callsLichessOnlyOnce() {
        given()
                .header("Authorization", bearer(userToken))
                .when()
                .get(DAILY_URL)
                .then().statusCode(200);

        given()
                .header("Authorization", bearer(userToken))
                .when()
                .get(DAILY_URL)
                .then().statusCode(200)
                .body("fen", is(VALID_FEN));

        assertThat(fakeLichessClient.getCalls(), is(1));
    }


    // ---------- bad data from Lichess -> 502, nothing saved ----------

    @Test
    void getDailyPuzzle_lichessSendsInvalidFen_returns502AndSavesNothing() {
        fakeLichessClient.setPuzzleToReturn(lichessPuzzle("8/8/8 w - - 0 1", VALID_SOLUTION));

        given()
                .header("Authorization", bearer(userToken))
                .when()
                .get(DAILY_URL)
                .then().statusCode(502)
                .body("error", containsString("FEN"));

        assertThat(dailyPuzzleDAO.getByDate(TODAY).isPresent(), is(false));
    }

    @Test
    void getDailyPuzzle_lichessSendsEmptySolution_returns502AndSavesNothing() {
        fakeLichessClient.setPuzzleToReturn(lichessPuzzle(VALID_FEN, List.of()));

        given()
                .header("Authorization", bearer(userToken))
                .when()
                .get(DAILY_URL)
                .then().statusCode(502)
                .body("error", containsString("solution"));

        assertThat(dailyPuzzleDAO.getByDate(TODAY).isPresent(), is(false));
    }

    @Test
    void getDailyPuzzle_lichessSendsNothing_returns502() {
        fakeLichessClient.setPuzzleToReturn(null);

        given()
                .header("Authorization", bearer(userToken))
                .when()
                .get(DAILY_URL)
                .then().statusCode(502);

        assertThat(dailyPuzzleDAO.getByDate(TODAY).isPresent(), is(false));
    }

    @Test
    void getDailyPuzzle_lichessIsDown_returns502AndSavesNothing() {
        fakeLichessClient.setExceptionToThrow(new ApiException(502, "Could not reach Lichess"));

        given()
                .header("Authorization", bearer(userToken))
                .when()
                .get(DAILY_URL)
                .then().statusCode(502)
                .body("error", is("Could not reach Lichess"));

        assertThat(dailyPuzzleDAO.getByDate(TODAY).isPresent(), is(false));
    }


    // ---------- access ----------

    @Test
    void getDailyPuzzle_noToken_returns401() {
        given()
                .when()
                .get(DAILY_URL)
                .then().statusCode(401)
                .body("error", is("Unauthorized"));

        assertThat(fakeLichessClient.getCalls(), is(0));
    }


    // ---------- concurrency ----------

    @Test
    void getDailyPuzzle_tenParallelRequests_allGetSamePuzzleAndOnlyOneIsSaved() throws Exception {
        int threads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        // makes all threads wait at the start line, so the requests really hit the server at the same time
        CountDownLatch startSignal = new CountDownLatch(1);

        List<Future<String>> futures = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            futures.add(executor.submit(() -> {
                startSignal.await();
                return given()
                        .header("Authorization", bearer(userToken))
                        .when()
                        .get(DAILY_URL)
                        .then().statusCode(200)
                        .extract().path("fen");
            }));
        }

        startSignal.countDown();

        List<String> fens = new ArrayList<>();
        for (Future<String> future : futures) {
            fens.add(future.get(30, TimeUnit.SECONDS)); // rethrows if a request failed (500 instead of 200)
        }
        executor.shutdown();

        // every request got a 200 and the same puzzle
        assertThat(fens, hasSize(threads));
        assertThat(fens, everyItem(is(VALID_FEN)));

        // exactly one puzzle saved for today (2 seeded + 1 new)
        assertThat(dailyPuzzleDAO.getByDate(TODAY).isPresent(), is(true));
        assertThat(dailyPuzzleDAO.getAll(), hasSize(3));

        // without extra locking several threads may have called Lichess before the first one saved,
        assertThat(fakeLichessClient.getCalls(), allOf(greaterThanOrEqualTo(1), lessThanOrEqualTo(threads)));
    }

    // ---------- helpers ----------

    private static LichessDailyPuzzleDTO validLichessPuzzle() {
        return lichessPuzzle(VALID_FEN, VALID_SOLUTION);
    }

    private static LichessDailyPuzzleDTO lichessPuzzle(String fen, List<String> solution) {
        return new LichessDailyPuzzleDTO(new Puzzle("W4lEb", 1964, solution, fen, "a1c1"));
    }
}