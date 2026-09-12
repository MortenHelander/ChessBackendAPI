package app.gameengine.pieces;

import app.gameengine.Position;
import lombok.Getter;

import java.util.List;

@Getter
public abstract class Piece {
    private final boolean isWhite;
    private final String type;



    public Piece(boolean isWhite, String type) {
        this.isWhite = isWhite;
        this.type = type;
    }

    public abstract List<Position> getPossibleMoves(Board board, Position position);

}
