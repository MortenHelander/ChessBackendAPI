package app.gameengine;

import app.gameengine.move_logic.PieceFinder;
import app.gameengine.pieces.Pawn;
import app.gameengine.pieces.Piece;

public class MoveExecutor {

    public boolean executeMove(Board board, Piece piece, MoveType moveType, Position oldPosition, Position newPosition){

        if (moveType == null){
            return false;
        }

        boolean moved = false;

        if (moveType == MoveType.NORMAL){
            moved = normalMove(board, piece, oldPosition, newPosition);
        }else if (moveType == MoveType.CASTLING){
            moved = castlingMove(board, piece, oldPosition, newPosition);
        }else if (moveType == MoveType.EN_PASSANT){
            moved = enPassantMove(board, piece, oldPosition, newPosition);
        }
        return moved;
    }

    private boolean normalMove(Board board, Piece piece, Position oldPosition, Position newPosition){
        if (board.getAllPieces().containsValue(piece)) {
            //remove piece from old position
            board.getAllPieces().remove(oldPosition);
            board.getAllPieces().put(newPosition, piece);
            return true;
        }
        return false;
    }

    private boolean castlingMove(Board board, Piece king, Position oldKingPosition, Position newPosition){

        //if castling right
        if (oldKingPosition.getX() < newPosition.getX()){
            Piece rook = PieceFinder.findPiece(board, PositionConverter.fromCoordinates(oldKingPosition.getX()+3, oldKingPosition.getY()));
            Position newRookPosition = PositionConverter.fromCoordinates(oldKingPosition.getX()+1, oldKingPosition.getY());
            Position oldRookPosition = (PositionConverter.fromCoordinates(oldKingPosition.getX()+3, oldKingPosition.getY()));
            return executeCastlingMove(board, king, oldKingPosition, newPosition, rook, newRookPosition, oldRookPosition);

            //if castling left
        }else if (oldKingPosition.getX() > newPosition.getX()){
            Piece rook = PieceFinder.findPiece(board, PositionConverter.fromCoordinates(oldKingPosition.getX()-4, oldKingPosition.getY()));
            Position newRookPosition = PositionConverter.fromCoordinates(oldKingPosition.getX()-1, oldKingPosition.getY());
            Position oldRookPosition = (PositionConverter.fromCoordinates(oldKingPosition.getX()-4, oldKingPosition.getY()));
            return executeCastlingMove(board, king, oldKingPosition, newPosition, rook, newRookPosition, oldRookPosition);
        }
        return false;
    }

    private boolean executeCastlingMove(Board board, Piece king, Position oldKingPosition, Position newPosition, Piece rook, Position newRookPosition, Position oldRookPosition) {
        if (board.getAllPieces().containsValue(king) && board.getAllPieces().containsValue(rook)) {
            board.getAllPieces().remove(oldRookPosition);
            board.getAllPieces().remove(oldKingPosition);
            board.getAllPieces().remove(newPosition);
            board.getAllPieces().put(newPosition, king);
            board.getAllPieces().put(newRookPosition, rook);
            return true;
        }
        return false;
    }

    private boolean enPassantMove(Board board, Piece piece, Position oldPosition, Position newPosition){

        if (piece instanceof Pawn pawn){
            //if en passant right
            if (oldPosition.getX() < newPosition.getX()){
                Position enemyPiecePosition = PositionConverter.fromCoordinates(oldPosition.getX()+1, oldPosition.getY());
                Piece enemyPiece = PieceFinder.findPiece(board, enemyPiecePosition);
                if (enemyPiece instanceof Pawn enemyPawn && enemyPawn.isEnPassantTakeable()){
                    board.getAllPieces().remove(oldPosition);
                    board.getAllPieces().put(newPosition, piece);
                    board.getAllPieces().remove(enemyPiecePosition, enemyPiece);
                    return true;
                }
                //if en passant left
            } else if (oldPosition.getX() > newPosition.getX()){
                Position enemyPiecePosition = PositionConverter.fromCoordinates(oldPosition.getX()-1, oldPosition.getY());
                Piece enemyPiece = PieceFinder.findPiece(board, enemyPiecePosition);
                if (enemyPiece instanceof Pawn enemyPawn && enemyPawn.isEnPassantTakeable()){
                    board.getAllPieces().remove(oldPosition);
                    board.getAllPieces().put(newPosition, piece);
                    board.getAllPieces().remove(enemyPiecePosition, enemyPiece);
                    return true;
                }
            }
        }
        return false;
    }

    public void promotion(Board board, Piece promotionPiece, Position promotionSquare){
        board.getAllPieces().put(promotionSquare, promotionPiece);
    }
}
