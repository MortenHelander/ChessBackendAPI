package app.testutils;

import app.entities.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceException;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

public final class UserTestPopulator {

    private UserTestPopulator() {}

    public static Map<String, User> populate(EntityManagerFactory emf) {
        try (EntityManager em = emf.createEntityManager()) {

            em.getTransaction().begin();
            User user1 = new User("Morten", "Helander", "morten.helander@hotmail.com", "Sheriff", "password123");
            User user2 = new User("Theis", "Rudkjær", "theis@hotmail.com", "SVG-man", "anotherpassword1441");
            User user3 = new User("Andreas", "Jensen", "andreas@hotmail.com", "Frontjoe", "andreas1234567");


            try {
                em.createNativeQuery("TRUNCATE TABLE users RESTART IDENTITY CASCADE").executeUpdate();
                em.persist(user1);
                em.persist(user2);
                em.persist(user3);
                em.flush();
            } catch (PersistenceException e) {
                if (em.getTransaction().isActive()) em.getTransaction().rollback();
                throw e;
            }
            em.getTransaction().commit();

            Map<String, User> seeded = new LinkedHashMap<>();
            seeded.put("user1", user1);
            seeded.put("user2", user2);
            seeded.put("user3", user3);
            return seeded;
        }
    }
}
