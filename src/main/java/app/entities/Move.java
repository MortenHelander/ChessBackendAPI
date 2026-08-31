package app.entities;

import app.gameengine.Position;
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
    @Setter
    private int moveNumber;
    private String uci;
    private LocalDateTime playedAt;
    private Position from;
    private Position to;
    @ManyToOne
    @Setter
    private Game game;


}
