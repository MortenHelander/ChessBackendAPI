package app.services;

import app.entities.Move;
import app.exceptions.GameReplayException;
import app.gameengine.Board;
import app.gameengine.MoveResult;
import app.gameengine.Promotion;
import app.gameengine.exceptions.InvalidGameActionException;
import app.gameengine.move_logic.PieceFinder;
import app.gameengine.move_logic.PromotionHelper;
import app.gameengine.pieces.Piece;

import java.util.List;

public class ReplayHelper {

    public static Board rebuildBoard(List<Move> moves) {
        Board board = new Board();
        board.initializeNewBoard();

        for (Move move : moves) {
            String label = "Stored move #" + move.getMoveNumber()
                    + " (" + move.getFrom() + "->" + move.getTo() + ")";

            Piece piece = board.getAllPieces().get(move.getFrom());
            if (piece == null) {
                throw new GameReplayException(label + " has no piece on its from-square");
            }

            try {
                MoveResult result = board.move(piece, move.getFrom(), move.getTo());
                if (!result.success()) {
                    throw new GameReplayException(label + " was rejected by the engine on replay");
                }
                if (result.isAwaitingPromotion()) {
                    String letter = move.getPromotionLetter();
                    if (letter == null) {
                        throw new GameReplayException(label + " is a promotion but has no promotion letter");
                    }
                    board.promotion(new Promotion(PromotionHelper.getPromotionPieceType(letter), move.getTo()));
                }
            } catch (InvalidGameActionException e) {
                throw new GameReplayException(label + " failed on replay: " + e.getMessage(), e);
            }
        }
        return board;
    }
}
