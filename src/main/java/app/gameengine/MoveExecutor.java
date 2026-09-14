package app.gameengine;

import app.gameengine.pieces.Pawn;
import app.gameengine.pieces.Piece;

import java.util.Map;

public class MoveExecutor {

    public boolean executeMove(Board board, Piece piece, MoveType moveType, Position oldPosition, Position newPosition){

        if (moveType == null){
            return false;
        }

        boolean moved = false;

        if (moveType == MoveType.NORMAL){
            moved = normalMove(board, piece, moveType, oldPosition, newPosition);
        }else if (moveType == MoveType.CASTLING){
            moved = castlingMove(board, piece, moveType, oldPosition, newPosition);
        }else if (moveType == MoveType.EN_PASSANT){
            moved = enPassantMove(board, piece, moveType, oldPosition, newPosition);
        }else if ()
        return moved;
    }

    public boolean normalMove(Board board, Piece piece, MoveType moveType, Position oldPosition, Position newPosition){
        if (board.getAllPieces().containsValue(piece)) {
            //remove piece from old position
            board.getAllPieces().remove(oldPosition);
            board.getAllPieces().put(newPosition, piece);
            return true;
        }
        return false;
    }

    public boolean castlingMove(Board board, Piece king, MoveType moveType, Position oldPosition, Position newPosition){

        //if castling right
        if (oldPosition.getX() < newPosition.getX()){
            Piece rook = PieceFinder.findPiece(this, PositionConverter.fromCoordinates(oldPosition.getX()+3, oldPosition.getY()));
            Position newRookPosition = PositionConverter.fromCoordinates(oldPosition.getX()+1, oldPosition.getY());
            return executeCastlingMove(board, king, oldPosition, newPosition, rook, newRookPosition);

            //if castling left
        }else if (oldPosition.getX() > newPosition.getX()){
            Piece rook = PieceFinder.findPiece(this, PositionConverter.fromCoordinates(oldPosition.getX()-4, oldPosition.getY()));
            Position newRookPosition = PositionConverter.fromCoordinates(oldPosition.getX()-1, oldPosition.getY());
            return executeCastlingMove(board, king, oldPosition, newPosition, rook, newRookPosition);
        }
        return false;
    }

    private boolean executeCastlingMove(Board board, Piece king, Position oldPosition, Position newPosition, Piece rook, Position newRookPosition) {
        if (board.getAllPieces().containsValue(king) && board.getAllPieces().containsValue(rook)) {
            board.getAllPieces().remove(oldPosition);
            board.getAllPieces().remove(newPosition);
            board.getAllPieces().put(newPosition, king);
            board.getAllPieces().put(newRookPosition, rook);
            return true;
        }
        return false;
    }

    public boolean enPassantMove(Board board, Piece piece, MoveType moveType, Position oldPosition, Position newPosition){

        if (piece instanceof Pawn pawn){
            //if en passant right
            if (oldPosition.getX() < newPosition.getX()){
                Position enemyPiecePosition = PositionConverter.fromCoordinates(oldPosition.getX()+1, oldPosition.getY());
                Piece enemyPiece = PieceFinder.findPiece(this, enemyPiecePosition);
                if (enemyPiece instanceof Pawn enemyPawn && enemyPawn.isEnPassantTakeable()){
                    board.getAllPieces().put(newPosition, piece);
                    board.getAllPieces().remove(enemyPiecePosition, enemyPiece);
                    return true;
                }
                //if en passant left
            } else if (oldPosition.getX() > newPosition.getX()){
                Position enemyPiecePosition = PositionConverter.fromCoordinates(oldPosition.getX()-1, oldPosition.getY());
                Piece enemyPiece = PieceFinder.findPiece(this, enemyPiecePosition);
                if (enemyPiece instanceof Pawn enemyPawn && enemyPawn.isEnPassantTakeable()){
                    board.getAllPieces().put(newPosition, piece);
                    board.getAllPieces().remove(enemyPiecePosition, enemyPiece);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean promotionMove(){

    }
}
