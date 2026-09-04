package app.dtos.chesscom;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class ChessComPlayerDTO {
    private String username;
    private String name;
    private String title;
    private String league;
}
