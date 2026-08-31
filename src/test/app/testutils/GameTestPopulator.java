package app.testutils;

import app.entities.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceException;

import java.util.LinkedHashMap;
import java.util.Map;

public class GameTestPopulator {

    public static Map<String, Game> populate(EntityManagerFactory emf) {
        try (EntityManager em = emf.createEntityManager()) {

            em.getTransaction().begin();
            Game game1 = Game.newGame(GameMode.CLASSIC, new Player(Color.WHITE, false), new Player(Color.BLACK, false));
            Game game2 = Game.newGame(GameMode.TRAINING, new Player(Color.WHITE, false), new Player(Color.BLACK, true));
            Game game3 = Game.newGame(GameMode.FUN, new Player(Color.WHITE, false), new Player(Color.BLACK, false));

            try {
                em.createNativeQuery("TRUNCATE TABLE games RESTART IDENTITY CASCADE").executeUpdate();
                em.persist(game1);
                em.persist(game2);
                em.persist(game3);
                em.flush();
            } catch (PersistenceException e) {
                if (em.getTransaction().isActive()) em.getTransaction().rollback();
                throw e;
            }
            em.getTransaction().commit();

            Map<String, Game> seeded = new LinkedHashMap<>();
            seeded.put("game1", game1);
            seeded.put("game2", game2);
            seeded.put("game3", game3);
            return seeded;
        }
    }
}
