package app.daos;

import app.entities.Game;
import app.entities.Player;
import app.entities.User;
import app.entities.UserStats;
import app.exceptions.ApiException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class GameDAO implements IDAO<Game, Integer>{

    private EntityManagerFactory emf;

    public GameDAO(EntityManagerFactory emf){
        this.emf = emf;
    }

    @Override
    public Game create(Game game) {
        if (game == null) {
            throw new ApiException(400, "Provided game is null");
        }
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            try {
                em.persist(game);
                em.getTransaction().commit();
            } catch (PersistenceException e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw new ApiException(500, "Creation of game failed: " + e.getMessage());
            } catch (RuntimeException e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw e;
            }
            return game;
        }
    }

    @Override
    public Game getById(Integer id) {
        if (id == null){
            throw new ApiException(400, "GameId is required");
        }
        try(EntityManager em = emf.createEntityManager()){
            try {
                Game game = em.find(Game.class, id);
                if (game != null) {
                    return game;
                }
                throw new ApiException(404, "Game not found");
            } catch (PersistenceException e){
                throw new ApiException(500, "Get game failed: " + e.getMessage());
            }
        }
    }

    @Override
    public List<Game> getAll() {
        try(EntityManager em = emf.createEntityManager()){
            try{
                TypedQuery<Game> query = em.createQuery("SELECT g FROM Game g", Game.class);
                return query.getResultList();
            } catch (PersistenceException e){
                throw new ApiException(500, "Get all games failed: " + e.getMessage());
            }
        }
    }

    public List<Game> getAllGameByUserId(Integer id){
        if (id == null){
            throw new ApiException(400, "UserID is required");
        }
        try(EntityManager em = emf.createEntityManager()){
            try {
                TypedQuery<Game> query = em.createQuery("SELECT DISTINCT g FROM Game g JOIN g.players p JOIN p.user u WHERE u.id = :id", Game.class);
                query.setParameter("id", id);
                return query.getResultList();
            } catch (PersistenceException e){
                throw new ApiException(500, "Get all games by UserID: " + id +  " failed: " + e.getMessage());
            }
        }
    }

    @Override
    public Game update(Game game) {
        if (game == null){
            throw new ApiException(400, "Provided game is null");
        } else if (game.getId() == null){
            throw new ApiException(400, "GameID is required");
        }
        Game updated;
        try(EntityManager em = emf.createEntityManager()){
            em.getTransaction().begin();
            try{
                Game existing = em.find(Game.class, game.getId());
                if (existing == null){
                    throw new ApiException(404, "Game not found");
                }
                updated = em.merge(game);
                em.getTransaction().commit();
            } catch (PersistenceException e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw new ApiException(500, "Update game failed: " + e.getMessage());
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
            throw new ApiException(400, "GameID is required");
        }
        try(EntityManager em = emf.createEntityManager()){
            em.getTransaction().begin();
            try{
                Game gameToRemove = em.find(Game.class, id);
                if (gameToRemove != null){
                    em.remove(gameToRemove);
                } else {
                    throw new ApiException(404, "Game not found");
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
