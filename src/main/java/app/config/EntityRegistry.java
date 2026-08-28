package app.config;

import app.entities.*;
import org.hibernate.cfg.Configuration;

final class EntityRegistry {

    private EntityRegistry() {}

    static void registerEntities(Configuration configuration) {
        configuration.addAnnotatedClass(User.class);
        configuration.addAnnotatedClass(Player.class);
        configuration.addAnnotatedClass(Game.class);
        configuration.addAnnotatedClass(Move.class);
        configuration.addAnnotatedClass(PowerUp.class);
        configuration.addAnnotatedClass(UserStats.class);
        // TODO: Add more entities here...
    }
}