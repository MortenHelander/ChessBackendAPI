package app.gameengine.move_logic;

import app.gameengine.Board;
import app.gameengine.Position;
import app.gameengine.pieces.*;
import app.testutils.BoardTestUtils;
import app.testutils.PiecesTestFactory;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import static org.hamcrest.Matchers.is;

class PawnMoveHelperTest {


    @Test
    void pawnStartingPosEmptyBoard_returnsTwoSquares() {
        Board board = BoardTestUtils.emptyBoard();
        Pawn pawn = PiecesTestFactory.whitePawn(false, false);
        BoardTestUtils.place(board, Position.E2, pawn);

        List<Position> moves = PawnMoveHelper.getPawnMoves(board, pawn, Position.E2.getX(), Position.E2.getY());

        //should contain only the two higher ranks of same file
        assertThat(moves, contains(Position.E3, Position.E4));
    }

    @Test
    void pawnNotStartingPosEmptyBoard_returnsOneSquare() {
        Board board = BoardTestUtils.emptyBoard();
        Pawn pawn = PiecesTestFactory.blackPawn(true, false);
        BoardTestUtils.place(board, Position.E5, pawn);

        List<Position> moves = PawnMoveHelper.getPawnMoves(board, pawn, Position.E5.getX(), Position.E5.getY());

        //should contain only 1 rank lower of same file
        assertThat(moves, contains(Position.E4));
    }

    @Test
    void enemyOneRankInFrontOfPawnStartPos_shouldReturnZeroSquares() {
        Board board = BoardTestUtils.emptyBoard();
        Pawn pawn = PiecesTestFactory.whitePawn(false, false);
        BoardTestUtils.place(board, Position.E2, pawn);
        Pawn bPawn = PiecesTestFactory.blackPawn(true, false);
        BoardTestUtils.place(board, Position.E3, bPawn);

        List<Position> moves = PawnMoveHelper.getPawnMoves(board, pawn, Position.E2.getX(), Position.E2.getY());

        assertThat(moves, is(empty()));
    }

    @Test
    void allyOneRankInFrontOfPawnStartPos_shouldReturnZeroSquares() {
        Board board = BoardTestUtils.emptyBoard();
        Pawn pawn = PiecesTestFactory.whitePawn(false, false);
        BoardTestUtils.place(board, Position.E2, pawn);
        Knight knight = PiecesTestFactory.whiteKnight();
        BoardTestUtils.place(board, Position.E3, knight);

        List<Position> moves = PawnMoveHelper.getPawnMoves(board, pawn, Position.E2.getX(), Position.E2.getY());

        assertThat(moves, is(empty()));
    }

    @Test
    void enemyOrAllyTwoRanksInFrontOfPawnStartPos_shouldReturnOneSquare() {
        Board board = BoardTestUtils.emptyBoard();
        Pawn pawn = PiecesTestFactory.whitePawn(false, false);
        BoardTestUtils.place(board, Position.E2, pawn);
        Knight knight = PiecesTestFactory.whiteKnight();
        BoardTestUtils.place(board, Position.E4, knight);

        List<Position> moves = PawnMoveHelper.getPawnMoves(board, pawn, Position.E2.getX(), Position.E2.getY());

        assertThat(moves, contains(Position.E3));
    }

    @Test
    void pawnDiagonalTakeBothDirectionsNotStartingPos_shouldReturnThreeSquares() {
        Board board = BoardTestUtils.emptyBoard();
        Pawn pawn = PiecesTestFactory.blackPawn(true, false);
        BoardTestUtils.place(board, Position.E5, pawn);
        Knight knight = PiecesTestFactory.whiteKnight();
        BoardTestUtils.place(board, Position.F4, knight);
        Pawn wPawn = PiecesTestFactory.whitePawn(true, false);
        BoardTestUtils.place(board, Position.D4, wPawn);

        List<Position> moves = PawnMoveHelper.getPawnMoves(board, pawn, Position.E5.getX(), Position.E5.getY());

        assertThat(moves.size(), is(3));
        assertThat(moves, containsInAnyOrder(Position.E4, Position.F4, Position.D4));
    }

    @Test
    void enemyPawnIsEnPassantAvailable_shouldReturnEnemySquareFileMinusOneRank() {
        Board board = BoardTestUtils.emptyBoard();
        Pawn pawn = PiecesTestFactory.blackPawn(true, false);
        BoardTestUtils.place(board, Position.E4, pawn);
        Pawn wPawn = PiecesTestFactory.whitePawn(true, true);
        BoardTestUtils.place(board, Position.D4, wPawn);

        List<Position> moves = PawnMoveHelper.getPawnMoves(board, pawn, Position.E4.getX(), Position.E4.getY());

        assertThat(moves.size(), is(2));
        assertThat(moves, containsInAnyOrder(Position.E3, Position.D3));

        boolean isEnPassantMove = PawnMoveHelper.isEnPassantMove(board, pawn, Position.E4, Position.D3);
        assertThat(isEnPassantMove, is(true));
    }

    @Test
    void notPawnEnPassantCheck_shouldReturnFalse() {
        Board board = BoardTestUtils.emptyBoard();
        Rook rook = PiecesTestFactory.whiteRook(true);
        Position position = Position.E3;
        BoardTestUtils.place(board, position, rook);

        Position desiredMove = Position.E6;

        boolean isEnPassantMove = PawnMoveHelper.isEnPassantMove(board, rook, position, desiredMove);
        assertThat(isEnPassantMove, is(false));
    }

    @Test
    void pawnRegularMoveEnPassantCheck_shouldReturnFalse() {
        Board board = BoardTestUtils.emptyBoard();
        Pawn pawn = PiecesTestFactory.blackPawn(true, false);
        BoardTestUtils.place(board, Position.E4, pawn);
        Pawn wPawn = PiecesTestFactory.whitePawn(true, false);
        BoardTestUtils.place(board, Position.D3, wPawn);

        boolean isEnPassantMove = PawnMoveHelper.isEnPassantMove(board, pawn, Position.E4, Position.D3);

        assertThat(isEnPassantMove, is(false));
    }

    @Test
    void pawnMakesOneSquareMove_shouldSetHasMovedTrueEnPassantFalse() {

        Pawn pawn = PiecesTestFactory.blackPawn(false, false);

        PawnMoveHelper.setPawnMoveStatus(pawn, Position.E6);

        assertThat(pawn.isHasMoved(), is(true));
        assertThat(pawn.isEnPassantTakeable(), is(false));
    }

    @Test
    void pawnMakesOpeningTwoSquareMove_shouldSetHasMovedTrueEnPassantTrue() {

        Pawn pawn = PiecesTestFactory.whitePawn(false, false);

        PawnMoveHelper.setPawnMoveStatus(pawn, Position.A4);

        assertThat(pawn.isHasMoved(), is(true));
        assertThat(pawn.isEnPassantTakeable(), is(true));
    }

    @Test
    void pawnGoesToEnPassantPositionButInTwoMoves_shouldSetHasMovedTrueEnPassantFalse () {

        Pawn pawn = PiecesTestFactory.whitePawn(false, false);

        PawnMoveHelper.setPawnMoveStatus(pawn, Position.A3);
        //hasmoved should now be true
        assertThat(pawn.isHasMoved(), is(true));
        assertThat(pawn.isEnPassantTakeable(), is(false));

        PawnMoveHelper.setPawnMoveStatus(pawn, Position.A4);

        //even though now going to rank 4 as white pawn in two moves should still not be en passant available
        assertThat(pawn.isHasMoved(), is(true));
        assertThat(pawn.isEnPassantTakeable(), is(false));

    }

    @Test
    void pawnIsEnPassantAvailableButTakesTurn_shouldSetEnPassantTakableFalse() {
        Pawn pawn = PiecesTestFactory.whitePawn(false, false);

        PawnMoveHelper.setPawnMoveStatus(pawn, Position.A4);
        //hasmoved AND enPassant should now be true
        assertThat(pawn.isHasMoved(), is(true));
        assertThat(pawn.isEnPassantTakeable(), is(true));

        PawnMoveHelper.setPawnMoveStatus(pawn, Position.A5);

        //pawn wasn't taken and moves again so can no longer be taken en passant
        assertThat(pawn.isHasMoved(), is(true));
        assertThat(pawn.isEnPassantTakeable(), is(false));
    }
}