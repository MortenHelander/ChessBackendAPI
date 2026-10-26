package app.gameengine.game_logic;

import app.gameengine.Board;
import app.gameengine.Position;
import app.gameengine.pieces.Bishop;
import app.gameengine.pieces.King;
import app.gameengine.pieces.Knight;
import app.gameengine.pieces.Piece;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DrawChecker {

    public static boolean isInsufficientMaterial(Board board){
        List<Map.Entry<Position, Piece>> nonKingPieces = new ArrayList<>();
        for (Map.Entry<Position, Piece> entry : board.getAllPieces().entrySet()) {
            if (!(entry.getValue() instanceof King)){
                nonKingPieces.add(entry); //adds everything but the kings since those are always present
            }
        }
        if (nonKingPieces.isEmpty()){
            return true;
        }
        if (nonKingPieces.size() == 1 && (nonKingPieces.getFirst().getValue() instanceof Knight || nonKingPieces.getFirst().getValue() instanceof Bishop)){
            return true;
        }
        if (nonKingPieces.size() == 2 && nonKingPieces.getFirst().getValue() instanceof Bishop bishop && nonKingPieces.get(1).getValue()
                instanceof Bishop bishop2 && bishop.isWhite() != bishop2.isWhite()){
            Position p1 = nonKingPieces.getFirst().getKey();
            Position p2 = nonKingPieces.get(1).getKey();

            //modolus to find even or uneven
            int p1SquareColor = (p1.getX()+p1.getY())%2;
            int p2SquareColor = (p2.getX()+p2.getY())%2;
            return p1SquareColor == p2SquareColor;
        }
        return false;
    }
}
