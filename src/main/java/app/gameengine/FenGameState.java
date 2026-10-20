package app.gameengine;

public record FenGameState(Board board, boolean isWhitesTurn, int halfMove, int fullMove) {
}
