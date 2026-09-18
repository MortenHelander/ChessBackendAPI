package app.dtos.lichess.records;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties
public record GameDTO(String id) {
}
