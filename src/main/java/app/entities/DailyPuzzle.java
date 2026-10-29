package app.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.proxy.HibernateProxy;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Entity
@Getter
@Table(name = "daily_puzzles")
@NoArgsConstructor
@AllArgsConstructor
public class DailyPuzzle {
    @Id
    private LocalDate id;
    private String lichessPuzzleId;
    private int rating;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "daily_puzzles_solutions",
            joinColumns = @JoinColumn(name = "daily_puzzle_id"))
    @OrderColumn(name = "move_order")
    @Column(name = "uci_move")
    private List<String> solution;
    private String fen;
    private String lastMove;
    @OneToMany(mappedBy = "dailyPuzzle")
    private Set<DailyPuzzleAttempt> puzzleAttempts = new HashSet<>();

    public DailyPuzzle(LocalDate id, String lichessPuzzleId, int rating, List<String> solution, String fen, String lastMove) {
        this.id = id;
        this.lichessPuzzleId = lichessPuzzleId;
        this.rating = rating;
        this.solution = solution;
        this.fen = fen;
        this.lastMove = lastMove;
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
        DailyPuzzle dailyPuzzle = (DailyPuzzle) o;
        return getId() != null && Objects.equals(getId(), dailyPuzzle.getId());
    }

    @Override
    public final int hashCode() {
        return getClass().hashCode();
    }
}
