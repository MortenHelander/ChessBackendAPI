package app.gameengine;

import app.gameengine.move_logic.CastlingHelper;
import app.gameengine.move_logic.PawnMoveHelper;
import app.gameengine.move_logic.PromotionHelper;
import app.gameengine.pieces.Piece;

public class MoveIdentifier {

    public static MoveType identifyMove(Board board, Piece piece, Position oldPosition, Position newPosition){

        if (CastlingHelper.isCastlingMove(board, piece, oldPosition, newPosition)){
            return MoveType.CASTLING;
        }
        if (PawnMoveHelper.isEnPassantMove(board, piece, oldPosition, newPosition)){
            return MoveType.EN_PASSANT;
        }
        if (PromotionHelper.isPromotionMove(piece, newPosition)){
            return MoveType.PROMOTION;
        }
        if (board.getAllPieces().containsValue(piece)){
            return MoveType.NORMAL;
        }
        return null;
    }
}
