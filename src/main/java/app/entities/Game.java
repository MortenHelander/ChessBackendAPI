package app.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "games")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Getter
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Enumerated(value = EnumType.STRING)
    private GameMode gameMode;
    @Enumerated(value = EnumType.STRING)
    private GameStatus gameStatus;
    @Enumerated(value = EnumType.STRING)
    private WinnerColor winnerColor;
    private boolean isWhitesTurn;
    private Integer totalMoves;

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<Move> moves = new HashSet<>();

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<Player> players = new HashSet<>();

    public void addMove(Move move){
        this.moves.add(move);
        if (move != null){
            move.setGame(this);
        }
    }

    public void addPlayer(Player player){
        this.players.add(player);
        if (player != null){
            player.setGame(this);
        }
    }
}
