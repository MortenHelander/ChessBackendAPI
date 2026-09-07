package app.entities;

import app.entities.enums.PowerUpStatus;
import app.entities.enums.PowerUpType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.util.Objects;

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
    @ToString.Exclude
    private Player player;

    public PlayerPowerUp(PowerUpType type, PowerUpStatus status) {
        this.type = type;
        this.status = status;
    }

    public void updateStatus(){
        if (this.status == PowerUpStatus.HELD){
            this.status = PowerUpStatus.USED;
        } else if (this.status == PowerUpStatus.AUTOMATICALLY_APPLIED){
            this.status = PowerUpStatus.AUTOMATICALLY_DISCARDED;
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
        PlayerPowerUp playerPowerUp = (PlayerPowerUp) o;
        return getId() != null && Objects.equals(getId(), playerPowerUp.getId());
    }
}
