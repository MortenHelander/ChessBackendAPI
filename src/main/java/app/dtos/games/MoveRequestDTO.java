package app.dtos.games;

import org.jetbrains.annotations.Nullable;

public record MoveRequestDTO(String from, String to, @Nullable String promotionLetter) {
}
