package app.services;

import app.dtos.chesscom.ChessComPlayerDTO;
import app.dtos.chesscom.ChessComStatsDTO;
import app.dtos.chesscom.records.Best;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.is;

class APIReaderTest {
    private APIReader apiReader = new APIReader();
    private String playerName = "hikaru";


    @Test
    void getJsonPlayerInfo() {

        String json = apiReader.getJsonPlayerInfo(playerName, false);

        assertThat(json, containsStringIgnoringCase(playerName));
    }

    @Test
    void getPlayerInfoChessCom() {

        String json = apiReader.getJsonPlayerInfo(playerName, false);
        ChessComPlayerDTO hikaru = apiReader.getPlayerInfoChessCom(json);

        assertThat(hikaru.getUsername(), is(playerName));
        assertThat(hikaru.getName(), is("Hikaru Nakamura"));
        assertThat(hikaru.getTitle(), is("GM"));
        assertThat(hikaru.getLeague(), is("Legend"));

    }

    @Test
    void getPlayerStatsChessCom(){

        String json = apiReader.getJsonPlayerInfo(playerName, true);

        ChessComStatsDTO hikaruStats = apiReader.getPlayerStatsChessCom(json);

        assertThat(hikaruStats, not(null));
        assertThat(hikaruStats, samePropertyValuesAs();
    }
}