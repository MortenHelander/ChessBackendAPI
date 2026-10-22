package app.clients;
import app.dtos.lichess.LichessDailyPuzzleDTO;
import app.exceptions.ApiException;
import app.utils.Utils;
import tools.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class LichessClient {
    private final String userAgent = Utils.getPropertyValue("USER_AGENT", "config.properties");
    private final HttpClient client = HttpClient.newHttpClient();

    public LichessDailyPuzzleDTO getDailyPuzzle(){

        String endpoint = "https://lichess.org/api/puzzle/daily";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("User-Agent", userAgent)
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        HttpResponse<String> response;
        try {
            response = client.send
                    (request, HttpResponse.BodyHandlers.ofString()
                    );
        }catch (InterruptedException e) {
            Thread.currentThread().interrupt(); //makes sure that the thread calling this sees the interrupted
            throw new ApiException(502, "Interrupted while calling Lichess");
        }catch (IOException e) {
            throw new ApiException(502, "Could not reach Lichess: " + e.getMessage());
        }

        if (response.statusCode() != 200){
            throw new ApiException(502, "Lichess responded with " + response.statusCode());
        }
        return GenericClient.convertJsonGeneric(response.body(), LichessDailyPuzzleDTO.class);
    }
}
