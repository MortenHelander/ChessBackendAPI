package app.gameengine.move_logic;

import app.gameengine.Board;
import app.gameengine.Position;
import app.gameengine.pieces.Bishop;
import app.gameengine.pieces.King;
import app.gameengine.pieces.Knight;
import app.gameengine.pieces.Rook;
import app.testutils.BoardTestUtils;
import app.testutils.PiecesTestFactory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

class CastlingHelperTest {

    @Test
    void kingSideClearAndSafe_shouldReturnKingSideCandidate() {
        Board board = BoardTestUtils.emptyBoard();
        King king = PiecesTestFactory.whiteKing(false);
        BoardTestUtils.place(board, Position.E1, king);
        Rook rook = PiecesTestFactory.whiteRook(false);
        BoardTestUtils.place(board, Position.H1, rook);

        List<Position> candidates = CastlingHelper.getCastlingMoves(board, king, Position.E1);

        assertThat(candidates, contains(Position.G1));
    }

    @Test
    void squareOnKingsPathIsAttacked_shouldNotOfferKingSideCastle() {
        Board board = BoardTestUtils.emptyBoard();
        King king = PiecesTestFactory.whiteKing(false);
        BoardTestUtils.place(board, Position.E1, king);
        Rook rook = PiecesTestFactory.whiteRook(false);
        BoardTestUtils.place(board, Position.H1, rook);
        // a black knight that attacks G1 (the king's landing square) without touching anything else on the path
        Knight blackKnight = PiecesTestFactory.blackKnight();
        BoardTestUtils.place(board, Position.E2, blackKnight);

        List<Position> candidates = CastlingHelper.getCastlingMoves(board, king, Position.E1);

        assertThat(candidates, not(hasItem(Position.G1)));
    }

    @Test
    void queenSideClearAndSafe_shouldReturnQueenSideCandidate() {
        // King E1, Rook A1, B1/C1/D1 all empty
        Board board = BoardTestUtils.emptyBoard();
        King king = PiecesTestFactory.whiteKing(false);
        BoardTestUtils.place(board, Position.E1, king);
        Rook rook = PiecesTestFactory.whiteRook(false);
        BoardTestUtils.place(board, Position.A1, rook);

        List<Position> candidates = CastlingHelper.getCastlingMoves(board, king, Position.E1);

        assertThat(candidates, contains(Position.C1));
    }

    @Test
    void kingHasMoved_shouldReturnNoCandidatesEitherSide() {
        Board board = BoardTestUtils.emptyBoard();
        King king = PiecesTestFactory.whiteKing(true);
        BoardTestUtils.place(board, Position.E1, king);
        Rook queenSideRook = PiecesTestFactory.whiteRook(false);
        BoardTestUtils.place(board, Position.A1, queenSideRook);
        Rook kingSideRook = PiecesTestFactory.whiteRook(false);
        BoardTestUtils.place(board, Position.H1, kingSideRook);

        List<Position> candidates = CastlingHelper.getCastlingMoves(board, king, Position.E1);

        //queen side should be unavailable but kingside is available
        assertThat(candidates, not(hasItem(Position.C1)));
        assertThat(candidates, not(hasItem(Position.G1)));
    }

    @Test
    void rookHasMoved_shouldOnlyBlockThatSide() {
        Board board = BoardTestUtils.emptyBoard();
        King king = PiecesTestFactory.whiteKing(false);
        BoardTestUtils.place(board, Position.E1, king);
        Rook queenSideRook = PiecesTestFactory.whiteRook(true);
        BoardTestUtils.place(board, Position.A1, queenSideRook);
        Rook kingSideRook = PiecesTestFactory.whiteRook(false);
        BoardTestUtils.place(board, Position.H1, kingSideRook);

        List<Position> candidates = CastlingHelper.getCastlingMoves(board, king, Position.E1);

        assertThat(candidates, contains(Position.G1));
        assertThat(candidates, not(hasItem(Position.C1)));
    }

    @Test
    void pieceBlockingBetweenKingAndRook_shouldExcludeThatSide() {

        Board board = BoardTestUtils.emptyBoard();
        King king = PiecesTestFactory.whiteKing(false);
        BoardTestUtils.place(board, Position.E1, king);
        Rook queenSideRook = PiecesTestFactory.whiteRook(true);
        BoardTestUtils.place(board, Position.A1, queenSideRook);
        Bishop enemyBishop = PiecesTestFactory.blackBishop();
        BoardTestUtils.place(board, Position.A3, enemyBishop);
        List<Position> enemyBishopPossibleMoves = enemyBishop.getPossibleMoves(board, Position.A3);

        List<Position> candidates = CastlingHelper.getCastlingMoves(board, king, Position.E1);

        //enemy bishop is attacking the path so no castling available
        assertThat(enemyBishopPossibleMoves, is(hasItem(Position.C1)));
        assertThat(candidates, not(hasItem(Position.C1)));
    }

    @Test
    void kingCurrentlyInCheck_shouldReturnNoCandidatesEvenIfPathsAreClear() {
        // enemy rook directly checking the king via an open file/rank
        Board board = BoardTestUtils.emptyBoard();
        King king = PiecesTestFactory.whiteKing(false);
        BoardTestUtils.place(board, Position.E1, king);
        Rook kingSideRook = PiecesTestFactory.whiteRook(false);
        BoardTestUtils.place(board, Position.H1, kingSideRook);
        Rook enemyRook = PiecesTestFactory.blackRook(true);
        BoardTestUtils.place(board, Position.E7, enemyRook);

        List<Position> enemyRookPossibleMoves = enemyRook.getPossibleMoves(board, Position.E7);
        List<Position> candidates = CastlingHelper.getCastlingMoves(board, king, Position.E1);

        assertThat(enemyRookPossibleMoves, is(hasItem(Position.E1)));
        assertThat(candidates, not(hasItem(Position.G1)));
    }
}