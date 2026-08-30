package app.daos;

import app.entities.Result;
import app.entities.User;
import app.entities.UserStats;
import app.exceptions.ApiException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class UserStatsDAO {

    private EntityManagerFactory emf;

    public UserStatsDAO(EntityManagerFactory emf){
        this.emf = emf;
    }

    public UserStats getById(Integer id){
        if (id == null){
            throw new ApiException(400, "UserID is required");
        }
        try(EntityManager em = emf.createEntityManager()){
            try {
                UserStats userStats = em.find(UserStats.class, id);
                if (userStats != null) {
                    return userStats;
                }
                throw new ApiException(404, "Users stats not found");
            } catch (PersistenceException e){
                throw new ApiException(500, "Get user stats failed: " + e.getMessage());
            }
        }
    }

    public List<UserStats> getAll(){
        try(EntityManager em = emf.createEntityManager()){
            try{
                TypedQuery<UserStats> query = em.createQuery("SELECT us FROM UserStats us", UserStats.class);
                return query.getResultList();
            } catch (PersistenceException e){
                throw new ApiException(500, "Get all users stats failed: " + e.getMessage());
            }
        }
    }

    //skal måske refactores pga mmr, da det kræver begge spillerens nuværende mmr for at beregne det nye
    public UserStats recordResult(Integer id, Result result){
        if (id == null){
            throw new ApiException(400, "Provided userId is null");
        }
        if (result == null){
            throw new NullPointerException("Provided result is null");
        }
        UserStats userStats;
        try(EntityManager em = emf.createEntityManager()){
            em.getTransaction().begin();
            try{
                userStats = em.find(UserStats.class, id);
                if (userStats == null){
                    throw new ApiException(404, "User not found");
                }
                userStats.recordResult(result);
                em.getTransaction().commit();
            } catch (PersistenceException e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw new ApiException(500, "Recording result failed: " + e.getMessage());
            } catch (RuntimeException e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw e;
            }
        }
        return userStats;
    }


}
