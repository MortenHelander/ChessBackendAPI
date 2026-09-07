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

    private String playerJsonResponse = """
            {
              "avatar": "https://images.chesscomfiles.com/uploads/v1/user/15448422.88c010c1.200x200o.3c5619f5441e.png",
              "player_id": 15448422,
              "@id": "https://api.chess.com/pub/player/hikaru",
              "url": "https://www.chess.com/member/Hikaru",
              "name": "Hikaru Nakamura",
              "username": "hikaru",
              "title": "GM",
              "followers": 1409190,
              "country": "https://api.chess.com/pub/country/US",
              "location": "Florida",
              "last_online": 1788489957,
              "joined": 1389043258,
              "status": "premium",
              "is_streamer": true,
              "twitch_url": "https://twitch.tv/gmhikaru",
              "verified": false,
              "league": "Legend",
              "streaming_platforms": [
                {
                  "type": "twitch",
                  "channel_url": "https://twitch.tv/gmhikaru"
                }
              ]
            }
            """;

    private String playerStatsJsonResponse = """
            {
              "chess_daily": {
                "last": {
                  "rating": 2239,
                  "date": 1770563021,
                  "rd": 103
                },
                "best": {
                  "rating": 2464,
                  "date": 1397136740,
                  "game": "https://www.chess.com/game/daily/84604826"
                },
                "record": {
                  "win": 73,
                  "loss": 11,
                  "draw": 4,
                  "time_per_move": 32755,
                  "timeout_percent": 0
                }
              },
              "chess960_daily": {
                "last": {
                  "rating": 1231,
                  "date": 1444458214,
                  "rd": 230
                },
                "best": {
                  "rating": 1489,
                  "date": 1397073007,
                  "game": "https://www.chess.com/game/daily/87191830"
                },
                "record": {
                  "win": 1,
                  "loss": 2,
                  "draw": 0,
                  "time_per_move": 32755,
                  "timeout_percent": 0
                }
              },
              "chess_rapid": {
                "last": {
                  "rating": 2838,
                  "date": 1786796329,
                  "rd": 44
                },
                "best": {
                  "rating": 2927,
                  "date": 1645902514,
                  "game": "https://www.chess.com/game/live/141332265952"
                },
                "record": {
                  "win": 205,
                  "loss": 69,
                  "draw": 220
                }
              },
              "chess_bullet": {
                "last": {
                  "rating": 3358,
                  "date": 1788026237,
                  "rd": 29
                },
                "best": {
                  "rating": 3570,
                  "date": 1605136047,
                  "game": "https://www.chess.com/game/live/5710095242"
                },
                "record": {
                  "win": 16820,
                  "loss": 2453,
                  "draw": 1141
                }
              },
              "chess_blitz": {
                "last": {
                  "rating": 3370,
                  "date": 1788124673,
                  "rd": 32
                },
                "best": {
                  "rating": 3469,
                  "date": 1785599066,
                  "game": "https://www.chess.com/game/live/162034628699"
                },
                "record": {
                  "win": 35610,
                  "loss": 5515,
                  "draw": 4341
                }
              },
              "fide": 2814,
              "tactics": {
                "highest": {
                  "rating": 2730,
                  "date": 1389043258
                },
                "lowest": {
                  "rating": 2730,
                  "date": 1389043258
                }
              },
              "puzzle_rush": {
                "best": {
                  "total_attempts": 126,
                  "score": 123
                }
              }
            }
            """;

    @Test
    void getPlayerInfoChessCom() {

        ChessComPlayerDTO hikaru = apiReader.getPlayerInfoChessCom(playerJsonResponse);

        assertThat(hikaru.getUsername(), is("hikaru"));
        assertThat(hikaru.getName(), is("Hikaru Nakamura"));
        assertThat(hikaru.getTitle(), is("GM"));
        assertThat(hikaru.getLeague(), is("Legend"));
    }

    @Test
    void getPlayerStatsChessCom(){

        ChessComStatsDTO hikaruStats = apiReader.getPlayerStatsChessCom(playerStatsJsonResponse);

        assertThat(hikaruStats, is(notNullValue()));
        assertThat(hikaruStats.getBlitz().best().rating(), is(3469));
        assertThat(hikaruStats.getBullet().last().rating(), is(3358));
        assertThat(hikaruStats.getRapid().record().win(), is(205));
    }
}