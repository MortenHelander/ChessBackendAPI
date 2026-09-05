package app.testutils;

import app.entities.*;
import app.entities.enums.Color;
import app.entities.enums.GameMode;
import app.gameengine.Position;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceException;

import java.util.LinkedHashMap;
import java.util.Map;

public class GameTestPopulator {

    public static Map<String, Game> populate(EntityManagerFactory emf) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            em.createNativeQuery("TRUNCATE TABLE moves, players, games, user_stats, users RESTART IDENTITY CASCADE")
                    .executeUpdate();

            User user1 = new User("Morten", "Helander", "morten@hotmail.com", "Sheriff", "password123");
            em.persist(user1);

            Player p1 = new Player(Color.WHITE, false);
            Player p2 = new Player(Color.BLACK, false);
            user1.addPlayer(p1);
            Game game1 = Game.newGame(GameMode.CLASSIC, p1, p2);

            Player p3 = new Player(Color.WHITE, false);
            Player p4 = new Player(Color.BLACK, true);
            user1.addPlayer(p3);
            Game game2 = Game.newGame(GameMode.TRAINING, p3, p4);

            Player p5 = new Player(Color.WHITE, false);
            Player p6 = new Player(Color.BLACK, false);
            user1.addPlayer(p5);
            Game game3 = Game.newGame(GameMode.FUN, p5, p6);

            // Only persist the games — cascade handles the players
            em.persist(game1);
            em.persist(game2);
            em.persist(game3);

            em.getTransaction().commit();

            Map<String, Game> seeded = new LinkedHashMap<>();
            seeded.put("game1", game1);
            seeded.put("game2", game2);
            seeded.put("game3", game3);
            return seeded;
        }
    }
}
