package app.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "player_power_ups")
@NoArgsConstructor
@Builder
@AllArgsConstructor
@ToString
@Getter
public class PlayerPowerUp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Enumerated(value = EnumType.STRING)
    private PowerUpType type;
    @Enumerated(value = EnumType.STRING)
    private PowerUpStatus status;

    @ManyToOne
    @Setter
    private Player player;
}
