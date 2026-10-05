package app.dtos.lichess;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties
public record LichessDailyPuzzleDTO(GameDTO game, Puzzle puzzle) {
}
