package app.gameengine.move_logic;

import app.gameengine.Board;
import app.gameengine.Position;
import app.gameengine.pieces.King;
import app.gameengine.pieces.Piece;

import java.util.Map;
public class PieceFinder{

    public static Piece findPiece(Board board, Position position){

        for (Map.Entry<Position, Piece> piecePositionEntry : board.getAllPieces().entrySet()) {
            if (piecePositionEntry.getKey().equals(position)){
                return piecePositionEntry.getValue();
            }
        }
        return null;
    }

    public static Position findAllyKingPosition(Map<Position, Piece> allyPieces){

        for (Map.Entry<Position, Piece> piecePositionEntry : allyPieces.entrySet()) {
            if (piecePositionEntry.getValue() instanceof King){
                return piecePositionEntry.getKey();
            }
        }
        return null;
    }
}
