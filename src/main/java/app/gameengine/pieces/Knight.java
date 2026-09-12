package app.gameengine.pieces;

import app.gameengine.Direction;
import app.gameengine.Position;

import java.util.ArrayList;
import java.util.List;

public class Knight extends Piece{

    public Knight(boolean isWhite, String type){
        super(isWhite, type);
    }

    @Override
    public List<Position> getPossibleMoves(Board board, Position position) {

        ArrayList<Position> thisPiecePossibleMoves = new ArrayList<>();

        int x = position.getX();
        int y = position.getY();

        List<Direction> offsets = new ArrayList<>(List.of(
                //2 left 1 up
                new Direction(-2, -1),
                //2 left 1 down
                new Direction(-2, +1),
                //1 left 2 up
                new Direction(-1, -2),
                //1 left 2 down
                new Direction(-1, +2),
                //2right 1 up
                new Direction(+2, -1),
                //2right 1 down
                new Direction(+2, +1),
                //1 right 2 up
                new Direction(+1, -2),
                //1 right 2 down
                new Direction(+1, +2)
        ));

        thisPiecePossibleMoves.addAll(NoneSlidingMoveHelper.getPossiblePositions(board, this, x, y, offsets));

        return thisPiecePossibleMoves;
    }
}
