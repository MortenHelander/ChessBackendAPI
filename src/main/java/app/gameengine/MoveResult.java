package app.gameengine;

public record MoveResult(boolean success, boolean isAwaitingPromotion, Position promotionSquare) {

    static MoveResult completed(){
        return new MoveResult(true, false, null);
    }
    static MoveResult awaitingPromotion(Position promotionSquare){
        return new MoveResult(true, true, promotionSquare);
    }
    static MoveResult failed(){
        return new MoveResult(false, false, null);
    }
}
