package app.gameengine;

import app.gameengine.move_logic.CastlingHelper;
import app.gameengine.move_logic.PawnMoveHelper;
import app.gameengine.pieces.Pawn;
import app.gameengine.pieces.Piece;


public class TurnHelper {
    private Piece lastMovedPieceWhite;
    private Piece lastMovedPieceBlack;

    public void endTurn(Piece piece, Position newPosition){
        CastlingHelper.setCastlingPieceMoveStatus(piece);

        Piece opponentLastMoved = piece.isWhite() ? lastMovedPieceBlack : lastMovedPieceWhite;
        if (opponentLastMoved instanceof Pawn pawn && pawn.isEnPassantTakeable()){
            PawnMoveHelper.setPawnMoveStatusMovedLastRound(pawn);
        }

        PawnMoveHelper.setPawnMoveStatus(piece, newPosition);

        if (piece.isWhite()){
            lastMovedPieceWhite = piece;
        } else {
            lastMovedPieceBlack = piece;
        }
    }

}
