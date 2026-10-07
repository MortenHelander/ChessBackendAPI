package app.gameengine.move_logic;

import app.gameengine.Position;
import app.gameengine.Promotion;
import app.gameengine.exceptions.InvalidPromotionSquareException;
import app.gameengine.exceptions.InvalidPromotionTypeException;
import app.gameengine.pieces.*;
import app.testutils.PiecesTestFactory;
import org.junit.jupiter.api.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PromotionHelperTest {

    @Test
    void whitePawnReachesRank8_shouldBePromotionMove() {
        Pawn pawn = PiecesTestFactory.whitePawn(true, false);
        assertThat(PromotionHelper.isPromotionMove(pawn, Position.E8), is(true));
    }

    @Test
    void whitePawnReachesRank1_shouldNotBePromotionMove() {
        Pawn pawn = PiecesTestFactory.whitePawn(true, false);
        assertThat(PromotionHelper.isPromotionMove(pawn, Position.E1), is(false));
    }

    @Test
    void blackPawnReachesRank1_shouldBePromotionMove() {
        Pawn pawn = PiecesTestFactory.blackPawn(true, false);
        assertThat(PromotionHelper.isPromotionMove(pawn, Position.E1), is(true));
    }

    @Test
    void nonPawnPieceReachesLastRank_shouldNotBePromotionMove() {
        Rook rook = PiecesTestFactory.blackRook(true);
        assertThat(PromotionHelper.isPromotionMove(rook, Position.E1), is(false));
    }

    @Test
    void getPromotionPiece_unrecognizedPieceType_throwsInvalidPromotionTypeException() {
        Promotion promotion = new Promotion("king", Position.E8);
        assertThrows(InvalidPromotionTypeException.class, () -> PromotionHelper.getPromotionPiece(promotion));
    }

    @Test
    void getPromotionPieceLetter_emptyPieceType_throwsInvalidPromotionTypeException() {
        String pieceType = "";
        assertThrows(InvalidPromotionTypeException.class, () -> PromotionHelper.getPromotionPieceLetter(pieceType));
    }

    @Test
    void getPromotionPiece_squareNotOnLastRank_throwsInvalidPromotionSquareException() {
        Promotion promotion = new Promotion("queen", Position.E4);
        assertThrows(InvalidPromotionSquareException.class, () -> PromotionHelper.getPromotionPiece(promotion));
    }

    @Test
    void getPromotionPiece_queenType_whiteRank_returnsWhiteQueen() {
        Promotion promotion = new Promotion("queen", Position.E8);
        Piece result = PromotionHelper.getPromotionPiece(promotion);

        assertThat(result, instanceOf(Queen.class));
        assertThat(result.isWhite(), is(true));
    }

    @Test
    void getPromotionPiece_rookType_returnsRookWithHasMovedTrue() {
        Promotion promotion = new Promotion("rook", Position.E8);
        Piece result = PromotionHelper.getPromotionPiece(promotion);

        Rook rook = (Rook) result;

        assertThat(result, instanceOf(Rook.class));
        assertThat(rook.isHasMoved(), is(true));
    }

    @Test
    void getPromotionPiece_blackRank_returnsBlackPiece() {
        Promotion promotion = new Promotion("bishop", Position.E1);
        Piece result = PromotionHelper.getPromotionPiece(promotion);

        assertThat(result, instanceOf(Bishop.class));
        assertThat(result.isWhite(), is(false));
    }

    @Test
    void getPromotionPieceLetter_knownTypes_returnCorrectLetters() {

        Promotion queen = new Promotion("queen", Position.E8);
        Promotion knight = new Promotion("knight", Position.E1);
        Promotion bishop = new Promotion("bishop", Position.E8);
        Promotion rook = new Promotion("rook", Position.E1);

        assertThat(PromotionHelper.getPromotionPieceLetter(queen.pieceType()), is("q"));
        assertThat(PromotionHelper.getPromotionPieceLetter(knight.pieceType()), is("n"));
        assertThat(PromotionHelper.getPromotionPieceLetter(bishop.pieceType()), is("b"));
        assertThat(PromotionHelper.getPromotionPieceLetter(rook.pieceType()), is("r"));
    }
}