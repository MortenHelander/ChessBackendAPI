package app.gameengine.exceptions;

public abstract class InvalidGameActionException extends RuntimeException {
    protected InvalidGameActionException(String message) {
        super(message);
    }
}