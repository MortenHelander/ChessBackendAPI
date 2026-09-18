package app.services;

import app.dtos.lichess.LichessDailyPuzzleDTO;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class LichessClientTest {

    String json = """
            {
              "game": {
                "id": "P3gwP0JC",
                "perf": {
                  "key": "rapid",
                  "name": "Rapid"
                },
                "rated": true,
                "players": [
                  {
                    "name": "masomehn",
                    "id": "masomehn",
                    "color": "white",
                    "rating": 1793
                  },
                  {
                    "name": "catafalque",
                    "id": "catafalque",
                    "color": "black",
                    "rating": 2001
                  }
                ],
                "pgn": "d4 Nf6 Nf3 g6 Nc3 d5 Bg5 Bg7 e3 Bg4 Be2 O-O Qd3 e6 h3 Bxf3 Bxf3 Nbd7 O-O a6 Ne2 c5 b3 cxd4 exd4 b5 Nf4 Qc7 Rfe1 Rfc8 Rac1 h6 Bxf6 Nxf6 Nxe6 fxe6 Qxg6 Qf7 Qd3 b4 Re3 Rc3 Qe2 Rxe3 Qxe3 Rc8 Qe2 Rc6 Qd3 Rc3 Qxa6 Qg6 Qxe6+ Kh7 Qe2 Ne4 Bg4 Bxd4 Qb5 Bxf2+ Kf1 Qf6 Qd7+ Kg6",
                "clock": "15+4"
              },
              "puzzle": {
                "id": "lpmqI",
                "rating": 1981,
                "plays": 109667,
                "solution": [
                  "g4h5",
                  "g6h5",
                  "d7g4"
                ],
                "themes": [
                  "mateIn2",
                  "middlegame",
                  "short",
                  "attraction",
                  "sacrifice"
                ],
                "fen": "8/3Q4/5qkp/3p4/1p2n1B1/1Pr4P/P1P2bP1/2R2K2 w - - 1 1",
                "lastMove": "h7g6",
                "initialPly": 63
              }
            }
            """;

    @Test
    void getDailyPuzzle(){
        LichessDailyPuzzleDTO dailyPuzzle = GenericClient.convertJsonGeneric(json, LichessDailyPuzzleDTO.class);

        assertThat(dailyPuzzle.getGame().id(), is("P3gwP0JC"));
        assertThat(dailyPuzzle.getPuzzle().id(), is("lpmqI"));
        assertThat(dailyPuzzle.getPuzzle().rating(), is(1981));
        assertThat(dailyPuzzle.getPuzzle().solution(), containsInRelativeOrder("g4h5", "g6h5", "d7g4"));
        assertThat(dailyPuzzle.getPuzzle().fen(), is("8/3Q4/5qkp/3p4/1p2n1B1/1Pr4P/P1P2bP1/2R2K2 w - - 1 1"));
        assertThat(dailyPuzzle.getPuzzle().lastMove(), is("h7g6"));
    }
}
