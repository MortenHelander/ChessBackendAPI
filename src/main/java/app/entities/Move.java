package app.entities;

import app.gameengine.Position;
import jakarta.persistence.*;
import jdk.jfr.Name;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "moves")
@NoArgsConstructor
@ToString
@Getter
@Builder
@AllArgsConstructor
public class Move {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Setter
    private int moveNumber;
    @Setter
    private String uci;
    @Setter
    private LocalDateTime playedAt;
    @Column(name = "from_position")
    private Position from;
    @Column(name = "to_position")
    private Position to;
    @ManyToOne
    @ToString.Exclude
    @Setter
    private Game game;

    public Move(Position from, Position to) {
        this.from = from;
        this.to = to;
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
        Move move = (Move) o;
        return getId() != null && Objects.equals(getId(), move.getId());
    }
}
