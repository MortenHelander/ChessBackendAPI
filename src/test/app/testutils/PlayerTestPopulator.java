package app.testutils;

import app.entities.Player;
import app.entities.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceException;

import java.util.LinkedHashMap;
import java.util.Map;

public class PlayerTestPopulator {

//    private PlayerTestPopulator() {}
//
//    public static Map<String, User> populate(EntityManagerFactory emf) {
//        try (EntityManager em = emf.createEntityManager()) {
//
//            em.getTransaction().begin();
//            Player player1 = new Player("Morten", "Helander", "morten.helander@hotmail.com", "Sheriff", "password123");
//            Player player2 = new Player("Theis", "Rudkjær", "theis@hotmail.com", "SVG-man", "anotherpassword1441");
//            Player player3 = new Player("Andreas", "Jensen", "andreas@hotmail.com", "Frontjoe", "andreas1234567");
//
//
//            try {
//                em.createNativeQuery("TRUNCATE TABLE players RESTART IDENTITY CASCADE").executeUpdate();
//                em.persist(user1);
//                em.persist(user2);
//                em.persist(user3);
//                em.flush();
//            } catch (PersistenceException e) {
//                if (em.getTransaction().isActive()) em.getTransaction().rollback();
//                throw e;
//            }
//            em.getTransaction().commit();
//
//            Map<String, User> seeded = new LinkedHashMap<>();
//            seeded.put("user1", user1);
//            seeded.put("user2", user2);
//            seeded.put("user3", user3);
//            return seeded;
//        }
//    }
}
