package app.dtos.chesscom.records;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Record(int win, int loss, int draw) {
}
