package app.daos;

import app.entities.User;
import app.exceptions.ApiException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class UserDAO implements IDAO<User, Integer>{

    private EntityManagerFactory emf;

    public UserDAO(EntityManagerFactory emf){
        this.emf = emf;
    }

    @Override
    public User create(User user) {
        if (user == null) {
            throw new ApiException(400, "Provided user is null");
        }
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            try {
                em.persist(user);
                em.getTransaction().commit();
            } catch (PersistenceException e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw new ApiException(500, "Creation of user failed: " + e.getMessage());
            } catch (RuntimeException e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw e;
            }
            return user;
        }
    }

    @Override
    public User getById(Integer id) {
        if (id == null){
            throw new ApiException(400, "UserID is required");
        }
        try(EntityManager em = emf.createEntityManager()){
            try {
                User user = em.find(User.class, id);
                if (user != null) {
                    return user;
                }
                throw new ApiException(404, "User not found");
            } catch (PersistenceException e){
                throw new ApiException(500, "Get user failed: " + e.getMessage());
            }
        }
    }

    @Override
    public List<User> getAll() {
        try(EntityManager em = emf.createEntityManager()){
            try{
                TypedQuery<User> query = em.createQuery("SELECT u FROM User u", User.class);
                return query.getResultList();
            } catch (PersistenceException e){
                throw new ApiException(500, "Get users failed: " + e.getMessage());
            }
        }
    }

    @Override
    public User update(User user) {
        if (user == null){
            throw new ApiException(400, "Provided user is null");
        } else if (user.getId() == null){
            throw new ApiException(400, "UserID is required");
        }
        User updated;
        try(EntityManager em = emf.createEntityManager()){
            em.getTransaction().begin();
            try{
                User existing = em.find(User.class, user.getId());
                if (existing == null){
                    throw new ApiException(404, "User not found");
                }
                updated = em.merge(user);
                em.getTransaction().commit();
            } catch (PersistenceException e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw new ApiException(500, "Update user failed: " + e.getMessage());
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
                User userToRemove = em.find(User.class, id);
                if (userToRemove != null){
                    em.remove(userToRemove);
                } else {
                    throw new ApiException(404, "User not found");
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
