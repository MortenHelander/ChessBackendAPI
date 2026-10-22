package app.testutils;

import app.entities.DailyPuzzle;
import app.entities.DailyPuzzleAttempt;
import app.entities.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceException;

import java.util.LinkedHashMap;
import java.util.Map;

public final class DailyPuzzleAttemptTestPopulator {

    private DailyPuzzleAttemptTestPopulator() {}

    public static Seeded populate(EntityManagerFactory emf) {
        // seeds the two puzzles (white to move on the 20th, black to move on the 21st).
        Map<String, DailyPuzzle> puzzles = DailyPuzzleTestPopulator.populate(emf);

        try (EntityManager em = emf.createEntityManager()) {

            em.getTransaction().begin();

            User user1 = new User("sheriffen", "morten@hotmail.com", "Morten", "Helander", "password123");
            User user2 = new User("svg-man", "theis@hotmail.com", "Theis", "Rudkjær", "anotherpassword1441");
            User user3 = new User("frontjoe", "andreas@hotmail.com", "Andreas", "Jensen", "andreas1234567");

            DailyPuzzleAttempt inProgress;
            DailyPuzzleAttempt solved;
            DailyPuzzleAttempt failed;

            try {
                em.createNativeQuery("TRUNCATE TABLE users RESTART IDENTITY CASCADE").executeUpdate();
                em.persist(user1);
                em.persist(user2);
                em.persist(user3);

                DailyPuzzle whitePuzzle = em.find(DailyPuzzle.class, puzzles.get("whiteToMove").getId());
                DailyPuzzle blackPuzzle = em.find(DailyPuzzle.class, puzzles.get("blackToMove").getId());

                // user1, black puzzle: played the first correct move (+ reply), made one mistake
                inProgress = DailyPuzzleAttempt.startNewPuzzle(blackPuzzle, user1);
                inProgress.advance();
                inProgress.removeOneAttempt();

                // user1, white puzzle: solved
                solved = DailyPuzzleAttempt.startNewPuzzle(whitePuzzle, user1);
                solved.markAsSolved();

                // user2, white puzzle: used up all tries
                failed = DailyPuzzleAttempt.startNewPuzzle(whitePuzzle, user2);
                while (failed.getAttemptsLeft() > 0) {
                    failed.removeOneAttempt();
                }

                em.persist(inProgress);
                em.persist(solved);
                em.persist(failed);
                em.flush();
            } catch (PersistenceException e) {
                if (em.getTransaction().isActive()) em.getTransaction().rollback();
                throw e;
            }
            em.getTransaction().commit();

            Map<String, User> seededUsers = new LinkedHashMap<>();
            seededUsers.put("user1", user1);
            seededUsers.put("user2", user2);
            seededUsers.put("user3", user3);

            Map<String, DailyPuzzleAttempt> seededAttempts = new LinkedHashMap<>();
            seededAttempts.put("inProgress", inProgress);
            seededAttempts.put("solved", solved);
            seededAttempts.put("failed", failed);

            return new Seeded(seededUsers, puzzles, seededAttempts);
        }
    }
}