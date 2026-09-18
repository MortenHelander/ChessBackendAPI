package app.gameengine.move_logic;

import app.gameengine.Board;
import app.gameengine.Position;
import app.gameengine.PositionConverter;
import app.gameengine.pieces.King;
import app.gameengine.pieces.Piece;
import app.gameengine.pieces.Rook;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CastlingHelper {
    public static List<Position> getCastlingMoves(Board board, Piece piece, Position position){

        List<Position> candidates = new ArrayList<>();

        int x = position.getX();
        int y = position.getY();

        //the king has never moved
        if (hasKingMoved(piece)){
            return candidates;
        }

        //the king isn't checked
        if (CheckChecker.isKingChecked(board, piece)){
            return candidates;
        }

        //check left of king
        Direction left = new Direction(-1, 0);
        //check if squares between king and left rook is empty and not checked on the way
        if (isBetweenEmptyAndNotCheckedOnTheWay(board, piece, x, y, left, 3)){

            Position rookLeftCandidatePosition = PositionConverter.fromCoordinates(x-4, y);
            Piece rookLeftCandidate = PieceFinder.findPiece(board, rookLeftCandidatePosition);
            if (rookLeftCandidate instanceof Rook rook && !rook.isHasMoved() && !isSquareChecked(board, piece, rookLeftCandidatePosition)){
                Position leftPosition = PositionConverter.fromCoordinates(x-2, y);
                candidates.add(leftPosition);
            }
        }

        //check right of king
        Direction right = new Direction(+1, 0);
        //check if squares between king and right rook is empty and not checked on the way
        if (isBetweenEmptyAndNotCheckedOnTheWay(board, piece, x, y, right, 2)){

            Position rookRightCandidatePosition = PositionConverter.fromCoordinates(x+3, y);
            Piece rookRightCandidate = PieceFinder.findPiece(board, rookRightCandidatePosition);
            if (rookRightCandidate instanceof Rook rook && !rook.isHasMoved() && !isSquareChecked(board, piece, rookRightCandidatePosition)){
                Position rightPosition = PositionConverter.fromCoordinates(x+2, y);
                candidates.add(rightPosition);
            }
        }
        return candidates;
    }


    public static boolean isCastlingMove(Board board, Piece piece, Position oldPosition, Position newPosition){

        if (piece instanceof King){
            List<Position> castlingCandidates = getCastlingMoves(board, piece, oldPosition);
            if (castlingCandidates.contains(newPosition)){
                return true;
            }
        }
        return false;
    }

    public static void setCastlingPieceMoveStatus(Piece piece){
        if (piece instanceof King king){
            if (!king.isHasMoved()){
                king.setHasMoved(true);
            }
        }
        if (piece instanceof Rook rook){
            if (!rook.isHasMoved()){
                rook.setHasMoved(true);
            }
        }
    }

    private static boolean hasKingMoved(Piece piece) {

        if (piece instanceof King king) {
            return king.isHasMoved();
        }
        return true;
    }

    private static boolean isBetweenEmptyAndNotCheckedOnTheWay(Board board, Piece piece, int x, int y, Direction direction, int requiredEmptySquares){

        List<Position> emptySquares = SlidingMoveHelper.getPossiblePositions(board, piece, x, y, direction);
        if (emptySquares.size() == requiredEmptySquares){
            int notCheckedSquares = 0;
            for (int i = 0; i<2; i++) {
                if (!isSquareChecked(board, piece, emptySquares.get(i))){
                    notCheckedSquares++;
                }
            }
            if (notCheckedSquares == 2){
                return true;
            }
        }
        return false;
    }

    private static boolean isSquareChecked(Board board, Piece piece, Position square){

        Map<Position, Piece> enemyPieces = PieceSorter.sortPieces(board, piece, false);
        List<Position> enemyPositions = new ArrayList<>();

        for (Map.Entry<Position, Piece> piecePositionEntry : enemyPieces.entrySet()) {
            enemyPositions.addAll(piecePositionEntry.getValue().getPossibleMoves(board, piecePositionEntry.getKey()));
        }
        if (enemyPositions.contains(square)){
            return true;
        } else {
            return false;
        }
    }
}
