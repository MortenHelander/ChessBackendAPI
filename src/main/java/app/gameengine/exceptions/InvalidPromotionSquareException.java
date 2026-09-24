package app.gameengine.exceptions;

import app.gameengine.Position;

public class InvalidPromotionSquareException extends InvalidGameActionException{

    public InvalidPromotionSquareException(Position position) {
        super("Invalid promotion square: " + position + " ( must be rank 1 or rank 8)");
    }
}
