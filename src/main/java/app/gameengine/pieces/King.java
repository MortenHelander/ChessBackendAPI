package app.gameengine.pieces;

import app.gameengine.Direction;
import app.gameengine.Position;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class King extends Piece{
    private boolean hasMoved;

    public King(boolean isWhite, String type, boolean hasMoved){
        super(isWhite, type);
        this.hasMoved = hasMoved;
    }

    @Override
    public List<Position> getPossibleMoves(Board board, Position position) {

        ArrayList<Position> thisPiecePossibleMoves = new ArrayList<>();

        int x = position.getX();
        int y = position.getY();

        List<Direction> offsets = new ArrayList<>(List.of(
                //left
                new Direction(-1, 0),
                //right
                new Direction(+1, 0),
                //up
                new Direction(0, -1),
                //down
                new Direction(0, +1),
                //up left
                new Direction(-1, -1),
                //down left
                new Direction(-1, +1),
                //up right
                new Direction(+1, -1),
                //down right
                new Direction(+1, +1)
        ));

        thisPiecePossibleMoves.addAll(NoneSlidingMoveHelper.getPossiblePositions(board, this, x, y, offsets));

        return thisPiecePossibleMoves;
    }
}
