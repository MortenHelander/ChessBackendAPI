package app.gameengine.pieces;

import app.gameengine.Position;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.processing.Generated;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Pawn extends Piece{
    private boolean hasMoved;
    private boolean isEnPassantTakeable;

    public Pawn(boolean isWhite, String type, boolean hasMoved, boolean isEnPassantTakeable){
        super(isWhite, type);
        this.hasMoved = hasMoved;
        this.isEnPassantTakeable = isEnPassantTakeable;
    }

    @Override
    public List<Position> getPossibleMoves(Board board, Position position) {

        ArrayList<Position> thisPawnsPossibleMoves = new ArrayList<>();

        int x = position.getX();
        int y = position.getY();

        thisPawnsPossibleMoves.addAll(PawnMoveHelper.getPawnMoves(board, this, x, y));

        return thisPawnsPossibleMoves;
    }
}
