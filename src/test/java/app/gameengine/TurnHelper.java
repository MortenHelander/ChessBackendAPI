package app.gameengine;

import app.gameengine.pieces.King;
import app.gameengine.pieces.Pawn;
import app.gameengine.pieces.Rook;
import app.testutils.PiecesTestFactory;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class TurnHelperTest {

    private final TurnHelper turnHelper = new TurnHelper();

    @Test
    void endTurn_kingMoves_setsHasMovedTrue() {
        King king = PiecesTestFactory.whiteKing(false);

        turnHelper.endTurn(king, Position.E2);

        assertThat(king.isHasMoved(), is(true));
    }

    @Test
    void endTurn_rookMoves_setsHasMovedTrue() {
        Rook rook = PiecesTestFactory.whiteRook(false);

        turnHelper.endTurn(rook, Position.A4);

        assertThat(rook.isHasMoved(), is(true));
    }

    @Test
    void endTurn_pawnDoubleStepOpeningMove_setsHasMovedAndEnPassantTakeable() {
        Pawn pawn = PiecesTestFactory.whitePawn(false, false);

        turnHelper.endTurn(pawn, Position.A4);

        assertThat(pawn.isHasMoved(), is(true));
        assertThat(pawn.isEnPassantTakeable(), is(true));
    }

    @Test
    void endTurn_pawnSingleStepMove_setsHasMovedTrueEnPassantFalse() {
        Pawn pawn = PiecesTestFactory.blackPawn(false, false);

        turnHelper.endTurn(pawn, Position.A6);

        assertThat(pawn.isHasMoved(), is(true));
        assertThat(pawn.isEnPassantTakeable(), is(false));
    }

    @Test
    void endTurn_opponentPawnWasEnPassantTakeable_getsClearedAfterNextTurn() {
        Pawn whitePawn = PiecesTestFactory.whitePawn(false, false);
        turnHelper.endTurn(whitePawn, Position.A4); // white double-steps, becomes en passant takeable

        assertThat(whitePawn.isEnPassantTakeable(), is(true)); // check on step 1

        Rook blackRook = PiecesTestFactory.blackRook(false);
        turnHelper.endTurn(blackRook, Position.H6); // black makes an unrelated move

        assertThat(whitePawn.isEnPassantTakeable(), is(false));
    }

    @Test
    void endTurn_symmetricForBlack_getsClearedAfterWhiteMoves() {
        Pawn blackPawn = PiecesTestFactory.blackPawn(false, false);
        turnHelper.endTurn(blackPawn, Position.A5); // black double-steps

        assertThat(blackPawn.isEnPassantTakeable(), is(true));

        King whiteKing = PiecesTestFactory.whiteKing(false);
        turnHelper.endTurn(whiteKing, Position.E2);

        assertThat(blackPawn.isEnPassantTakeable(), is(false));
    }

    @Test
    void endTurn_opponentLastMovedPieceWasNotAPawn_noErrorAndNoSideEffect() {
        Rook whiteRook = PiecesTestFactory.whiteRook(false);
        turnHelper.endTurn(whiteRook, Position.A4);

        King blackKing = PiecesTestFactory.blackKing(false);
        // should not throw despite opponent's last move not being a pawn
        turnHelper.endTurn(blackKing, Position.E7);

        assertThat(blackKing.isHasMoved(), is(true));
    }

    @Test
    void endTurn_firstMoveOfTheGame_noPriorOpponentMove_doesNotThrow() {
        Pawn firstPawn = PiecesTestFactory.whitePawn(false, false);

        turnHelper.endTurn(firstPawn, Position.E4);

        assertThat(firstPawn.isHasMoved(), is(true));
    }
}