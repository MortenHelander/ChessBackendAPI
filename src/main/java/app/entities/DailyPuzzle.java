package app.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.time.LocalDate;
import java.util.List;

@Entity
public class DailyPuzzle {
    @Id
    private LocalDate id;
    private String fen;
    private List<String> solutionList;
    private long rating;
}
