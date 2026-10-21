package app.services;

import app.entities.Move;
import app.exceptions.GameReplayException;
import app.gameengine.Board;
import app.gameengine.game_logic.MoveResult;
import app.gameengine.game_logic.Promotion;
import app.gameengine.exceptions.InvalidGameActionException;
import app.gameengine.game_logic.PromotionHelper;
import app.gameengine.pieces.Piece;

import java.util.List;

public class ReplayHelper {

    public static Board rebuildBoard(List<Move> moves) {
        Board board = new Board();
        board.initializeNewBoard();

        return rebuildBoard(board, moves);
    }

    public static Board rebuildBoard(Board board, List<Move> moves){
        for (Move move : moves) {
            String label = "Stored move #" + move.getMoveNumber()
                    + " (" + move.getFrom() + "->" + move.getTo() + ")";

            Piece piece = board.getAllPieces().get(move.getFrom());
            if (piece == null) {
                throw new GameReplayException(label + " has no piece on its from-square");
            }

            try {
                MoveResult result = board.move(piece, move.getFrom(), move.getTo());
                if (!result.success()){
                    throw new GameReplayException(label + " is not a valid move on this board");
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
