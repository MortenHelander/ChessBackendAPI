package app.entities;

import app.entities.enums.PuzzleStatus;
import app.exceptions.ApiException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.proxy.HibernateProxy;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(
        name = "puzzle_attempts",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "daily_puzzle_id"})
)
@NoArgsConstructor
@Getter
public class DailyPuzzleAttempt {
    private static final int MAX_TRIES = 10;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private int moveIndex;
    private int attemptsLeft;
    @Enumerated(EnumType.STRING)
    private PuzzleStatus status;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "daily_puzzle_id", nullable = false)
    @Setter
    private DailyPuzzle dailyPuzzle;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    @Setter
    private User user;
    @Version
    @Column(name = "optlock", nullable = false)
    private long version = 0;

    public static DailyPuzzleAttempt startNewPuzzle(DailyPuzzle dailyPuzzle, User user){
        DailyPuzzleAttempt attempt = new DailyPuzzleAttempt();
        attempt.moveIndex = 0;
        attempt.attemptsLeft = MAX_TRIES;
        attempt.status = PuzzleStatus.IN_PROGRESS;
        attempt.startedAt = LocalDateTime.now();
        user.addPuzzleAttempt(attempt);
        dailyPuzzle.addPuzzleAttempt(attempt);
        return attempt;
    }

    public void removeOneAttempt(){
        checkStatus();
        this.attemptsLeft--;
        if (attemptsLeft <= 0){
            markAsFailed();
        }
    }

    public void advance(){
        checkStatus();
        moveIndex = moveIndex + 2;
    }

    public void markAsSolved(){
        checkStatus();
        this.moveIndex = dailyPuzzle.getSolution().size();
        this.status = PuzzleStatus.SOLVED;
        this.finishedAt = LocalDateTime.now();
    }

    public void markAsFailed(){
        checkStatus();
        this.status = PuzzleStatus.FAILED;
        this.finishedAt = LocalDateTime.now();
    }

    private void checkStatus(){
        if (status != PuzzleStatus.IN_PROGRESS){
            throw new ApiException(400, "Puzzle attempt is already finished");
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
        DailyPuzzleAttempt puzzleAttempt = (DailyPuzzleAttempt) o;
        return getId() != null && Objects.equals(getId(), puzzleAttempt.getId());
    }

    @Override
    public final int hashCode() {
        return getClass().hashCode();
    }
}
