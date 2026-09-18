package app.gameengine.move_logic;

import app.gameengine.Board;
import app.gameengine.Position;
import app.gameengine.pieces.Piece;

import java.util.Map;

public class PieceSorter {
    public static Map<Position, Piece> sortPieces(Board board, Piece piece, boolean needAllies){

        Map<Position, Piece> allyPieces;
        Map<Position, Piece> enemyPieces;

        if (piece.isWhite()){
            allyPieces = board.getAllWhitePieces();
            enemyPieces = board.getAllBlackPieces();
        } else {
            allyPieces = board.getAllBlackPieces();
            enemyPieces = board.getAllWhitePieces();
        }
        if (needAllies){
            return allyPieces;
        } else {
            return enemyPieces;
        }
    }
}
