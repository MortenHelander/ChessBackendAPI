package app.daos;

import app.entities.DailyPuzzle;
import app.exceptions.ApiException;
import app.exceptions.DuplicatePuzzleException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;
import org.hibernate.exception.ConstraintViolationException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class DailyPuzzleDAO{
    private final EntityManagerFactory emf;

    public DailyPuzzleDAO(EntityManagerFactory emf){
        this.emf = emf;
    }

    public DailyPuzzle create(DailyPuzzle dailyPuzzle){
        if (dailyPuzzle == null) {
            throw new ApiException(400, "Provided daily puzzle is null");
        }
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            try {
                em.persist(dailyPuzzle);
                em.getTransaction().commit();
            } catch (PersistenceException e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                if (isDuplicateKey(e)){ //handles if two users try to create the daily puzzle at the same time
                    throw new DuplicatePuzzleException(409, "Daily puzzle for " + dailyPuzzle.getId() + " already exists");
                }else{
                    throw new ApiException(500, "Creation of daily puzzle failed: " + e.getMessage());
                }
            } catch (RuntimeException e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw e;
            }
            return dailyPuzzle;
        }
    }

    public Optional<DailyPuzzle> getByDate(LocalDate date){
        if (date == null){
            throw new ApiException(400, "Date is required");
        }
        try(EntityManager em = emf.createEntityManager()){
            try {
                DailyPuzzle dailyPuzzle = em.find(DailyPuzzle.class, date);
                return Optional.ofNullable(dailyPuzzle); //expected to find nothing each day then call the API to create it
            } catch (PersistenceException e){
                throw new ApiException(500, "Get daily puzzle failed: " + e.getMessage());
            }
        }
    }

    public List<DailyPuzzle> getAll() {
        try(EntityManager em = emf.createEntityManager()){
            try{
                TypedQuery<DailyPuzzle> query = em.createQuery("SELECT p FROM DailyPuzzle p", DailyPuzzle.class);
                return query.getResultList();
            } catch (PersistenceException e){
                throw new ApiException(500, "Get all daily puzzles failed: " + e.getMessage());
            }
        }
    }

    public boolean delete(LocalDate date) {
        if (date == null){
            throw new ApiException(400, "Daily puzzle date is required");
        }
        try(EntityManager em = emf.createEntityManager()){
            em.getTransaction().begin();
            try{
                DailyPuzzle puzzleToRemove = em.find(DailyPuzzle.class, date);
                if (puzzleToRemove != null){
                    em.remove(puzzleToRemove);
                } else {
                    throw ApiException.notFound("DailyPuzzle", date);
                }
                em.getTransaction().commit();
            } catch (PersistenceException e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
            } catch (RuntimeException e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw e;
            }
        }
        return true;
    }

    private static boolean isDuplicateKey(Throwable e){
        Throwable current = e;
        while (current != null){
            if (current instanceof ConstraintViolationException cve){
                if ("23505".equals(cve.getSQLState()) || cve.getKind() == ConstraintViolationException.ConstraintKind.UNIQUE){ //checks both the code and kind for unique or primary violation
                    return true;
                }
            }
            current = current.getCause();
        }
        return false;
    }
}
