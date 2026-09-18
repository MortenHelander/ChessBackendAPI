package app.dtos.lichess.records;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
@JsonIgnoreProperties
public record Puzzle(String id, int rating, List<String> solution, String fen, String lastMove) {
}
