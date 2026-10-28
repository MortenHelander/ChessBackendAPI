package app.gameengine.game_logic;

import app.entities.Move;
import app.gameengine.FenGameState;
import app.gameengine.gameservices.FenParser;
import app.gameengine.gameservices.UciParser;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class DrawCheckerTest {

    //tests here will say what pieces are left and the expected value of the return value

    // knights jump out and back again: after these 4 half-moves the board is the same as before
    private static final List<String> KINGSIDE_KNIGHT_SHUFFLE = List.of("g1f3", "g8f6", "f3g1", "f6g8");
    private static final List<String> QUEENSIDE_KNIGHT_SHUFFLE = List.of("b1c3", "b8c6", "c3b1", "c6b8");

    // ---------- insufficient material ----------
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

    // ---------- stalemate ----------

    @Test
    void isStaleMate_blackKingHasNoMovesAndIsNotInCheck_shouldReturnTrue() {
        // black king on a8, white queen on b6 covers every square around it, black to move
        FenGameState state = FenParser.fromFen("k7/8/1Q6/8/8/8/8/2K5 b - - 0 1");

        assertThat(DrawChecker.isStaleMate(state.board(), false), is(true));
    }

    @Test
    void isStaleMate_startingBoard_shouldReturnFalse() {
        FenGameState state = FenParser.fromFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");

        assertThat(DrawChecker.isStaleMate(state.board(), true), is(false));
    }

    @Test
    void isStaleMate_checkmate_shouldReturnFalse() {
        // same king on a8, but the queen on b7 gives check: that is checkmate, not stalemate
        FenGameState state = FenParser.fromFen("k7/1Q6/1K6/8/8/8/8/8 b - - 0 1");

        assertThat(DrawChecker.isStaleMate(state.board(), false), is(false));
    }


    // ---------- fifty-move rule ----------

    @Test
    void isFiftyMoveRule_hundredHalfMovesWithoutPawnMoveOrCapture_shouldReturnTrue() {
        List<Move> moves = UciParser.toMoves(repeat(QUEENSIDE_KNIGHT_SHUFFLE, 25)); // 25 x 4 = 100 half-moves

        assertThat(DrawChecker.isFiftyMoveRule(moves), is(true));
    }

    @Test
    void isFiftyMoveRule_ninetyNineHalfMoves_shouldReturnFalse() {
        List<String> uciMoves = repeat(QUEENSIDE_KNIGHT_SHUFFLE, 25).subList(0, 99);

        assertThat(DrawChecker.isFiftyMoveRule(UciParser.toMoves(uciMoves)), is(false));
    }

    @Test
    void isFiftyMoveRule_pawnMoveResetsTheCount_shouldReturnFalse() {
        // 98 quiet half-moves leave the knights on c3 and c6, then a pawn move resets the count to 0
        List<String> uciMoves = new ArrayList<>(repeat(QUEENSIDE_KNIGHT_SHUFFLE, 25).subList(0, 98));
        uciMoves.add("e2e4");
        uciMoves.add("c6b8");

        assertThat(DrawChecker.isFiftyMoveRule(UciParser.toMoves(uciMoves)), is(false));
    }

    @Test
    void isFiftyMoveRule_noMoves_shouldReturnFalse() {
        assertThat(DrawChecker.isFiftyMoveRule(List.of()), is(false));
    }


    // ---------- threefold repetition ----------

    @Test
    void isThreefoldRepetition_startPositionThreeTimes_shouldReturnTrue() {
        // start position + after 1st shuffle + after 2nd shuffle = 3 times
        List<Move> moves = UciParser.toMoves(repeat(KINGSIDE_KNIGHT_SHUFFLE, 2));

        assertThat(DrawChecker.isThreefoldRepetition(moves), is(true));
    }

    @Test
    void isThreefoldRepetition_startPositionTwice_shouldReturnFalse() {
        List<Move> moves = UciParser.toMoves(repeat(KINGSIDE_KNIGHT_SHUFFLE, 1));

        assertThat(DrawChecker.isThreefoldRepetition(moves), is(false));
    }

    @Test
    void isThreefoldRepetition_oneMoveShortOfThirdTime_shouldReturnFalse() {
        List<String> uciMoves = repeat(KINGSIDE_KNIGHT_SHUFFLE, 2).subList(0, 7);

        assertThat(DrawChecker.isThreefoldRepetition(UciParser.toMoves(uciMoves)), is(false));
    }

    @Test
    void isThreefoldRepetition_enPassantNotPossible_positionsCountAsTheSame_shouldReturnTrue() {
        // after e7e5 the pawn could in theory be taken en passant, but no white pawn stands next to it,
        // so the position counts as the same as the ones after each shuffle
        List<String> uciMoves = new ArrayList<>(List.of("e2e4", "e7e5"));
        uciMoves.addAll(repeat(KINGSIDE_KNIGHT_SHUFFLE, 2));

        assertThat(DrawChecker.isThreefoldRepetition(UciParser.toMoves(uciMoves)), is(true));
    }

    @Test
    void isThreefoldRepetition_enPassantPossible_positionsAreDifferent_shouldNeedOneMoreRepetition() {
        // after d7d5 the white pawn on e5 CAN take en passant, so that position is different
        // from the later ones where the chance is gone
        List<String> start = List.of("e2e4", "a7a6", "e4e5", "d7d5");

        List<String> twoShuffles = new ArrayList<>(start);
        twoShuffles.addAll(repeat(KINGSIDE_KNIGHT_SHUFFLE, 2));
        assertThat(DrawChecker.isThreefoldRepetition(UciParser.toMoves(twoShuffles)), is(false));

        List<String> threeShuffles = new ArrayList<>(start);
        threeShuffles.addAll(repeat(KINGSIDE_KNIGHT_SHUFFLE, 3));
        assertThat(DrawChecker.isThreefoldRepetition(UciParser.toMoves(threeShuffles)), is(true));
    }


    // ---------- helpers ----------

    private static List<String> repeat(List<String> uciMoves, int times) {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < times; i++) {
            result.addAll(uciMoves);
        }
        return result;
    }
}