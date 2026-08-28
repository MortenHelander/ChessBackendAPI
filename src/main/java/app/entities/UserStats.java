package app.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_stats")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Getter
public class UserStats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Integer gamesPlayed;
    private Integer wins;
    private Integer losses;
    private double winrate;
    private double mmr;
    @OneToOne
    @MapsId
    @ToString.Exclude
    @Setter
    private User user;
}
