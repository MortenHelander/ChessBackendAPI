package app.services;

import app.entities.Move;
import app.gameengine.Board;
import app.gameengine.MoveResult;
import app.gameengine.Promotion;
import app.gameengine.move_logic.PieceFinder;
import app.gameengine.move_logic.PromotionHelper;
import app.gameengine.pieces.Piece;

import java.util.List;

public class ReplayHelper {

    public static Board rebuildBoard(List<Move> moves){

        Board board = new Board();
        board.initializeNewBoard();

        for (Move move : moves) {
            Piece piece = board.getAllPieces().get(move.getFrom());
            MoveResult result = board.move(piece, move.getFrom(), move.getTo());
            if (result.isAwaitingPromotion()){
                String letter = move.getPromotionLetter();
                board.promotion(new Promotion(PromotionHelper.getPromotionPieceType(letter), move.getTo()));
            }
        }
        return board;
    }
}
