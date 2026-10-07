package app.gameengine.move_logic;

import app.gameengine.Board;
import app.gameengine.Position;
import app.gameengine.pieces.King;
import app.gameengine.pieces.Piece;
import app.testutils.BoardTestUtils;
import app.testutils.PiecesTestFactory;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.*;

class PieceFinderTest {

    @Test
    void findAllyKingPosition_noKingPresent_returnsNull() {
        Map<Position, Piece> pieces = Map.of(Position.A1, PiecesTestFactory.whiteRook(true));
        assertThat(PieceFinder.findAllyKingPosition(pieces), is(nullValue()));
    }

    @Test
    void findAllyKingPosition_kingPresent_returnsCorrectPosition() {
        Board board = BoardTestUtils.emptyBoard();
        King king = PiecesTestFactory.whiteKing(true);

        BoardTestUtils.place(board, Position.H4, king);

        assertThat(PieceFinder.findAllyKingPosition(board.getAllPieces()), is(Position.H4));
    }
}
