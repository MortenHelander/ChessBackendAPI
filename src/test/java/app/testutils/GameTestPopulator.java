package app.testutils;

import app.entities.*;
import app.entities.enums.Color;
import app.entities.enums.GameMode;
import app.entities.enums.PowerUpStatus;
import app.entities.enums.PowerUpType;
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
            PlayerPowerUp player1PowerUp1 = new PlayerPowerUp(PowerUpType.GOOD_AI, PowerUpStatus.AUTOMATICALLY_APPLIED);
            PlayerPowerUp player1PowerUp2 = new PlayerPowerUp(PowerUpType.GAIN_RANDOM_PIECE, PowerUpStatus.HELD);
            PlayerPowerUp player1PowerUp3 = new PlayerPowerUp(PowerUpType.KILL_RANDOM_PIECE, PowerUpStatus.HELD);
            p1.addPowerUp(player1PowerUp1);
            p1.addPowerUp(player1PowerUp2);
            p1.addPowerUp(player1PowerUp3);

            Player p2 = new Player(Color.BLACK, false);
            user1.addPlayer(p1);
            Game game1 = Game.newGame(GameMode.CLASSIC, p1, p2);
            Move move1 = new Move(Position.D7, Position.D5);
            Move move2 = new Move(Position.A2, Position.A3);
            Move move3 = new Move(Position.H8, Position.G6);
            Move move4 = new Move(Position.H4, Position.G1);
            game1.addMoveAndShiftTurn(move1);
            game1.addMoveAndShiftTurn(move2);
            game1.addMoveAndShiftTurn(move3);
            game1.addMoveAndShiftTurn(move4);

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
