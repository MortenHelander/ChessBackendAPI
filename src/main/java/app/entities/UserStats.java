package app.entities;

import app.entities.enums.Result;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.util.Objects;

@Entity
@Table(name = "user_stats")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Getter
public class UserStats {
    @Id
    @Column(name = "user_id")
    private Integer id;
    private Integer gamesPlayed;
    private Integer wins;
    private Integer losses;
    private Integer draws;
    private double mmr;
    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    @Setter
    private User user;

    public void recordResult(Result result){

        gamesPlayed ++;
        if (result == Result.WIN){
            wins++;
        } else if (result == Result.LOSS){
            losses ++;
        } else if (result == Result.DRAW){
            draws ++;
        }

        //call mmr service or whatever it's gonna be
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
        UserStats userStats = (UserStats) o;
        return getId() != null && Objects.equals(getId(), userStats.getId());
    }

    @Override
    public final int hashCode() {
        return getClass().hashCode();
    }
}
