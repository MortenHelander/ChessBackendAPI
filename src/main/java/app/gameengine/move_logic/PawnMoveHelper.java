package app.gameengine.move_logic;

import app.gameengine.Board;
import app.gameengine.Position;
import app.gameengine.PositionConverter;
import app.gameengine.pieces.Pawn;
import app.gameengine.pieces.Piece;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PawnMoveHelper {
    public static List<Position> getPawnMoves(Board board, Pawn pawn, int x, int y){

        List<Position> thisPawnsPossibleMoves = new ArrayList<>();
        Map<Position, Piece> whitePieces = board.getAllWhitePieces();
        Map<Position, Piece> blackPieces = board.getAllBlackPieces();

        //white pawn
        if (pawn.isWhite()){
            //one step up
            Position candidate = PositionConverter.fromCoordinates(x, y+1);
            if (candidate != null && !board.getAllPieces().containsKey(candidate)){
                thisPawnsPossibleMoves.add(candidate);
            }

            //two steps up if the pawn hasn't moved
            if (!pawn.isHasMoved()){
                Position candidate2 = PositionConverter.fromCoordinates(x, y+2);

                if (candidate2 != null && !board.getAllPieces().containsKey(candidate2) && !board.getAllPieces().containsKey(candidate)){
                    thisPawnsPossibleMoves.add(candidate2);
                }
            }

            //check if possible to take a black piece by moving diagonal up
            Position leftUp = PositionConverter.fromCoordinates(x-1, y+1);
            if (leftUp != null && blackPieces.containsKey(leftUp)){
                thisPawnsPossibleMoves.add(leftUp);
            }
            Position rightUp = PositionConverter.fromCoordinates(x+1, y+1);
            if (rightUp != null && blackPieces.containsKey(rightUp)){
                thisPawnsPossibleMoves.add(rightUp);
            }

            //check for possible en passant
            Position enemyLeftPawnPosition = PositionConverter.fromCoordinates(x-1, y);
            Piece enemyPawnLeftCandidate = PieceFinder.findPiece(board, enemyLeftPawnPosition);
            if (enemyPawnLeftCandidate != null && !enemyPawnLeftCandidate.isWhite() && enemyPawnLeftCandidate instanceof Pawn pawnCandidate && pawnCandidate.isEnPassantTakeable()){
                thisPawnsPossibleMoves.add(leftUp);
            }

            Position enemyRightPawnPosition = PositionConverter.fromCoordinates(x+1, y);
            Piece enemyPawnRightCandidate = PieceFinder.findPiece(board, enemyRightPawnPosition);
            if (enemyPawnRightCandidate != null && !enemyPawnRightCandidate.isWhite() && enemyPawnRightCandidate instanceof Pawn pawnCandidate && pawnCandidate.isEnPassantTakeable()){
                thisPawnsPossibleMoves.add(rightUp);
            }
        }

        //black pawn
        if (!pawn.isWhite()){
            //one step down
            Position candidate = PositionConverter.fromCoordinates(x, y-1);
            if (candidate != null && !board.getAllPieces().containsKey(candidate)){
                thisPawnsPossibleMoves.add(candidate);
            }

            //two steps down if the pawn hasn't moved
            if (!pawn.isHasMoved()){
                Position candidate2 = PositionConverter.fromCoordinates(x, y-2);
                if (candidate2 != null && !board.getAllPieces().containsKey(candidate2) && !board.getAllPieces().containsKey(candidate)){
                    thisPawnsPossibleMoves.add(candidate2);
                }
            }

            //check if possible to take a white piece by moving diagonal downward
            Position leftDown = PositionConverter.fromCoordinates(x-1, y-1);
            if (leftDown != null && whitePieces.containsKey(leftDown)){
                thisPawnsPossibleMoves.add(leftDown);
            }
            Position rightDown = PositionConverter.fromCoordinates(x+1, y-1);
            if (rightDown != null && whitePieces.containsKey(rightDown)){
                thisPawnsPossibleMoves.add(rightDown);
            }

            //check for possible en passant
            Position enemyLeftPawnPosition = PositionConverter.fromCoordinates(x-1, y);
            Piece enemyPawnLeftCandidate = PieceFinder.findPiece(board, enemyLeftPawnPosition);
            if (enemyPawnLeftCandidate != null && enemyPawnLeftCandidate.isWhite() && enemyPawnLeftCandidate instanceof Pawn pawnCandidate && pawnCandidate.isEnPassantTakeable()){
                thisPawnsPossibleMoves.add(leftDown);
            }

            Position enemyRightPawnPosition = PositionConverter.fromCoordinates(x+1, y);
            Piece enemyPawnRightCandidate = PieceFinder.findPiece(board, enemyRightPawnPosition);
            if (enemyPawnRightCandidate != null && enemyPawnRightCandidate.isWhite() && enemyPawnRightCandidate instanceof Pawn pawnCandidate && pawnCandidate.isEnPassantTakeable()){
                thisPawnsPossibleMoves.add(rightDown);
            }
        }
        return thisPawnsPossibleMoves;
    }

    public static boolean isEnPassantMove(Board board, Piece piece, Position oldPosition, Position newPosition){

        Piece nullPiece = PieceFinder.findPiece(board, newPosition);
        if (piece instanceof Pawn && nullPiece == null){
            if (oldPosition.getX() < newPosition.getX() || oldPosition.getX() > newPosition.getX()){
                return true;
            }
        }
        return false;
    }

    public static void setPawnMoveStatus(Piece piece, Position newPosition){

        if (piece instanceof Pawn pawn){
            if (!pawn.isHasMoved()){
                pawn.setHasMoved(true);
                //if white pawn hasn't moved and are now on rank 4, they are available for en passant next round
                if (pawn.isWhite() && newPosition.getY() == 3){
                    pawn.setEnPassantTakeable(true);
                    //same for black pawn
                } else if (!pawn.isWhite() && newPosition.getY() == 4){
                    pawn.setEnPassantTakeable(true);
                }
            } else {
                setPawnMoveStatusMovedLastRound(pawn);
            }
        }

    }

    public static void setPawnMoveStatusMovedLastRound(Pawn pawn){

        pawn.setEnPassantTakeable(false);
    }
}
