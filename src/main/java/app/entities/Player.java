package app.entities;

import app.entities.enums.Color;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "players")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Getter
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Enumerated(value = EnumType.STRING)
    private Color color;
    private boolean isAi;
    @ManyToOne
    @Setter
    private User user;
    @ToString.Exclude
    @ManyToOne
    @Setter
    private Game game;
    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<PlayerPowerUp> playerPowerUps = new HashSet<>();

    public Player(Color color, boolean isAi) {
        this();
        this.color = color;
        this.isAi = isAi;
    }

    public void addPowerUp(PlayerPowerUp playerPowerUp){
        this.playerPowerUps.add(playerPowerUp);
        if (playerPowerUp != null){
            playerPowerUp.setPlayer(this);
        }
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null)
            return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer()
                .getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer()
                .getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass)
            return false;
        Player player = (Player) o;
        return getId() != null && Objects.equals(getId(), player.getId());
    }

    @Override
    public final int hashCode() {
        return getClass().hashCode();
    }
}
