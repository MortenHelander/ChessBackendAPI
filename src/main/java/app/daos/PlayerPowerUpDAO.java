package app.daos;

import app.entities.PlayerPowerUp;
import app.exceptions.ApiException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class PlayerPowerUpDAO {
    private EntityManagerFactory emf;

    public PlayerPowerUpDAO(EntityManagerFactory emf){
        this.emf = emf;
    }

    public PlayerPowerUp create(PlayerPowerUp playerPowerUp) {
        if (playerPowerUp == null) {
            throw new ApiException(400, "Provided power-up is null");
        }
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            try {
                em.persist(playerPowerUp);
                em.getTransaction().commit();
            } catch (PersistenceException e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw new ApiException(500, "Creation of power-up failed: " + e.getMessage());
            } catch (RuntimeException e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw e;
            }
            return playerPowerUp;
        }
    }

    public PlayerPowerUp getById(Integer id) {
        if (id == null){
            throw new ApiException(400, "Power-Up ID is required");
        }
        try(EntityManager em = emf.createEntityManager()){
            try {
                PlayerPowerUp move = em.find(PlayerPowerUp.class, id);
                if (move != null) {
                    return move;
                }
                throw new ApiException(404, "Power-up not found");
            } catch (PersistenceException e){
                throw new ApiException(500, "Get power-up failed: " + e.getMessage());
            }
        }
    }

    public PlayerPowerUp update(PlayerPowerUp playerPowerUp) {
        if (playerPowerUp == null){
            throw new ApiException(400, "Provided power-up is null");
        } else if (playerPowerUp.getId() == null){
            throw new ApiException(400, "Power-up ID is required");
        }
        PlayerPowerUp updated;
        try(EntityManager em = emf.createEntityManager()){
            em.getTransaction().begin();
            try{
                PlayerPowerUp existing = em.find(PlayerPowerUp.class, playerPowerUp.getId());
                if (existing == null){
                    throw new ApiException(404, "Power-up not found");
                }
                updated = em.merge(playerPowerUp);
                em.getTransaction().commit();
            } catch (PersistenceException e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw new ApiException(500, "Update power-up failed: " + e.getMessage());
            } catch (RuntimeException e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw e;
            }
            return updated;
        }
    }

    public List<PlayerPowerUp> getAllPowerUpsByGameId(Integer id){
        if (id == null){
            throw new ApiException(400, "Power-up ID is required");
        }
        try(EntityManager em = emf.createEntityManager()){
            try {
                TypedQuery<PlayerPowerUp> query = em.createQuery("SELECT DISTINCT pu FROM PlayerPowerUp pu JOIN pu.player p JOIN p.game g WHERE g.id = :id", PlayerPowerUp.class);
                query.setParameter("id", id);
                return query.getResultList();
            } catch (PersistenceException e){
                throw new ApiException(500, "Get all power-ups by GameID: " + id +  " failed: " + e.getMessage());
            }
        }
    }
}
