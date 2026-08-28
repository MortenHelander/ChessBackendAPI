package app.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "moves")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Getter
public class Move {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private int moveNumber;
    private String uci;
    private LocalDateTime playedAt;
    @ManyToOne
    @Setter
    private Game game;
}
