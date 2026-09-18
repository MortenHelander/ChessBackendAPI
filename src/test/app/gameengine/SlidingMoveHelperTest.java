package app.gameengine;

import app.gameengine.move_logic.Direction;
import app.gameengine.move_logic.SlidingMoveHelper;
import app.gameengine.pieces.Bishop;
import app.gameengine.pieces.Pawn;
import app.gameengine.pieces.Queen;
import app.gameengine.pieces.Rook;
import app.testutils.BoardTestUtils;
import app.testutils.PiecesTestFactory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import static org.hamcrest.Matchers.is;

public class SlidingMoveHelperTest {

        @Test
        void emptyBoard_returnsAllSquaresToEdgeOfBoard() {
            Board board = BoardTestUtils.emptyBoard();
            Rook rook = PiecesTestFactory.whiteRook(false);
            BoardTestUtils.place(board, Position.D4, rook);

            List<Position> moves = SlidingMoveHelper.getPossiblePositions(
                    board, rook, Position.D4.getX(), Position.D4.getY(), new Direction(1, 0));

            assertThat(moves, contains(Position.E4, Position.F4, Position.G4, Position.H4));
        }

        @Test
        void allyInPath_stopsBeforeAlly() {
            //queen on d1 can go diagonally up all positions until ally pawn blocks way on h5
            Board board = BoardTestUtils.emptyBoard();
            Queen queen = PiecesTestFactory.whiteQueen();
            Pawn pawn = PiecesTestFactory.whitePawn(true, false);

            BoardTestUtils.place(board, Position.D1, queen);
            BoardTestUtils.place(board, Position.H5, pawn);

            List<Position> moves = SlidingMoveHelper.getPossiblePositions(board, queen, Position.D1.getX(), Position.D1.getY(), new Direction(1, 1));

            assertThat(moves, contains(Position.E2, Position.F3, Position.G4));
            assertThat(moves, not(hasItem(Position.H5)));
        }

        @Test
        void enemyInPath_includesCaptureSquareThenStops() {
            //in same position black pawn is on f3 queen should see all positions between and including enemy pawn position
            Board board = BoardTestUtils.emptyBoard();
            Queen queen = PiecesTestFactory.whiteQueen();
            Pawn pawn = PiecesTestFactory.blackPawn(true, false);

            BoardTestUtils.place(board, Position.D1, queen);
            BoardTestUtils.place(board, Position.F3, pawn);

            List<Position> moves = SlidingMoveHelper.getPossiblePositions(board, queen, Position.D1.getX(), Position.D1.getY(), new Direction(1, 1));

            assertThat(moves, contains(Position.E2, Position.F3));
            assertThat(moves, not(hasItem(Position.G4)));
        }

        @Test
        void directionOffBoard_returnsEmptyList() {
            //a black bishop on starting position can't move up left or right
            Board board = BoardTestUtils.emptyBoard();
            Bishop bishop = PiecesTestFactory.blackBishop();

            BoardTestUtils.place(board, Position.C8, bishop);

            List<Position> moves = SlidingMoveHelper.getPossiblePositions(board, bishop, Position.C8.getX(), Position.C8.getY(), new Direction(-1, 1));

            assertThat(moves, is(empty()));
        }
}
