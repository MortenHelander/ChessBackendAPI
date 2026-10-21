package app.testutils;

import app.entities.DailyPuzzle;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceException;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class DailyPuzzleTestPopulator {

    // fixed dates so the tests never depend on the real clock
    public static final LocalDate WHITE_PUZZLE_DATE = LocalDate.of(2026, 10, 20);
    public static final LocalDate BLACK_PUZZLE_DATE = LocalDate.of(2026, 10, 21);
    // a date with no puzzle, for "not found" tests
    public static final LocalDate NO_PUZZLE_DATE = LocalDate.of(2026, 10, 22);

    private DailyPuzzleTestPopulator() {
    }

    public static Map<String, DailyPuzzle> populate(EntityManagerFactory emf) {
        try (EntityManager em = emf.createEntityManager()) {

            em.getTransaction().begin();

            // real Lichess daily puzzle, white to move, mate in 2 (3 moves in the solution)
            DailyPuzzle whiteToMove = new DailyPuzzle(
                    WHITE_PUZZLE_DATE,
                    "lpmqI",
                    1981,
                    List.of("g4h5", "g6h5", "d7g4"),
                    "8/3Q4/5qkp/3p4/1p2n1B1/1Pr4P/P1P2bP1/2R2K2 w - - 1 1",
                    "h7g6");

            // real Lichess daily puzzle, black to move, mate in 5 (9 moves in the solution)
            DailyPuzzle blackToMove = new DailyPuzzle(
                    BLACK_PUZZLE_DATE,
                    "W4lEb",
                    1964,
                    List.of("c8c1", "d3f1", "c1f1", "g1f1", "e8b5", "d4d3", "b5d3", "f1g1", "e5e1"),
                    "2r1b3/1p2kpQ1/p3p3/4r3/3R4/P2B4/1P3PPP/2R3K1 b - - 0 1",
                    "a1c1");

            try {
                em.createNativeQuery("TRUNCATE TABLE daily_puzzles RESTART IDENTITY CASCADE").executeUpdate();
                em.persist(whiteToMove);
                em.persist(blackToMove);
                em.flush();
            } catch (PersistenceException e) {
                if (em.getTransaction().isActive()) em.getTransaction().rollback();
                throw e;
            }
            em.getTransaction().commit();

            Map<String, DailyPuzzle> seeded = new LinkedHashMap<>();
            seeded.put("whiteToMove", whiteToMove);
            seeded.put("blackToMove", blackToMove);
            return seeded;
        }
    }
}