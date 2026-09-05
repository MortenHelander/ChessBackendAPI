package app.daos;

import app.entities.Game;
import app.entities.Move;
import app.entities.Player;
import app.exceptions.ApiException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class MoveDAO {

    private EntityManagerFactory emf;

    public MoveDAO(EntityManagerFactory emf){
        this.emf = emf;
    }

    public Move create(Move move) {
        if (move == null) {
            throw new ApiException(400, "Provided move is null");
        }
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            try {
                em.persist(move);
                em.getTransaction().commit();
            } catch (PersistenceException e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw new ApiException(500, "Creation of move failed: " + e.getMessage());
            } catch (RuntimeException e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw e;
            }
            return move;
        }
    }

    public Move getById(Integer id) {
        if (id == null){
            throw new ApiException(400, "MoveID is required");
        }
        try(EntityManager em = emf.createEntityManager()){
            try {
                Move move = em.find(Move.class, id);
                if (move != null) {
                    return move;
                }
                throw new ApiException(404, "Move not found");
            } catch (PersistenceException e){
                throw new ApiException(500, "Get move failed: " + e.getMessage());
            }
        }
    }

    public List<Move> getAllMovesByGameId(Integer id){
        if (id == null){
            throw new ApiException(400, "GameID is required");
        }
        try(EntityManager em = emf.createEntityManager()){
            try {
                TypedQuery<Move> query = em.createQuery("SELECT DISTINCT m FROM Move m JOIN m.games g WHERE g.id = :id", Move.class);
                query.setParameter("id", id);
                return query.getResultList();
            } catch (PersistenceException e){
                throw new ApiException(500, "Get all moves by GameID: " + id +  " failed: " + e.getMessage());
            }
        }
    }
}
