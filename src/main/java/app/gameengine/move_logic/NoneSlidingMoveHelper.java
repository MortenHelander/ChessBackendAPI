package app.gameengine.move_logic;

import app.gameengine.Board;
import app.gameengine.Position;
import app.gameengine.PositionConverter;
import app.gameengine.pieces.Piece;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NoneSlidingMoveHelper {
    public static List<Position> getPossiblePositions(Board board, Piece piece, int x, int y, List<Direction>offsets){

        Map<Position, Piece> allyPieces = PieceSorter.sortPieces(board, piece, true);

        List<Position> candidates = new ArrayList<>();

        for (Direction offset : offsets) {
            Position candidate = PositionConverter.fromCoordinates(x + offset.dx(), y + offset.dy());
            if (candidate != null && !allyPieces.containsKey(candidate)){
                candidates.add(candidate);
            }
        }
        return candidates;
    }
}
