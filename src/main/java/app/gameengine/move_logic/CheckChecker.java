package app.gameengine.move_logic;

import app.gameengine.Board;
import app.gameengine.Position;
import app.gameengine.pieces.King;
import app.gameengine.pieces.Piece;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CheckChecker {

    public static List<Position> isCheckedAfterMove(Board board, Piece piece, Position currentPosition, List<Position> thisPiecePossibleMoves){

        thisPiecePossibleMoves.removeIf(move -> {
            //first check and save potential enemy piece to restore later
            Piece potentialEnemy = board.getAllPieces().get(move);

            //temporarily move piece
            board.getAllPieces().put(move, piece);

            //temporarily remove piece from current position
            board.getAllPieces().remove(currentPosition, piece);

            //check king for checkmate
            boolean isChecked = isKingChecked(board, piece);

            //put the piece back to original position
            board.getAllPieces().put(currentPosition, piece);

            //remove temporary move
            board.getAllPieces().remove(move, piece);

            //restore enemy piece if exist
            if (potentialEnemy != null) {
                board.getAllPieces().put(move, potentialEnemy);
            }

            return isChecked;
        });

        return thisPiecePossibleMoves;
    }

    public static boolean isKingChecked(Board board, Piece piece){

        Map<Position, Piece> allyPieces = PieceSorter.sortPieces(board, piece, true);
        Map<Position, Piece> enemyPieces = PieceSorter.sortPieces(board, piece, false);
        List<Position> enemyPositions = new ArrayList<>();
        Position allyKingPosition = PieceFinder.findAllyKingPosition(allyPieces);

        for (Map.Entry<Position, Piece> piecePositionEntry : enemyPieces.entrySet()) {
            enemyPositions.addAll(piecePositionEntry.getValue().getPossibleMoves(board, piecePositionEntry.getKey()));
        }
        if (enemyPositions.contains(allyKingPosition)){
            return true;
        } else {
            return false;
        }
    }

    public static boolean isCheckMate(Board board, boolean isWhitesTurn){
        return hasNoLegalMoves(board, isWhitesTurn) && isSideInCheck(board, isWhitesTurn);
    }

    public static boolean isStaleMate(Board board, boolean isWhitesTurn){
        return hasNoLegalMoves(board, isWhitesTurn) && !isSideInCheck(board, isWhitesTurn);
    }

    private static boolean hasNoLegalMoves(Board board, boolean isWhitesTurn){

        Map<Position, Piece> allyPieces;
        List<Position> possiblePositions = new ArrayList<>();

        if (isWhitesTurn){
            allyPieces = board.getAllWhitePieces();
        } else {
            allyPieces = board.getAllBlackPieces();
        }

        for (Map.Entry<Position, Piece> piecePositionEntry : allyPieces.entrySet()) {
            List<Position> thisPiecePossiblePositions = piecePositionEntry.getValue().getPossibleMoves(board, piecePositionEntry.getKey());
            isCheckedAfterMove(board, piecePositionEntry.getValue(), piecePositionEntry.getKey(), thisPiecePossiblePositions);
            possiblePositions.addAll(thisPiecePossiblePositions);
        }

        return possiblePositions.isEmpty();
    }

    private static boolean isSideInCheck(Board board, boolean isWhitesTurn){
        Map<Position, Piece> allyPieces;

        if (isWhitesTurn){
            allyPieces = board.getAllWhitePieces();
        } else {
            allyPieces = board.getAllBlackPieces();
        }
        Position kingPosition = PieceFinder.findAllyKingPosition(allyPieces);
        Piece king = allyPieces.get(kingPosition);
        return isKingChecked(board, king);
    }

}
