package app.gameengine.exceptions;

public class InvalidPromotionTypeException extends InvalidGameActionException {

    public InvalidPromotionTypeException(String pieceType) {
        super("Unknown promotion piece type: " + pieceType);
    }
}