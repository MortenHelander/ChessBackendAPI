package app.testutils;

import app.entities.User;
import app.entities.UserStats;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceException;
import java.util.LinkedHashMap;
import java.util.Map;

public final class UserTestPopulator {

    private UserTestPopulator() {}

    public static Map<String, User> populate(EntityManagerFactory emf) {
        try (EntityManager em = emf.createEntityManager()) {

            em.getTransaction().begin();
            User user1 = new User("sheriffen", "morten@hotmail.com", "Morten", "Helander", "password123");
            user1.addUserStats(UserStats.builder()
                    .gamesPlayed(0)
                    .wins(0)
                    .losses(0)
                    .mmr(1000)
                    .build());
            User user2 = new User("svg-man", "theis@hotmail.com", "Theis", "Rudkjær", "anotherpassword1441");
            user2.addUserStats(UserStats.builder()
                    .gamesPlayed(25)
                    .wins(12)
                    .losses(13)
                    .mmr(900)
                    .build());
            User user3 = new User("frontjoe", "andreas@hotmail.com", "Andreas", "Jensen", "andreas1234567");
            user3.addUserStats(UserStats.builder()
                    .gamesPlayed(200)
                    .wins(150)
                    .losses(50)
                    .mmr(1500)
                    .build());


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
