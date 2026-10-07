package app.services;

import app.entities.Move;
import app.exceptions.GameReplayException;
import app.gameengine.Board;
import app.gameengine.Position;
import app.gameengine.PositionConverter;
import app.gameengine.exceptions.InvalidGameActionException;
import app.gameengine.pieces.Pawn;
import app.gameengine.pieces.Queen;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ReplayHelperTest {

    @Test
    void emptyList_returnsStartingBoard() {
        Board board = ReplayHelper.rebuildBoard(List.of());
        assertThat(board.getAllPieces().size(), is(32));
    }

    @Test void normalMove_isApplied() {
        Board board = ReplayHelper.rebuildBoard(moves("e2e4"));
        assertThat(board.getAllPieces().get(Position.E4), instanceOf(Pawn.class));
        assertThat(board.getAllPieces().get(Position.E2), nullValue());
    }

    @Test void promotion_replacesPawnWithChosenPiece() {
        Board board = ReplayHelper.rebuildBoard(setupPlus("a7b8q"));
        assertThat(board.getAllPieces().get(Position.B8), instanceOf(Queen.class));
        assertThat(board.getAllPieces().get(Position.A7), nullValue());
    }

    @Test void emptyFromSquare_throwsWithMoveNumberInMessage() {
        // e2 is empty by the second move: the board has drifted from the stored history
        GameReplayException ex = assertThrows(GameReplayException.class,
                () -> ReplayHelper.rebuildBoard(moves("e2e4", "e2e3")));
        assertThat(ex.getMessage(), containsString("Stored move #2"));
        assertThat(ex.getMessage(), containsString("has no piece on its from-square"));
    }

    @Test void promotionWithoutLetter_throws() {
        GameReplayException ex = assertThrows(GameReplayException.class,
                () -> ReplayHelper.rebuildBoard(setupPlus("a7b8")));        // no letter
        assertThat(ex.getMessage(), containsString("is a promotion but has no promotion letter"));
    }

    @Test void invalidPromotionLetter_isWrappedWithCause() {
        GameReplayException ex = assertThrows(GameReplayException.class,
                () -> ReplayHelper.rebuildBoard(setupPlus("a7b8x")));       // 'x' is not a piece
        assertThat(ex.getMessage(), containsString("failed on replay"));
        assertThat(ex.getCause(), instanceOf(InvalidGameActionException.class));
    }



    private static final List<String> BEFORE_PROMOTION = List.of(
            "a2a4", "b7b5", "a4b5", "a7a6", "b5a6", "c8b7", "a6a7", "h7h6");

    private Move createMove(String uci, int moveNumber) {
        String fromText = uci.substring(0, 2);   // "e2"
        String toText = uci.substring(2, 4);     // "e4"

        String promotionLetter = null;
        if (uci.length() == 5) {
            promotionLetter = uci.substring(4);
        }

        Position from = PositionConverter.fromString(fromText);
        Position to = PositionConverter.fromString(toText);

        Move move = new Move(from, to, promotionLetter);
        move.setMoveNumber(moveNumber);
        return move;
    }

    private List<Move> buildMoves(List<String> ucis) {
        List<Move> moves = new ArrayList<>();
        for (int i = 0; i < ucis.size(); i++) {
            moves.add(createMove(ucis.get(i), i + 1));
        }
        return moves;
    }

    private List<Move> moves(String... ucis) {
        return buildMoves(Arrays.asList(ucis));
    }

    private List<Move> setupPlus(String... extraUcis) {
        List<String> allUcis = new ArrayList<>(BEFORE_PROMOTION);
        allUcis.addAll(Arrays.asList(extraUcis));
        return buildMoves(allUcis);
    }
}
