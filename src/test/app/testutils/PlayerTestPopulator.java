package app.testutils;

import app.entities.Color;
import app.entities.Player;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceException;
import java.util.LinkedHashMap;
import java.util.Map;

public class PlayerTestPopulator {

    private PlayerTestPopulator() {}

    public static Map<String, Player> populate(EntityManagerFactory emf) {
        try (EntityManager em = emf.createEntityManager()) {

            em.getTransaction().begin();
            Player player1 = Player.builder().color(Color.BLACK).isAi(false).build();
            Player player2 = Player.builder().color(Color.WHITE).isAi(false).build();
            Player player3 = Player.builder().color(Color.BLACK).isAi(true).build();


            try {
                em.createNativeQuery("TRUNCATE TABLE players RESTART IDENTITY CASCADE").executeUpdate();
                em.persist(player1);
                em.persist(player2);
                em.persist(player3);
                em.flush();
            } catch (PersistenceException e) {
                if (em.getTransaction().isActive()) em.getTransaction().rollback();
                throw e;
            }
            em.getTransaction().commit();

            Map<String, Player> seeded = new LinkedHashMap<>();
            seeded.put("player1", player1);
            seeded.put("player2", player2);
            seeded.put("player3", player3);
            return seeded;
        }
    }
}
