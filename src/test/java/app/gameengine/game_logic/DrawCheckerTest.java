package app.gameengine.game_logic;

import app.gameengine.FenGameState;
import app.gameengine.gameservices.FenParser;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class DrawCheckerTest {

    //tests here will say what pieces are left and the expected value of the return value
    @Test
    void isInsufficientMaterial_startingBoard_shouldReturnFalse() {
        FenGameState state = FenParser.fromFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");

        assertThat(DrawChecker.isInsufficientMaterial(state.board()), is(false));
    }

    @Test
    void isInsufficientMaterial_kingVsKing_shouldReturnTrue() {
        FenGameState state = FenParser.fromFen("4k3/8/8/8/8/8/8/4K3 w - - 0 1");

        assertThat(DrawChecker.isInsufficientMaterial(state.board()), is(true));
    }

    @Test
    void isInsufficientMaterial_kingAndBishopVsKing_shouldReturnTrue() {
        FenGameState state = FenParser.fromFen("4k3/8/8/8/8/8/8/2B1K3 w - - 0 1");

        assertThat(DrawChecker.isInsufficientMaterial(state.board()), is(true));
    }

    @Test
    void isInsufficientMaterial_kingAndKnightVsKing_shouldReturnTrue() {
        FenGameState state = FenParser.fromFen("4k3/8/8/8/8/8/8/1N2K3 w - - 0 1");

        assertThat(DrawChecker.isInsufficientMaterial(state.board()), is(true));
    }

    @Test
    void isInsufficientMaterial_kingAndBishopVsVsKingAndBishop_sameSquareColor_shouldReturnTrue() {
        FenGameState state = FenParser.fromFen("3bk3/8/8/8/8/8/8/2B1K3 w - - 0 1");

        assertThat(DrawChecker.isInsufficientMaterial(state.board()), is(true));
    }

    @Test
    void isInsufficientMaterial_kingAndBishopVsVsKingAndBishop_notSameSquareColor_shouldReturnFalse() {
        FenGameState state = FenParser.fromFen("2b1k3/8/8/8/8/8/8/2B1K3 w - - 0 1");

        assertThat(DrawChecker.isInsufficientMaterial(state.board()), is(false));
    }

    @Test
    void isInsufficientMaterial_kingAndRookVsKing_shouldReturnFalse() {
        FenGameState state = FenParser.fromFen("4k3/8/8/8/8/8/8/R3K3 w - - 0 1");

        assertThat(DrawChecker.isInsufficientMaterial(state.board()), is(false));
    }
}
