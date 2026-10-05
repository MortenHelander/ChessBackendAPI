package app.services;

import app.gameengine.Board;
import app.gameengine.Position;
import app.gameengine.move_logic.CastlingHelper;
import app.gameengine.move_logic.CheckChecker;
import app.gameengine.pieces.King;
import app.gameengine.pieces.Piece;

import java.util.ArrayList;
import java.util.List;

public class MoveValidator {
    public static List<Position> getLegalMoves(Board board, Piece piece, Position from) {
        List<Position> moves = new ArrayList<>(piece.getPossibleMoves(board, from));
        CheckChecker.isCheckedAfterMove(board, piece, from, moves);
        if (piece instanceof King) {
            moves.addAll(CastlingHelper.getCastlingMoves(board, piece, from));
        }
        return moves;
    }
}
