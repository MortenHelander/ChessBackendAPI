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
        if (isBetweenEmptyAndNotCheckedOnTheWay(board, piece, x, y, left, 3, position)){

            Position rookLeftCandidatePosition = PositionConverter.fromCoordinates(x-4, y);
            Piece rookLeftCandidate = PieceFinder.findPiece(board, rookLeftCandidatePosition);
            if (rookLeftCandidate instanceof Rook rook && !rook.isHasMoved()){
                Position leftPosition = PositionConverter.fromCoordinates(x-2, y);
                candidates.add(leftPosition);
            }
        }

        //check right of king
        Direction right = new Direction(+1, 0);
        //check if squares between king and right rook is empty and not checked on the way
        if (isBetweenEmptyAndNotCheckedOnTheWay(board, piece, x, y, right, 2, position)){

            Position rookRightCandidatePosition = PositionConverter.fromCoordinates(x+3, y);
            Piece rookRightCandidate = PieceFinder.findPiece(board, rookRightCandidatePosition);
            if (rookRightCandidate instanceof Rook rook && !rook.isHasMoved()){
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

    private static boolean isBetweenEmptyAndNotCheckedOnTheWay(Board board, Piece piece, int x, int y, Direction direction, int requiredEmptySquares, Position currentPosition){

        List<Position> emptySquares = SlidingMoveHelper.getPossiblePositions(board, piece, x, y, direction);
        if (emptySquares.size() == requiredEmptySquares){
            int notCheckedSquares = 0;
            for (int i = 0; i<2; i++) {
                if (!isSquareEmpty(board, emptySquares.get(i))){
                    return false;
                }
                if (!isSquareAttacked(board, piece, emptySquares.get(i), currentPosition)){
                    notCheckedSquares++;
                }
            }
            if (notCheckedSquares == 2){
                return true;
            }
        }
        return false;
    }

    private static boolean isSquareEmpty(Board board, Position square){
        return !board.getAllPieces().containsKey(square);
    }

    private static boolean isSquareAttacked(Board board, Piece piece, Position square, Position currentPosition){

        Map<Position, Piece> enemyPieces = PieceSorter.sortPieces(board, piece, false);
        List<Position> enemyPossibleMoves = new ArrayList<>();

        //move king temporarily to check for pawns moves (only available to see attacks when enemy is in correct position)
        board.getAllPieces().put(square, piece);
        board.getAllPieces().remove(currentPosition, piece);

        for (Map.Entry<Position, Piece> piecePositionEntry : enemyPieces.entrySet()) {
            enemyPossibleMoves.addAll(piecePositionEntry.getValue().getPossibleMoves(board, piecePositionEntry.getKey()));
        }
        //put the piece back to original position
        board.getAllPieces().put(currentPosition, piece);

        //remove temporary move
        board.getAllPieces().remove(square, piece);
        return enemyPossibleMoves.contains(square);
    }
}
