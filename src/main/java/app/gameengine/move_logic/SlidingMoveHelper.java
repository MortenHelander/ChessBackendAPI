package app.gameengine.move_logic;

import app.gameengine.Board;
import app.gameengine.Position;
import app.gameengine.PositionConverter;
import app.gameengine.pieces.Piece;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SlidingMoveHelper {
    public static List<Position> getPossiblePositions (Board board, Piece piece, int x, int y, Direction direction){

        Map<Position, Piece> allyPieces = PieceSorter.sortPieces(board, piece, true);
        Map<Position, Piece> enemyPieces = PieceSorter.sortPieces(board, piece, false);


        ArrayList<Position> candidates = new ArrayList<>();

        for (int i = 1; i<9; i++){
            Position candidate = PositionConverter.fromCoordinates(x + direction.dx()*i, y+ direction.dy()*i);
            //if out of bounds
            if (candidate == null){
                break;
            }
            //hitting another ally piece
            if (allyPieces.containsKey(candidate)){
                break;
            }
            //if enemy piece can be taken
            if (enemyPieces.containsKey(candidate)){
                candidates.add(candidate);
                break;
            }
            candidates.add(candidate);
        }
        return candidates;
    }
}
