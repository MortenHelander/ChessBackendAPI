package app.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
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
    private boolean isWhite;
    private boolean isAi;
    @ManyToOne
    @Setter
    private User user;
    @ManyToOne
    @Setter
    private Game game;
    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<PowerUp> powerUps = new HashSet<>();

    public void addPowerUp(PowerUp powerUp){
        this.powerUps.add(powerUp);
        if (powerUp != null){
            powerUp.setPlayer(this);
        }
    }
}
