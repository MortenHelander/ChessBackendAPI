package app.gameengine.pieces;

import app.gameengine.Direction;
import app.gameengine.Position;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Rook extends Piece{
    private boolean hasMoved;

    public Rook(boolean isWhite, String type, boolean hasMoved){
        super(isWhite, type);
        this.hasMoved = hasMoved;
    }

    @Override
    public List<Position> getPossibleMoves(Board board, Position position) {

        List<Position> thisPiecePossibleMoves = new ArrayList<>();

        int x = position.getX();
        int y = position.getY();

        //up
        Direction up = new Direction(0, -1);
        thisPiecePossibleMoves.addAll(SlidingMoveHelper.getPossiblePositions(board, this, x, y, up));

        //down
        Direction down = new Direction(0, +1);
        thisPiecePossibleMoves.addAll(SlidingMoveHelper.getPossiblePositions(board, this, x, y, down));

        //left
        Direction left = new Direction(-1, 0);
        thisPiecePossibleMoves.addAll(SlidingMoveHelper.getPossiblePositions(board, this, x, y, left));

        //right
        Direction right = new Direction(+1, 0);
        thisPiecePossibleMoves.addAll(SlidingMoveHelper.getPossiblePositions(board, this, x, y, right));

        return thisPiecePossibleMoves;
    }

}
