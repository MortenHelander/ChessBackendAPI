package app.dtos.lichess;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties
public record GameDTO(String id) {
}
