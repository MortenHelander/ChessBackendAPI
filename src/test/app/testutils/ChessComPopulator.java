package app.testutils;

import app.entities.Color;
import app.entities.Game;
import app.entities.GameMode;
import app.entities.Player;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.Map;

public class ChessComPopulator {
    public static Map<String, Game> populate(EntityManagerFactory emf) {
        try (EntityManager em = emf.createEntityManager()) {

            em.getTransaction().begin();
            Game game1 = Game.newGame(GameMode.CLASSIC, new Player(Color.WHITE, false), new Player(Color.BLACK, false));
            Game game2 = Game.newGame(GameMode.TRAINING, new Player(Color.WHITE, false), new Player(Color.BLACK, true));
            Game game3 = Game.newGame(GameMode.FUN, new Player(Color.WHITE, false), new Player(Color.BLACK, false));
}
