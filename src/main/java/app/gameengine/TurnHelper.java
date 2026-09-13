package app.gameengine;

import app.gameengine.pieces.Pawn;
import app.gameengine.pieces.Piece;


public class TurnHelper {
    private Piece lastMovedPieceWhite;
    private Piece lastMovedPieceBlack;

    public void endTurn(Piece piece, Position newPosition){
        CastlingHelper.setCastlingPieceMoveStatus(piece);
        PawnMoveHelper.setPawnMoveStatus(piece, newPosition);
        handleLastPieceStatus(piece);
    }

    public void handleLastPieceStatus(Piece piece){

        if (piece instanceof Pawn pawn && pawn.isEnPassantTakeable()){
            PawnMoveHelper.setPawnMoveStatusMovedLastRound(pawn);
        }
        if (piece.isWhite()){
            lastMovedPieceWhite = piece;
        } else {
            lastMovedPieceBlack = piece;
        }
    }
}
