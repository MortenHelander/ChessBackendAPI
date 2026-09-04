package app.services;

import app.dtos.chesscom.ChessComPlayerDTO;
import app.dtos.chesscom.ChessComStatsDTO;
import app.utils.Utils;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class APIReader {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private String userAgent = Utils.getPropertyValue("USER_AGENT", "config.properties");


    public String getJsonPlayerInfo(String playerName, boolean needStats){

        String stats = "";
        if (needStats){
            stats = "/stats";
        }

        String endpoint = "https://api.chess.com/pub/player/" + playerName + stats;

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

    public ChessComPlayerDTO getPlayerInfoChessCom(String json){
        try{
            return objectMapper.readValue(json, ChessComPlayerDTO.class);
        } catch (JacksonException e) {
            throw new RuntimeException(e);
        }
    }

    public ChessComStatsDTO getPlayerStatsChessCom(String json){
        try{
            return objectMapper.readValue(json, ChessComStatsDTO.class);
        } catch (JacksonException e) {
            throw new RuntimeException(e);
        }
    }
}
