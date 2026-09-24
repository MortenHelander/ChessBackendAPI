package app.gameengine;

import app.gameengine.pieces.*;
import app.testutils.BoardTestUtils;
import app.testutils.PiecesTestFactory;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class MoveExecutorTest {

    private final MoveExecutor executor = new MoveExecutor();

    @Test
    void normalMove_pieceOnBoard_movesAndReturnsTrue() {
        Board board = BoardTestUtils.emptyBoard();
        Rook rook = PiecesTestFactory.whiteRook(false);
        BoardTestUtils.place(board, Position.A1, rook);

        boolean moved = executor.executeMove(board, rook, MoveType.NORMAL, Position.A1, Position.A4);

        assertThat(moved, is(true));
        assertThat(board.getAllPieces().get(Position.A1), is(nullValue()));
        assertThat(board.getAllPieces().get(Position.A4), is(sameInstance(rook)));
    }

    @Test
    void normalMove_pieceNotOnBoard_returnsFalseAndLeavesBoardUnchanged() {
        Board board = BoardTestUtils.emptyBoard();
        Rook rook = PiecesTestFactory.whiteRook(false); // never placed on the board

        boolean moved = executor.executeMove(board, rook, MoveType.NORMAL, Position.A1, Position.A4);

        assertThat(moved, is(false));
        assertThat(board.getAllPieces().isEmpty(), is(true));
    }

    @Test
    void executeMove_nullMoveType_returnsFalse() {
        Board board = BoardTestUtils.emptyBoard();
        King king = PiecesTestFactory.whiteKing(false);
        BoardTestUtils.place(board, Position.E1, king);

        boolean moved = executor.executeMove(board, king, null, Position.E1, Position.E2);

        assertThat(moved, is(false));
    }

    @Test
    void executeMove_promotionMoveType_notHandledHere_returnsFalse() {

        Board board = BoardTestUtils.emptyBoard();
        Pawn pawn = PiecesTestFactory.whitePawn(true, false);
        BoardTestUtils.place(board, Position.E7, pawn);

        boolean moved = executor.executeMove(board, pawn, MoveType.PROMOTION, Position.E7, Position.E8);

        assertThat(moved, is(false));
        assertThat(board.getAllPieces().get(Position.E7), is(sameInstance(pawn)));
    }

    @Test
    void castlingKingSide_movesKingAndRookCorrectly() {
        Board board = BoardTestUtils.emptyBoard();
        King king = PiecesTestFactory.whiteKing(false);
        BoardTestUtils.place(board, Position.E1, king);
        Rook rook = PiecesTestFactory.whiteRook(false);
        BoardTestUtils.place(board, Position.H1, rook);

        boolean moved = executor.executeMove(board, king, MoveType.CASTLING, Position.E1, Position.G1);

        assertThat(moved, is(true));
        assertThat(board.getAllPieces().get(Position.G1), is(sameInstance(king)));
        assertThat(board.getAllPieces().get(Position.F1), is(sameInstance(rook)));
        assertThat(board.getAllPieces().get(Position.E1), is(nullValue()));

        assertThat(board.getAllPieces().get(Position.H1), is(nullValue()));
    }

    @Test
    void castlingQueenSide_movesKingAndRookCorrectly() {
        Board board = BoardTestUtils.emptyBoard();
        King king = PiecesTestFactory.whiteKing(false);
        BoardTestUtils.place(board, Position.E1, king);
        Rook rook = PiecesTestFactory.whiteRook(false);
        BoardTestUtils.place(board, Position.A1, rook);

        boolean moved = executor.executeMove(board, king, MoveType.CASTLING, Position.E1, Position.C1);

        assertThat(moved, is(true));
        assertThat(board.getAllPieces().get(Position.C1), is(sameInstance(king)));
        assertThat(board.getAllPieces().get(Position.D1), is(sameInstance(rook)));

        assertThat(board.getAllPieces().get(Position.A1), is(nullValue()));
    }

    @Test
    void castling_noRookAtExpectedSquare_returnsFalse() {
        Board board = BoardTestUtils.emptyBoard();
        King king = PiecesTestFactory.whiteKing(false);
        BoardTestUtils.place(board, Position.E1, king);

        boolean moved = executor.executeMove(board, king, MoveType.CASTLING, Position.E1, Position.G1);

        assertThat(moved, is(false));
        assertThat(board.getAllPieces().get(Position.E1), is(sameInstance(king)));
    }

    @Test
    void enPassantRight_capturesEnemyPawnAndMoves() {
        Board board = BoardTestUtils.emptyBoard();
        Pawn whitePawn = PiecesTestFactory.whitePawn(true, false);
        BoardTestUtils.place(board, Position.D5, whitePawn);
        Pawn blackPawn = PiecesTestFactory.blackPawn(true, true);
        BoardTestUtils.place(board, Position.E5, blackPawn);

        boolean moved = executor.executeMove(board, whitePawn, MoveType.EN_PASSANT, Position.D5, Position.E6);

        assertThat(moved, is(true));
        assertThat(board.getAllPieces().get(Position.E6), is(sameInstance(whitePawn)));
        assertThat(board.getAllPieces().get(Position.E5), is(nullValue())); // captured pawn removed

        assertThat(board.getAllPieces().get(Position.D5), is(nullValue()));
    }

    @Test
    void enPassant_targetPawnNotEnPassantTakeable_returnsFalse() {
        Board board = BoardTestUtils.emptyBoard();
        Pawn whitePawn = PiecesTestFactory.whitePawn(true, false);
        BoardTestUtils.place(board, Position.D5, whitePawn);
        Pawn blackPawn = PiecesTestFactory.blackPawn(true, false); // NOT takeable
        BoardTestUtils.place(board, Position.E5, blackPawn);

        boolean moved = executor.executeMove(board, whitePawn, MoveType.EN_PASSANT, Position.D5, Position.E6);

        assertThat(moved, is(false));
        assertThat(board.getAllPieces().get(Position.D5), is(sameInstance(whitePawn)));
        assertThat(board.getAllPieces().get(Position.E5), is(sameInstance(blackPawn)));
    }

    @Test
    void promotion_placesNewPieceOnPromotionSquare() {
        Board board = BoardTestUtils.emptyBoard();
        Pawn pawn = PiecesTestFactory.whitePawn(true, false);
        BoardTestUtils.place(board, Position.E8, pawn);
        Queen queen = PiecesTestFactory.whiteQueen();

        executor.promotion(board, queen, Position.E8);

        assertThat(board.getAllPieces().get(Position.E8), is(sameInstance(queen)));
        assertThat(board.getAllPieces().size(), is(1));
    }
}