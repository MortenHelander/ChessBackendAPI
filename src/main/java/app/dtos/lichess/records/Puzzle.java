package app.dtos.lichess.records;

import java.util.List;

public record Puzzle(int id, long rating, List<String> solution, String fen, String lastMove) {
}
