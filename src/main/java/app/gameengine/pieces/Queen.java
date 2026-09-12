package app.gameengine.pieces;

import app.gameengine.Direction;
import app.gameengine.Position;

import java.util.ArrayList;
import java.util.List;

public class Queen extends Piece{

    public Queen(boolean isWhite, String type){
        super(isWhite, type);
    }

    @Override
    public List<Position> getPossibleMoves(Board board, Position position) {


        List<Position> thisPiecePossibleMoves = new ArrayList<>();

        int x = position.getX();
        int y = position.getY();

        //up left
        Direction upLeft = new Direction(-1, -1);
        thisPiecePossibleMoves.addAll(SlidingMoveHelper.getPossiblePositions(board, this, x, y, upLeft));

        //down left
        Direction downLeft = new Direction(-1, +1);
        thisPiecePossibleMoves.addAll(SlidingMoveHelper.getPossiblePositions(board, this, x, y, downLeft));

        //up right
        Direction upRight = new Direction(+1, -1);
        thisPiecePossibleMoves.addAll(SlidingMoveHelper.getPossiblePositions(board, this, x, y, upRight));

        //down right
        Direction downRight = new Direction(+1, +1);
        thisPiecePossibleMoves.addAll(SlidingMoveHelper.getPossiblePositions(board, this, x, y, downRight));

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
