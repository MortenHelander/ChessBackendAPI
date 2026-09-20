package app.gameengine.move_logic;

import app.gameengine.Board;
import app.gameengine.Position;
import app.gameengine.pieces.*;
import app.testutils.BoardTestUtils;
import app.testutils.PiecesTestFactory;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

class CheckCheckerTest {

    @Test
    void kingAttackedByRookOnOpenFile_shouldReturnTrue() {
        Board board = BoardTestUtils.emptyBoard();
        King king = PiecesTestFactory.whiteKing(true);
        BoardTestUtils.place(board, Position.E1, king);
        Rook enemyRook = PiecesTestFactory.blackRook(true);
        BoardTestUtils.place(board, Position.E8, enemyRook);

        boolean isChecked = CheckChecker.isKingChecked(board, king);

        assertThat(isChecked, is(true));
    }

    @Test
    void kingNotAttacked_noEnemyOnAnyLine_shouldReturnFalse() {

        Board board = BoardTestUtils.emptyBoard();
        King king = PiecesTestFactory.whiteKing(true);
        BoardTestUtils.place(board, Position.E1, king);
        Rook enemyRook = PiecesTestFactory.blackRook(true);
        BoardTestUtils.place(board, Position.D2, enemyRook);

        boolean isChecked = CheckChecker.isKingChecked(board, king);

        assertThat(isChecked, is(false));
    }

    @Test
    void kingAttackLineBlockedByAlly_shouldReturnFalse() {

        Board board = BoardTestUtils.emptyBoard();
        King king = PiecesTestFactory.whiteKing(true);
        BoardTestUtils.place(board, Position.E1, king);
        Rook enemyRook = PiecesTestFactory.blackRook(true);
        BoardTestUtils.place(board, Position.E8, enemyRook);
        Queen queen = PiecesTestFactory.whiteQueen();
        BoardTestUtils.place(board, Position.E4, queen);

        boolean isChecked = CheckChecker.isKingChecked(board, king);

        assertThat(isChecked, is(false));
    }

    @Test
    void kingAttackedByKnight_shouldReturnTrue() {

        Board board = BoardTestUtils.emptyBoard();
        King king = PiecesTestFactory.whiteKing(true);
        BoardTestUtils.place(board, Position.E1, king);
        Knight enemyKnight = PiecesTestFactory.blackKnight();
        BoardTestUtils.place(board, Position.D3, enemyKnight);

        boolean isChecked = CheckChecker.isKingChecked(board, king);

        assertThat(isChecked, is(true));
    }

    @Test
    void kingAttackedByPawnDiagonal_shouldReturnTrue() {

        Board board = BoardTestUtils.emptyBoard();
        King king = PiecesTestFactory.whiteKing(true);
        BoardTestUtils.place(board, Position.E1, king);
        Pawn enemyPawn = PiecesTestFactory.blackPawn(true, false);
        BoardTestUtils.place(board, Position.D2, enemyPawn);

        boolean isChecked = CheckChecker.isKingChecked(board, king);

        assertThat(isChecked, is(true));
    }

    @Test
    void allyPieceBlockingCheck_shouldReturnOnlySquaresStillBlockingEnemyAttack() {
        // king E1, ally rook E4, enemy rook E8 — the ally rook is pinned along the E-file.
        Board board = BoardTestUtils.emptyBoard();
        King king = PiecesTestFactory.whiteKing(true);
        BoardTestUtils.place(board, Position.E1, king);
        Rook allyRook = PiecesTestFactory.whiteRook(true);
        BoardTestUtils.place(board, Position.E4, allyRook);
        Queen enemyQueen = PiecesTestFactory.blackQueen();
        BoardTestUtils.place(board, Position.E8, enemyQueen);


        List<Position> allyRookMoves = new ArrayList<>();
        int x = Position.E4.getX();
        int y = Position.E4.getY();

        //up
        Direction up = new Direction(0, -1);
        allyRookMoves.addAll(SlidingMoveHelper.getPossiblePositions(board, allyRook, x, y, up));
        //down
        Direction down = new Direction(0, +1);
        allyRookMoves.addAll(SlidingMoveHelper.getPossiblePositions(board, allyRook, x, y, down));
        //left
        Direction left = new Direction(-1, 0);
        allyRookMoves.addAll(SlidingMoveHelper.getPossiblePositions(board, allyRook, x, y, left));
        //right
        Direction right = new Direction(+1, 0);
        allyRookMoves.addAll(SlidingMoveHelper.getPossiblePositions(board, allyRook, x, y, right));

        //before checked rook should be able to slide to any side leaving king open
        assertThat(allyRookMoves, is(hasItem(Position.D4)));

        allyRookMoves = CheckChecker.isCheckedAfterMove(board, allyRook, Position.E4, allyRookMoves);

        //method places and removes the selected piece on the board but afterwards board piece count should be thwe same
        assertThat(board.getAllPieces().size(), is(3));

        //ally rook moves should now contain all ranks of file E except for it's own position and ally kings nothing else
        assertThat(allyRookMoves, containsInAnyOrder(Position.E2, Position.E3, Position.E5, Position.E6, Position.E7, Position.E8));
    }

    @Test
    void noAttackOnKing_shouldNotAffectMoveList() {
        Board board = BoardTestUtils.emptyBoard();
        King king = PiecesTestFactory.whiteKing(true);
        BoardTestUtils.place(board, Position.E1, king);
        Queen queen = PiecesTestFactory.whiteQueen();
        BoardTestUtils.place(board, Position.D4, queen);

        List<Position> moves = queen.getPossibleMoves(board, Position.D4);

        //queen in middle of board with only king blocking has 27 potential moves
        assertThat(moves.size(), is(27));

        Pawn enemyPawn = PiecesTestFactory.blackPawn(false, false);
        BoardTestUtils.place(board, Position.C7, enemyPawn);

        moves = CheckChecker.isCheckedAfterMove(board, queen, Position.D4, moves);

        //enemy pawn can't threaten king so size should stay the same
        assertThat(moves.size(), is(27));
    }


    @Test
    void kingCorneredAndAttacked_noEscape_shouldReturnTrue() {
        Board board = BoardTestUtils.emptyBoard();
        King whiteKing = PiecesTestFactory.whiteKing(true);
        BoardTestUtils.place(board, Position.A8, whiteKing);
        BoardTestUtils.place(board, Position.B6, PiecesTestFactory.blackKing(true));
        BoardTestUtils.place(board, Position.A7, PiecesTestFactory.blackQueen());

        //white king is currently checked and have no possible escape so a true checkmate
        assertThat(CheckChecker.isKingChecked(board, whiteKing), is(true));
        assertThat(CheckChecker.isCheckMate(board, true), is(true));
        assertThat(CheckChecker.isStaleMate(board, true), is(false));
    }

    @Test
    void kingHasNoMoves_butNotInCheck_shouldBeStalemateNotCheckmate() {
        Board board = BoardTestUtils.emptyBoard();
        King whiteKing = PiecesTestFactory.whiteKing(true);
        BoardTestUtils.place(board, Position.A1, whiteKing);
        BoardTestUtils.place(board, Position.B3, PiecesTestFactory.blackQueen());

        //king cannot move anywhere without being checked but is not currently in check so a stalemate is true
        assertThat(CheckChecker.isKingChecked(board, whiteKing), is(false));
        assertThat(CheckChecker.isCheckMate(board, true), is(false));
        assertThat(CheckChecker.isStaleMate(board, true), is(true));
    }
}