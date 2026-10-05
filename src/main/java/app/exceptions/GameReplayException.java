package app.exceptions;

public class GameReplayException extends RuntimeException {
    public GameReplayException(String message) {
        super(message);
    }
    public GameReplayException(String message, Throwable cause) {
        super(message, cause);
    }
}