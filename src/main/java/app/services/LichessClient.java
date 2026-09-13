package app.services;

import app.utils.Utils;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class LichessClient {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private String userAgent = Utils.getPropertyValue("USER_AGENT", "config.properties");

    public String getJsonPlayerInfo(){

        String endpoint = "https://lichess.org/api/puzzle/daily";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("User-Agent", userAgent)
                .GET()
                .build();

        HttpClient client = HttpClient.newHttpClient();

        HttpResponse<String> response;
        try {
            response = client.send
                    (request, HttpResponse.BodyHandlers.ofString()
                    );
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println(response.body());
        return response.body();
    }
}
