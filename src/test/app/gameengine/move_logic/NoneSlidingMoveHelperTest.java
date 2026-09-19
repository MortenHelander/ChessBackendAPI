package app.gameengine.move_logic;

import app.gameengine.Board;
import app.gameengine.Position;
import app.gameengine.pieces.King;
import app.gameengine.pieces.Knight;
import app.gameengine.pieces.Pawn;
import app.testutils.BoardTestUtils;
import app.testutils.PiecesTestFactory;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import static org.hamcrest.Matchers.is;

import static org.junit.jupiter.api.Assertions.*;

class NoneSlidingMoveHelperTest {

    @Test
    void emptyBoard_returnsOneSquarePerOffset(){
        Board board = BoardTestUtils.emptyBoard();
        King king = PiecesTestFactory.whiteKing(true);
        BoardTestUtils.place(board, Position.E2, king);
        List<Direction> offsets = new ArrayList<>(List.of(
                //left
                new Direction(-1, 0),
                //right
                new Direction(+1, 0),
                //up
                new Direction(0, -1),
                //down
                new Direction(0, +1),
                //up left
                new Direction(-1, -1),
                //down left
                new Direction(-1, +1),
                //up right
                new Direction(+1, -1),
                //down right
                new Direction(+1, +1)));

        List<Position> moves = NoneSlidingMoveHelper.getPossiblePositions(board, king, Position.E2.getX(), Position.E2.getY(), offsets);

        assertThat(moves.size(), is(8));
    }

    @Test
    void pieceOnBoardEdge_shouldNotContainIllegalSquares(){
        Board board = BoardTestUtils.emptyBoard();
        Knight knight = PiecesTestFactory.blackKnight();
        BoardTestUtils.place(board, Position.H8, knight);

        List<Direction> offsets = new ArrayList<>(List.of(
                //2 left 1 up
                new Direction(-2, -1),
                //2 left 1 down
                new Direction(-2, +1),
                //1 left 2 up
                new Direction(-1, -2),
                //1 left 2 down
                new Direction(-1, +2),
                //2right 1 up
                new Direction(+2, -1),
                //2right 1 down
                new Direction(+2, +1),
                //1 right 2 up
                new Direction(+1, -2),
                //1 right 2 down
                new Direction(+1, +2)));

        List<Position> moves = NoneSlidingMoveHelper.getPossiblePositions(board, knight, Position.H8.getX(), Position.H8.getY(), offsets);

        //a knight in corner should only have two available positions
        assertThat(moves.size(), is(2));
    }

    @Test
    void allyOnPossibleSquare_shouldBeExcluded(){

        Board board = BoardTestUtils.emptyBoard();
        King king = PiecesTestFactory.whiteKing(true);
        BoardTestUtils.place(board, Position.E5, king);
        Pawn pawn = PiecesTestFactory.whitePawn(true, false);
        BoardTestUtils.place(board, Position.E4, pawn);
        List<Direction> offsets = new ArrayList<>(List.of(
                //left
                new Direction(-1, 0),
                //right
                new Direction(+1, 0),
                //up
                new Direction(0, -1),
                //down
                new Direction(0, +1),
                //up left
                new Direction(-1, -1),
                //down left
                new Direction(-1, +1),
                //up right
                new Direction(+1, -1),
                //down right
                new Direction(+1, +1)));

        List<Position> moves = NoneSlidingMoveHelper.getPossiblePositions(board, king, Position.E5.getX(), Position.E5.getY(), offsets);

        assertThat(moves.size(), is(8));
        assertThat(moves.contains(Position.E4), is(false));
    }

    @Test
    void enemyOnPossibleSquare_shouldBeIncluded(){

        Board board = BoardTestUtils.emptyBoard();
        King king = PiecesTestFactory.whiteKing(true);
        BoardTestUtils.place(board, Position.E5, king);
        Pawn pawn = PiecesTestFactory.blackPawn(true, false);
        BoardTestUtils.place(board, Position.E4, pawn);
        List<Direction> offsets = new ArrayList<>(List.of(
                //left
                new Direction(-1, 0),
                //right
                new Direction(+1, 0),
                //up
                new Direction(0, -1),
                //down
                new Direction(0, +1),
                //up left
                new Direction(-1, -1),
                //down left
                new Direction(-1, +1),
                //up right
                new Direction(+1, -1),
                //down right
                new Direction(+1, +1)));

        List<Position> moves = NoneSlidingMoveHelper.getPossiblePositions(board, king, Position.E5.getX(), Position.E5.getY(), offsets);

        assertThat(moves.size(), is(8));
        assertThat(moves.contains(Position.E4), is(true));
    }

}