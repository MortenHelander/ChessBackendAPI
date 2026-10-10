package app.daos;

import app.entities.User;
import app.entities.UserStats;
import app.exceptions.ApiException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class UserDAO implements IDAO<User, Integer> {

    private EntityManagerFactory emf;

    public UserDAO(EntityManagerFactory emf) {
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
                if (isUsernameTaken(em, user.getUsername())) {
                    throw new ApiException(409, "Username is taken");
                }
                if (isEmailTaken(em, user.getEmail())) {
                    throw new ApiException(409, "Email is taken");
                }
                user.addUserStats(UserStats.builder()
                        .gamesPlayed(0)
                        .wins(0)
                        .losses(0)
                        .draws(0)
                        .mmr(500)
                        .build());
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
        if (id == null) {
            throw new ApiException(400, "UserID is required");
        }
        try (EntityManager em = emf.createEntityManager()) {
            try {
                User user = em.find(User.class, id);
                if (user != null) {
                    return user;
                }
                throw ApiException.notFound("User", id);
            } catch (PersistenceException e) {
                throw new ApiException(500, "Get user failed: " + e.getMessage());
            }
        }
    }

    public User getByUsername(String username) {
        try (EntityManager em = emf.createEntityManager()) {
            TypedQuery<User> query = em.createQuery("SELECT u FROM User u WHERE u.username = :username", User.class);
            query.setParameter("username", username);
            return query.getSingleResult();
        } catch (PersistenceException e) {
            return null;
        }
    }

    @Override
    public List<User> getAll() {
        try (EntityManager em = emf.createEntityManager()) {
            try {
                TypedQuery<User> query = em.createQuery("SELECT u FROM User u", User.class);
                return query.getResultList();
            } catch (PersistenceException e) {
                throw new ApiException(500, "Get all users failed: " + e.getMessage());
            }
        }
    }

    @Override
    public User update(User user) {
        if (user == null) {
            throw new ApiException(400, "Provided user is null");
        } else if (user.getId() == null) {
            throw new ApiException(400, "UserID is required");
        }
        User updated;
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            try {
                User existing = em.find(User.class, user.getId());
                if (existing == null) {
                    throw ApiException.notFound("User", user.getId());
                }
                if (isUsernameTakenByOther(em, user.getUsername(), user.getId())) {
                    throw new ApiException(409, "Username is taken");
                }
                if (isEmailTakenByOther(em, user.getEmail(), user.getId())) {
                    throw new ApiException(409, "Email is taken");
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
        if (id == null) {
            throw new ApiException(400, "UserID is required");
        }
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            try {
                User userToRemove = em.find(User.class, id);
                if (userToRemove != null) {
                    em.remove(userToRemove);
                } else {
                    throw ApiException.notFound("User", id);
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

    private boolean isUsernameTaken(EntityManager em, String username) {
        try {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(u) FROM User u WHERE u.username = :value", Long.class);
            query.setParameter("value", username);
            return query.getSingleResult() > 0;
        } catch (PersistenceException e) {
            throw new ApiException(500, "Check for username failed: " + e.getMessage());
        }
    }

    private boolean isEmailTaken(EntityManager em, String email) {
        try {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(u) FROM User u WHERE u.email = :value", Long.class);
            query.setParameter("value", email);
            return query.getSingleResult() > 0;
        } catch (PersistenceException e) {
            throw new ApiException(500, "Check for email failed: " + e.getMessage());
        }
    }

    private boolean isUsernameTakenByOther(EntityManager em, String username, Integer id) {
        try {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(u) FROM User u WHERE u.username = :value AND u.id <> :id", Long.class);
            query.setParameter("value", username);
            query.setParameter("id", id);
            return query.getSingleResult() > 0;
        } catch (PersistenceException e) {
            throw new ApiException(500, "Check for username failed: " + e.getMessage());
        }
    }

    private boolean isEmailTakenByOther(EntityManager em, String email, Integer id) {
        try {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(u) FROM User u WHERE u.email = :value AND u.id <> :id", Long.class); //<> taken by someone other than yourself
            query.setParameter("value", email);
            query.setParameter("id", id);
            return query.getSingleResult() > 0;
        } catch (PersistenceException e) {
            throw new ApiException(500, "Check for email failed: " + e.getMessage());
        }
    }
}
