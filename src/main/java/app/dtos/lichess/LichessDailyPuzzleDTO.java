package app.dtos.lichess;

import app.dtos.lichess.records.GameDTO;
import app.dtos.lichess.records.Puzzle;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

@Getter
@JsonIgnoreProperties
public class LichessDailyPuzzleDTO {

    private GameDTO game;
    private Puzzle puzzle;
}
