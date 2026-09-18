package app.gameengine.pieces;

import app.gameengine.Board;
import app.gameengine.move_logic.Direction;
import app.gameengine.Position;
import app.gameengine.move_logic.SlidingMoveHelper;

import java.util.ArrayList;
import java.util.List;

public class Bishop extends Piece {

    public Bishop(boolean isWhite, String type) {
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

        return thisPiecePossibleMoves;
    }
}
