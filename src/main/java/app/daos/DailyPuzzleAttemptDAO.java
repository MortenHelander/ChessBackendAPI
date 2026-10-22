package app.daos;

import app.entities.DailyPuzzleAttempt;
import app.exceptions.ApiException;
import app.exceptions.DuplicatePuzzleException;
import jakarta.persistence.*;
import org.hibernate.exception.ConstraintViolationException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class DailyPuzzleAttemptDAO {

    private final EntityManagerFactory emf;

    public DailyPuzzleAttemptDAO(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public DailyPuzzleAttempt create(DailyPuzzleAttempt attempt) {
        if (attempt == null) {
            throw new ApiException(400, "Provided daily puzzle attempt is null");
        }
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            try {
                em.persist(attempt);
                em.getTransaction().commit();
            } catch (PersistenceException e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                if (isDuplicateKey(e)) { //only one daily attempt per user
                    throw new DuplicatePuzzleException(409, "Daily puzzle attempt for user: " + attempt.getUser().getUsername() + " already exists");
                } else {
                    throw new ApiException(500, "Creation of daily puzzle attempt failed: " + e.getMessage());
                }
            } catch (RuntimeException e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw e;
            }
            return attempt;
        }
    }

    public Optional<DailyPuzzleAttempt> getByUserIdAndPuzzleDate(Integer userId, LocalDate puzzleDate) {
        if (userId == null) {
            throw new ApiException(400, "UserID is required");
        }
        if (puzzleDate == null) {
            throw new ApiException(400, "Date is required");
        }
        try (EntityManager em = emf.createEntityManager()) {
            try {
                TypedQuery<DailyPuzzleAttempt> query = em.createQuery("SELECT dpa FROM DailyPuzzleAttempt dpa " +
                        "JOIN dpa.user u JOIN dpa.dailyPuzzle dp WHERE u.id = :id AND dp.id = :date", DailyPuzzleAttempt.class);
                query.setParameter("id", userId);
                query.setParameter("date", puzzleDate);
                return query.getResultStream().findFirst();
            } catch (PersistenceException e) {
                throw new ApiException(500, "Get daily puzzle failed: " + e.getMessage());
            }
        }
    }

    public List<DailyPuzzleAttempt> getAllPuzzlesByUserId(Integer id) {
        if (id == null) {
            throw new ApiException(400, "UserID is required");
        }
        try (EntityManager em = emf.createEntityManager()) {
            try {
                TypedQuery<DailyPuzzleAttempt> query = em.createQuery("SELECT p FROM DailyPuzzleAttempt p JOIN p.user u WHERE u.id = :id", DailyPuzzleAttempt.class);
                query.setParameter("id", id);
                return query.getResultList();
            } catch (PersistenceException e) {
                throw new ApiException(500, "Get all daily puzzle attempts for user with ID: " + id + " failed: " + e.getMessage());
            }
        }
    }

    public DailyPuzzleAttempt update(DailyPuzzleAttempt attempt) {
        if (attempt == null) {
            throw new ApiException(400, "Provided puzzle attempt is null");
        } else if (attempt.getId() == null) {
            throw new ApiException(400, "Puzzle attempt ID is required");
        }
        DailyPuzzleAttempt updated;
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            try {
                DailyPuzzleAttempt existing = em.find(DailyPuzzleAttempt.class, attempt.getId());
                if (existing == null) {
                    throw ApiException.notFound("DailyPuzzleAttempt", attempt.getId());
                }
                updated = em.merge(attempt);
                em.getTransaction().commit();
            } catch (OptimisticLockException e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw e;
            } catch (PersistenceException e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                OptimisticLockException lockException = findOptimisticLock(e); //finds optimisticlock ex wrapped in hibernates rollbackexception
                if (lockException != null) {
                    throw lockException;
                }
                throw new ApiException(500, "Update daily puzzle attempt failed: " + e.getMessage());
            } catch (RuntimeException e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw e;
            }
            return updated;
        }
    }

    private static OptimisticLockException findOptimisticLock(Throwable e) {
        Throwable current = e;
        while (current != null) {
            if (current instanceof OptimisticLockException lockException) {
                return lockException;
            }
            current = current.getCause();
        }
        return null;
    }

    private static boolean isDuplicateKey(Throwable e) {
        Throwable current = e;
        while (current != null) {
            if (current instanceof ConstraintViolationException cve) {
                if ("23505".equals(cve.getSQLState()) || cve.getKind() == ConstraintViolationException.ConstraintKind.UNIQUE) {
                    return true;
                }
            }
            current = current.getCause();
        }
        return false;
    }
}
