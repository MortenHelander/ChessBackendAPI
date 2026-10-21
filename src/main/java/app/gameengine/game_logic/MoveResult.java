package app.gameengine.game_logic;

import app.gameengine.Position;

public record MoveResult(boolean success, boolean isAwaitingPromotion, Position promotionSquare) {

    public static MoveResult completed(){
        return new MoveResult(true, false, null);
    }
    public static MoveResult awaitingPromotion(Position promotionSquare){
        return new MoveResult(true, true, promotionSquare);
    }
    public static MoveResult failed(){
        return new MoveResult(false, false, null);
    }
}
