package app.endpoints;

import app.config.ApplicationConfig;
import app.config.HibernateTestConfig;
import app.daos.DailyPuzzleAttemptDAO;
import app.daos.DailyPuzzleDAO;
import app.dtos.lichess.LichessDailyPuzzleDTO;
import app.dtos.lichess.Puzzle;
import app.entities.DailyPuzzle;
import app.entities.DailyPuzzleAttempt;
import app.entities.User;
import app.entities.enums.PuzzleStatus;
import app.exceptions.ApiException;
import app.gameengine.Board;
import app.gameengine.FenGameState;
import app.gameengine.Position;
import app.gameengine.gameservices.FenParser;
import app.gameengine.gameservices.PieceFinder;
import app.gameengine.gameservices.UciParser;
import app.gameengine.pieces.Bishop;
import app.gameengine.pieces.Piece;
import app.gameengine.pieces.Rook;
import app.services.ReplayHelper;
import app.testutils.DailyPuzzleAttemptTestPopulator;
import app.testutils.DailyPuzzleTestPopulator;
import app.testutils.FakeLichessClient;
import app.testutils.Seeded;
import app.testutils.TestTokens;
import io.javalin.Javalin;
import io.javalin.http.ContentType;
import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;

import static app.testutils.TestTokens.bearer;
import static app.testutils.TestTokens.tokenFor;
import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DailyPuzzleEndpointsTest {

    // "today" for the whole test class. The populator seeds the 20th and 21st, so the 22nd starts empty
    private static final LocalDate TODAY = DailyPuzzleTestPopulator.NO_PUZZLE_DATE;
    private static final String DAILY_URL = "/api/v1/puzzles/daily";
    private static final String MOVES_URL = "/api/v1/puzzles/daily/moves";

    // real Lichess daily puzzle, black to move, mate in 5
    private static final String VALID_FEN = "2r1b3/1p2kpQ1/p3p3/4r3/3R4/P2B4/1P3PPP/2R3K1 b - - 0 1";
    private static final List<String> VALID_SOLUTION =
            List.of("c8c1", "d3f1", "c1f1", "g1f1", "e8b5", "d4d3", "b5d3", "f1g1", "e5e1");

    // real Lichess daily puzzle, white to move, mate in 2
    private static final String WHITE_FEN = "8/3Q4/5qkp/3p4/1p2n1B1/1Pr4P/P1P2bP1/2R2K2 w - - 1 1";
    private static final List<String> WHITE_SOLUTION = List.of("g4h5", "g6h5", "d7g4");

    // made-up mate in 1 with TWO mating moves: a1a8 and e1e8. The solution only lists a1a8
    private static final String TWO_MATES_FEN = "6k1/5ppp/8/8/8/8/8/R3R1K1 w - - 0 1";

    EntityManagerFactory emf = HibernateTestConfig.getEntityManagerFactory();
    ApplicationConfig applicationConfig;
    Javalin app;
    FakeLichessClient fakeLichessClient;
    DailyPuzzleDAO dailyPuzzleDAO;
    DailyPuzzleAttemptDAO attemptDAO;

    String userToken = TestTokens.userToken();

    // tokens for real, seeded users (needed for moves, because a new attempt loads the user from the db).
    // user2 and user3 have no attempt on today's puzzle
    User user2;
    User user3;
    String user2Token;
    String user3Token;

    @BeforeAll
    void init() {
        Clock fixedClock = Clock.fixed(TODAY.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneOffset.UTC);
        fakeLichessClient = new FakeLichessClient(validLichessPuzzle());
        dailyPuzzleDAO = new DailyPuzzleDAO(emf);
        attemptDAO = new DailyPuzzleAttemptDAO(emf);

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
        // seeds users, the two puzzles (20th + 21st) and attempts on them. Today (22nd) starts empty
        Seeded seeded = DailyPuzzleAttemptTestPopulator.populate(emf);
        Map<String, User> users = seeded.users();
        user2 = users.get("user2");
        user3 = users.get("user3");
        user2Token = tokenFor(user2.getId(), user2.getUsername());
        user3Token = tokenFor(user3.getId(), user3.getUsername());

        fakeLichessClient.reset();
        fakeLichessClient.setPuzzleToReturn(validLichessPuzzle());
    }

    @Test
    void tempCheck(){
        FenGameState state = FenParser.fromFen(VALID_FEN);
        Board board = ReplayHelper.rebuildBoard(state.board(), UciParser.toMoves(List.of("c8c1", "d3f1")));
        Piece bRook = PieceFinder.findPiece(board, Position.C1);
        Piece wBishop = PieceFinder.findPiece(board, Position.F1);
        assertThat(bRook, is(instanceOf(Rook.class)));
        assertThat(bRook.isWhite(), is(false));
        assertThat(wBishop, is(instanceOf(Bishop.class)));
        assertThat(wBishop.isWhite(), is(true));
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


    // ---------- no puzzle yet -> fetch from Lichess ----------

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

        startSignal.countDown(); // go!

        List<String> fens = new ArrayList<>();
        for (Future<String> future : futures) {
            fens.add(future.get(30, TimeUnit.SECONDS)); // rethrows if a request failed (e.g. a 500 instead of 200)
        }
        executor.shutdown();

        // every request got a 200 and the same puzzle
        assertThat(fens, hasSize(threads));
        assertThat(fens, everyItem(is(VALID_FEN)));

        // exactly one puzzle saved for today (2 seeded + 1 new)
        assertThat(dailyPuzzleDAO.getByDate(TODAY).isPresent(), is(true));
        assertThat(dailyPuzzleDAO.getAll(), hasSize(3));

        // without extra locking several threads may have called Lichess before the first one saved,
        // the database primary key is what keeps it to one puzzle
        assertThat(fakeLichessClient.getCalls(), allOf(greaterThanOrEqualTo(1), lessThanOrEqualTo(threads)));
    }


    // ---------- playing: correct moves ----------

    @Test
    void playMove_correctFirstMove_returnsOpponentReply() {
        seedTodaysPuzzle(VALID_FEN, VALID_SOLUTION);

        postMove(user3Token, "c8", "c1")
                .statusCode(201)
                .body("isCorrect", is(true))
                .body("opponentMove", is("d3f1"))
                .body("status", is("IN_PROGRESS"))
                .body("attemptsLeft", is(10))
                .body("movesLeft", is(4));

        DailyPuzzleAttempt saved = attemptDAO.getByUserIdAndPuzzleDate(user3.getId(), TODAY).get();
        assertThat(saved.getMoveIndex(), is(2));
    }

    @Test
    void playMove_fullSolutionBlackPuzzle_isSolved() {
        seedTodaysPuzzle(VALID_FEN, VALID_SOLUTION);

        postMove(user3Token, "c8", "c1").statusCode(201).body("opponentMove", is("d3f1"));
        postMove(user3Token, "c1", "f1").statusCode(201).body("opponentMove", is("g1f1"));
        postMove(user3Token, "e8", "b5").statusCode(201).body("opponentMove", is("d4d3"));
        postMove(user3Token, "b5", "d3").statusCode(201).body("opponentMove", is("f1g1"));

        // last move: no reply, puzzle solved
        postMove(user3Token, "e5", "e1")
                .statusCode(201)
                .body("isCorrect", is(true))
                .body("opponentMove", nullValue())
                .body("status", is("SOLVED"))
                .body("movesLeft", is(0));

        DailyPuzzleAttempt saved = attemptDAO.getByUserIdAndPuzzleDate(user3.getId(), TODAY).get();
        assertThat(saved.getStatus(), is(PuzzleStatus.SOLVED));
        assertThat(saved.getFinishedAt(), is(notNullValue()));
    }

    @Test
    void playMove_fullSolutionWhitePuzzle_isSolved() {
        seedTodaysPuzzle(WHITE_FEN, WHITE_SOLUTION);

        postMove(user3Token, "g4", "h5")
                .statusCode(201)
                .body("isCorrect", is(true))
                .body("opponentMove", is("g6h5"));

        postMove(user3Token, "d7", "g4")
                .statusCode(201)
                .body("status", is("SOLVED"));
    }

    @Test
    void playMove_alternativeCheckmate_isAcceptedAsSolved() {
        // the solution says a1a8, but e1e8 is also checkmate
        seedTodaysPuzzle(TWO_MATES_FEN, List.of("a1a8"));

        postMove(user3Token, "e1", "e8")
                .statusCode(201)
                .body("isCorrect", is(true))
                .body("status", is("SOLVED"))
                .body("attemptsLeft", is(10));
    }


    // ---------- playing: wrong moves ----------

    @Test
    void playMove_wrongButLegalMove_costsOneTry() {
        seedTodaysPuzzle(VALID_FEN, VALID_SOLUTION);

        postMove(user3Token, "a6", "a5")
                .statusCode(201)
                .body("isCorrect", is(false))
                .body("opponentMove", nullValue())
                .body("status", is("IN_PROGRESS"))
                .body("attemptsLeft", is(9))
                .body("movesLeft", is(5));
    }

    @Test
    void playMove_afterWrongMove_isCorrectMoveIsStillAccepted() {
        seedTodaysPuzzle(VALID_FEN, VALID_SOLUTION);

        postMove(user3Token, "a6", "a5").statusCode(201).body("isCorrect", is(false));

        // the wrong move is not kept on the board, the user is still at the first move
        postMove(user3Token, "c8", "c1")
                .statusCode(201)
                .body("isCorrect", is(true))
                .body("opponentMove", is("d3f1"))
                .body("attemptsLeft", is(9));
    }

    @Test
    void playMove_tenWrongMoves_failsAttempt() {
        seedTodaysPuzzle(VALID_FEN, VALID_SOLUTION);

        for (int i = 0; i < 9; i++) {
            postMove(user3Token, "a6", "a5").statusCode(201).body("status", is("IN_PROGRESS"));
        }

        postMove(user3Token, "a6", "a5")
                .statusCode(201)
                .body("isCorrect", is(false))
                .body("status", is("FAILED"))
                .body("attemptsLeft", is(0));

        DailyPuzzleAttempt saved = attemptDAO.getByUserIdAndPuzzleDate(user3.getId(), TODAY).get();
        assertThat(saved.getStatus(), is(PuzzleStatus.FAILED));
        assertThat(saved.getFinishedAt(), is(notNullValue()));
    }

    @Test
    void playMove_afterFailed_returns400() {
        seedTodaysPuzzle(VALID_FEN, VALID_SOLUTION);
        for (int i = 0; i < 10; i++) {
            postMove(user3Token, "a6", "a5").statusCode(201);
        }

        postMove(user3Token, "c8", "c1")
                .statusCode(400)
                .body("error", is("Puzzle already FAILED"));
    }

    @Test
    void playMove_afterSolved_returns400() {
        seedTodaysPuzzle(WHITE_FEN, WHITE_SOLUTION);
        postMove(user3Token, "g4", "h5").statusCode(201);
        postMove(user3Token, "d7", "g4").statusCode(201).body("status", is("SOLVED"));

        postMove(user3Token, "g4", "h5")
                .statusCode(400)
                .body("error", is("Puzzle already SOLVED"));
    }


    // ---------- playing: invalid moves (rejected, no try used) ----------

    @Test
    void playMove_illegalMove_returns400AndSavesNoAttempt() {
        seedTodaysPuzzle(VALID_FEN, VALID_SOLUTION);

        // pawn on a6 has already moved, so it can't go two squares
        postMove(user3Token, "a6", "a4")
                .statusCode(400)
                .body("error", is("Illegal move"));

        assertThat(attemptDAO.getByUserIdAndPuzzleDate(user3.getId(), TODAY).isPresent(), is(false));
    }

    @Test
    void playMove_illegalMoveAfterWrongMove_doesNotCostTry() {
        seedTodaysPuzzle(VALID_FEN, VALID_SOLUTION);
        postMove(user3Token, "a6", "a5").statusCode(201).body("attemptsLeft", is(9));

        postMove(user3Token, "a6", "a4").statusCode(400);

        DailyPuzzleAttempt saved = attemptDAO.getByUserIdAndPuzzleDate(user3.getId(), TODAY).get();
        assertThat(saved.getAttemptsLeft(), is(9));
        assertThat(saved.getStatus(), is(PuzzleStatus.IN_PROGRESS));
    }

    @Test
    void playMove_opponentsPiece_returns400() {
        seedTodaysPuzzle(VALID_FEN, VALID_SOLUTION);

        // black is solving, d4 is a white rook
        postMove(user3Token, "d4", "d5")
                .statusCode(400)
                .body("error", is("Not your piece"));
    }

    @Test
    void playMove_emptySquare_returns400() {
        seedTodaysPuzzle(VALID_FEN, VALID_SOLUTION);

        postMove(user3Token, "a1", "a2")
                .statusCode(400)
                .body("error", is("No piece on selected square"));
    }

    @Test
    void playMove_invalidSquare_returns400() {
        seedTodaysPuzzle(VALID_FEN, VALID_SOLUTION);

        postMove(user3Token, "z9", "c1")
                .statusCode(400);
    }

    @Test
    void playMove_missingFrom_returns400() {
        seedTodaysPuzzle(VALID_FEN, VALID_SOLUTION);

        given()
                .header("Authorization", bearer(user3Token))
                .contentType(ContentType.JSON)
                .body(Map.of("to", "c1"))
                .when()
                .post(MOVES_URL)
                .then().statusCode(400)
                .body("error", is("From position is required"));
    }

    @Test
    void playMove_noToken_returns401() {
        seedTodaysPuzzle(VALID_FEN, VALID_SOLUTION);

        given()
                .contentType(ContentType.JSON)
                .body(Map.of("from", "c8", "to", "c1"))
                .when()
                .post(MOVES_URL)
                .then().statusCode(401);
    }


    // ---------- playing: users and safety ----------

    @Test
    void playMove_twoUsers_haveSeparateAttempts() {
        seedTodaysPuzzle(VALID_FEN, VALID_SOLUTION);

        // user3 gets one move further
        postMove(user3Token, "c8", "c1").statusCode(201).body("isCorrect", is(true));

        // user2 still starts at the first move, so the same move isCorrect for them too
        postMove(user2Token, "c8", "c1")
                .statusCode(201)
                .body("isCorrect", is(true))
                .body("opponentMove", is("d3f1"));

        assertThat(attemptDAO.getByUserIdAndPuzzleDate(user3.getId(), TODAY).get().getMoveIndex(), is(2));
        assertThat(attemptDAO.getByUserIdAndPuzzleDate(user2.getId(), TODAY).get().getMoveIndex(), is(2));
    }

    @Test
    void playMove_responseNeverContainsSolution() {
        seedTodaysPuzzle(VALID_FEN, VALID_SOLUTION);

        postMove(user3Token, "a6", "a5")
                .statusCode(201)
                .body("$", not(hasKey("solution")))
                .body("$", not(hasKey("expected")));
    }

    @Test
    void playMove_sameMoveTwiceInParallel_neverReturns500() throws Exception {
        seedTodaysPuzzle(VALID_FEN, VALID_SOLUTION);

        int threads = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch startSignal = new CountDownLatch(1);

        List<Future<Integer>> futures = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            futures.add(executor.submit(() -> {
                startSignal.await();
                return given()
                        .header("Authorization", bearer(user3Token))
                        .contentType(ContentType.JSON)
                        .body(Map.of("from", "c8", "to", "c1"))
                        .when()
                        .post(MOVES_URL)
                        .then().extract().statusCode();
            }));
        }
        startSignal.countDown();

        List<Integer> statusCodes = new ArrayList<>();
        for (Future<Integer> future : futures) {
            statusCodes.add(future.get(30, TimeUnit.SECONDS));
        }
        executor.shutdown();

        // depending on timing: both 201 (the second one is then checked against the NEXT move and counts as wrong),
        // or one 201 and one 409 (unique constraint on create, or @Version on update). Never a 500
        assertThat(statusCodes, everyItem(anyOf(is(201), is(409))));
        assertThat(statusCodes, hasItem(201));

        // still exactly one attempt for user3
        assertThat(attemptDAO.getAllPuzzlesByUserId(user3.getId()), hasSize(1));
    }


    // ---------- helpers ----------

    private void seedTodaysPuzzle(String fen, List<String> solution) {
        dailyPuzzleDAO.create(new DailyPuzzle(TODAY, "testPuzzle", 1500, solution, fen, "a1a1"));
    }

    private ValidatableResponse postMove(String token, String from, String to) {
        return given()
                .header("Authorization", bearer(token))
                .contentType(ContentType.JSON)
                .body(Map.of("from", from, "to", to))
                .when()
                .post(MOVES_URL)
                .then();
    }

    private static LichessDailyPuzzleDTO validLichessPuzzle() {
        return lichessPuzzle(VALID_FEN, VALID_SOLUTION);
    }

    private static LichessDailyPuzzleDTO lichessPuzzle(String fen, List<String> solution) {
        return new LichessDailyPuzzleDTO(new Puzzle("W4lEb", 1964, solution, fen, "a1c1"));
    }
}