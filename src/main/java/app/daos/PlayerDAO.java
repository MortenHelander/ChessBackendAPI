package app.daos;

import app.entities.Player;
import app.entities.User;
import app.exceptions.ApiException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class PlayerDAO implements IDAO<Player, Integer> {

    private EntityManagerFactory emf;

    public PlayerDAO(EntityManagerFactory emf){
        this.emf = emf;
    }

    @Override
    public Player create(Player player) {
        if (player == null) {
            throw new ApiException(400, "Provided player is null");
        }
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            try {
                em.persist(player);
                em.getTransaction().commit();
            } catch (PersistenceException e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw new ApiException(500, "Creation of player failed: " + e.getMessage());
            } catch (RuntimeException e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw e;
            }
            return player;
        }
    }

    @Override
    public Player getById(Integer id) {
        if (id == null){
            throw new ApiException(400, "UserID is required");
        }
        try(EntityManager em = emf.createEntityManager()){
            try {
                Player player = em.find(Player.class, id);
                if (player != null) {
                    return player;
                }
                throw new ApiException(404, "Player not found");
            } catch (PersistenceException e){
                throw new ApiException(500, "Get player failed: " + e.getMessage());
            }
        }
    }

    @Override
    public List<Player> getAll() {
        try(EntityManager em = emf.createEntityManager()){
            try{
                TypedQuery<Player> query = em.createQuery("SELECT p FROM Player p", Player.class);
                return query.getResultList();
            } catch (PersistenceException e){
                throw new ApiException(500, "Get all players failed: " + e.getMessage());
            }
        }
    }

    @Override
    public Player update(Player player) {
        if (player == null){
            throw new ApiException(400, "Provided user is null");
        } else if (player.getId() == null){
            throw new ApiException(400, "UserID is required");
        }
        Player updated;
        try(EntityManager em = emf.createEntityManager()){
            em.getTransaction().begin();
            try{
                Player existing = em.find(Player.class, player.getId());
                if (existing == null){
                    throw new ApiException(404, "Player not found");
                }
                updated = em.merge(player);
                em.getTransaction().commit();
            } catch (PersistenceException e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw new ApiException(500, "Update player failed: " + e.getMessage());
            } catch (RuntimeException e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw e;
            }
            return updated;
        }
    }

    @Override
    public boolean delete(Integer id) {
        if (id == null){
            throw new ApiException(400, "UserID is required");
        }
        try(EntityManager em = emf.createEntityManager()){
            em.getTransaction().begin();
            try{
                Player playerToRemove = em.find(Player.class, id);
                if (playerToRemove != null){
                    em.remove(playerToRemove);
                } else {
                    throw new ApiException(404, "Player not found");
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
}
