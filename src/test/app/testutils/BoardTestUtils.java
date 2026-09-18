package app.testutils;

import app.gameengine.Board;
import app.gameengine.Position;
import app.gameengine.pieces.Piece;

import java.io.IOException;

public class BoardTestUtils {

    public static Board emptyBoard() {
        Board board = new Board();

        board.initializeNewBoard();
        board.getAllPieces().clear();
        return board;
    }

    public static void place(Board board, Position position, Piece piece) {
        board.getAllPieces().put(position, piece);
    }
}
