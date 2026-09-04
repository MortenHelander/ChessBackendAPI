package app.dtos.chesscom;

import app.dtos.chesscom.records.Blitz;
import app.dtos.chesscom.records.Bullet;
import app.dtos.chesscom.records.Daily;
import app.dtos.chesscom.records.Rapid;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChessComStatsDTO {

    @JsonProperty("chess_daily")
    private Daily daily;
    @JsonProperty("chess_rapid")
    private Rapid rapid;
    @JsonProperty("chess_bullet")
    private Bullet bullet;
    @JsonProperty("chess_blitz")
    private Blitz blitz;

    @Override
    public String toString() {
        return "Stats:\n" + daily +
                ",\n" + rapid +
                ",\n" + bullet +
                ",\n" + blitz;
    }
}
