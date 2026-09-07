package app.entities;

import app.entities.enums.Color;
import app.entities.enums.GameMode;
import app.entities.enums.GameStatus;
import app.exceptions.ApiException;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Entity
@Table(name = "games")
@NoArgsConstructor
@ToString
@Getter
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private LocalDateTime startedAt;
    @Enumerated(value = EnumType.STRING)
    private GameMode gameMode;
    @Enumerated(value = EnumType.STRING)
    private GameStatus gameStatus;
    @Enumerated(value = EnumType.STRING)
    private Color winnerColor;
    private boolean isWhitesTurn;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("moveNumber ASC")
    @ToString.Exclude
    private List<Move> moves = new ArrayList<>();

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "game", cascade = CascadeType.ALL)
    private Set<Player> players = new HashSet<>();


    public static Game newGame(GameMode gameMode, Player whitePlayer, Player blackPlayer){
        if (whitePlayer.getColor() != Color.WHITE || blackPlayer.getColor() != Color.BLACK){
            throw new ApiException(400, "Players must be assigned WHITE and BLACK respectively");
        }
        Game game = new Game();
        game.gameMode = gameMode;
        game.gameStatus = GameStatus.IN_PROGRESS;
        game.startedAt = LocalDateTime.now();
        game.isWhitesTurn = true;
        game.addPlayer(whitePlayer);
        game.addPlayer(blackPlayer);
        return game;
    }

    public void addMoveAndShiftTurn(Move move){
        this.moves.add(move);
        isWhitesTurn = !isWhitesTurn;
        if (move != null){
            move.setGame(this);
            move.setMoveNumber(moves.size());
            //java default is nanosecond (9 decimals) while postgres stores with only 6 decimals, so cutting off for similarity and testing
            move.setPlayedAt(LocalDateTime.now().truncatedTo(ChronoUnit.MICROS));

            //gets the names of positions to use for stockfish api
            move.setUci(move.getFrom().name().toLowerCase() + move.getTo().name().toLowerCase());
        }
    }

    public void finishGame(GameStatus status, Color winnerColor){
        if (this.gameStatus != GameStatus.IN_PROGRESS){
            throw new ApiException(400, "Game is already finished");
        }
        this.gameStatus = status;
        this.winnerColor = winnerColor;
    }


    private void addPlayer(Player player){
        this.players.add(player);
        if (player != null){
            player.setGame(this);
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
        Game game = (Game) o;
        return getId() != null && Objects.equals(getId(), game.getId());
    }

    @Override
    public final int hashCode() {
        return getClass().hashCode();
    }
}
