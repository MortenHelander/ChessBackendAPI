package app.gameengine;

import app.exceptions.ApiException;
import app.gameengine.pieces.*;
import app.services.MoveValidator;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FenParserTest {

    private static final String START_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
    // daily puzzle from LichessClientTest
    private static final String PUZZLE_FEN = "8/3Q4/5qkp/3p4/1p2n1B1/1Pr4P/P1P2bP1/2R2K2 w - - 1 1";
    // white just played e2e4, black pawn on d4 can take en passant
    private static final String EN_PASSANT_FEN = "4k3/8/8/8/3pP3/8/8/4K3 b - e3 0 1";


    // ---------- start position ----------

    @Test
    void startPosition_matchesNewBoard() {
        Board expected = new Board();
        expected.initializeNewBoard();

        Board board = FenParser.fromFen(START_FEN).board();

        assertThat(board.getAllPieces().size(), is(32));
        for (Map.Entry<Position, Piece> entry : expected.getAllPieces().entrySet()) {
            Piece expectedPiece = entry.getValue();
            Piece actualPiece = board.getAllPieces().get(entry.getKey());

            assertThat("piece on " + entry.getKey(), actualPiece, instanceOf(expectedPiece.getClass()));
            assertThat("color on " + entry.getKey(), actualPiece.isWhite(), is(expectedPiece.isWhite()));
        }
    }

    @Test
    void startPosition_turnAndCountersAreRead() {
        FenGameState state = FenParser.fromFen(START_FEN);

        assertThat(state.isWhitesTurn(), is(true));
        assertThat(state.halfMove(), is(0));
        assertThat(state.fullMove(), is(1));
    }

    @Test
    void startPosition_pawnsHaveNotMoved() {
        Board board = FenParser.fromFen(START_FEN).board();

        Pawn whitePawn = (Pawn) board.getAllPieces().get(Position.E2);
        Pawn blackPawn = (Pawn) board.getAllPieces().get(Position.D7);

        assertThat(whitePawn.isHasMoved(), is(false));
        assertThat(blackPawn.isHasMoved(), is(false));
        assertThat(whitePawn.isEnPassantTakeable(), is(false));
    }

    @Test
    void startPositionFullCastlingRights_kingsAndRooksHaveNotMoved() {
        Board board = FenParser.fromFen(START_FEN).board();

        assertThat(((King) board.getAllPieces().get(Position.E1)).isHasMoved(), is(false));
        assertThat(((King) board.getAllPieces().get(Position.E8)).isHasMoved(), is(false));
        assertThat(((Rook) board.getAllPieces().get(Position.A1)).isHasMoved(), is(false));
        assertThat(((Rook) board.getAllPieces().get(Position.H1)).isHasMoved(), is(false));
        assertThat(((Rook) board.getAllPieces().get(Position.A8)).isHasMoved(), is(false));
        assertThat(((Rook) board.getAllPieces().get(Position.H8)).isHasMoved(), is(false));
    }


    // ---------- puzzle position ----------

    @Test
    void puzzleFen_placesPiecesOnCorrectSquares() {
        Board board = FenParser.fromFen(PUZZLE_FEN).board();

        assertThat(board.getAllPieces().size(), is(17));

        Piece queen = board.getAllPieces().get(Position.D7);
        assertThat(queen, instanceOf(Queen.class));
        assertThat(queen.isWhite(), is(true));

        Piece king = board.getAllPieces().get(Position.G6);
        assertThat(king, instanceOf(King.class));
        assertThat(king.isWhite(), is(false));

        Piece bishop = board.getAllPieces().get(Position.G4);
        assertThat(bishop, instanceOf(Bishop.class));
        assertThat(bishop.isWhite(), is(true));

        Piece knight = board.getAllPieces().get(Position.E4);
        assertThat(knight, instanceOf(Knight.class));
        assertThat(knight.isWhite(), is(false));

        assertThat(board.getAllPieces().get(Position.E1), nullValue());
    }

    @Test
    void puzzleFen_turnAndCountersAreRead() {
        FenGameState state = FenParser.fromFen(PUZZLE_FEN);

        assertThat(state.isWhitesTurn(), is(true));
        assertThat(state.halfMove(), is(1));
        assertThat(state.fullMove(), is(1));
    }

    @Test
    void noCastlingRights_kingsAndRooksHaveMoved() {
        Board board = FenParser.fromFen(PUZZLE_FEN).board();

        assertThat(((King) board.getAllPieces().get(Position.F1)).isHasMoved(), is(true));
        assertThat(((King) board.getAllPieces().get(Position.G6)).isHasMoved(), is(true));
        assertThat(((Rook) board.getAllPieces().get(Position.C1)).isHasMoved(), is(true));
    }

    @Test
    void pawnOffStartRank_hasMoved() {
        Board board = FenParser.fromFen(PUZZLE_FEN).board();

        // white pawn on B3 and black pawn on B4 are both off their start rank
        Pawn whitePawn = (Pawn) board.getAllPieces().get(Position.B3);
        Pawn blackPawn = (Pawn) board.getAllPieces().get(Position.B4);
        // white pawn on A2 is still on its start rank
        Pawn startRankPawn = (Pawn) board.getAllPieces().get(Position.A2);

        assertThat(whitePawn.isHasMoved(), is(true));
        assertThat(blackPawn.isHasMoved(), is(true));
        assertThat(startRankPawn.isHasMoved(), is(false));
    }


    // ---------- turn, castling, counters ----------

    @Test
    void blackToMove_isWhitesTurnFalse() {
        FenGameState state = FenParser.fromFen("4k3/8/8/8/8/8/8/4K3 b - - 0 1");

        assertThat(state.isWhitesTurn(), is(false));
    }

    @Test
    void partialCastlingRights_onlyListedRooksHaveNotMoved() {
        // white may castle king side, black queen side
        Board board = FenParser.fromFen("r3k2r/8/8/8/8/8/8/R3K2R w Kq - 0 1").board();

        assertThat(((King) board.getAllPieces().get(Position.E1)).isHasMoved(), is(false));
        assertThat(((Rook) board.getAllPieces().get(Position.H1)).isHasMoved(), is(false));
        assertThat(((Rook) board.getAllPieces().get(Position.A1)).isHasMoved(), is(true));

        assertThat(((King) board.getAllPieces().get(Position.E8)).isHasMoved(), is(false));
        assertThat(((Rook) board.getAllPieces().get(Position.A8)).isHasMoved(), is(false));
        assertThat(((Rook) board.getAllPieces().get(Position.H8)).isHasMoved(), is(true));
    }

    @Test
    void castlingRightsFromFen_kingCanCastle() {
        Board board = FenParser.fromFen("4k3/8/8/8/8/8/8/4K2R w K - 0 1").board();
        Piece king = board.getAllPieces().get(Position.E1);

        List<Position> moves = MoveValidator.getLegalMoves(board, king, Position.E1);

        assertThat(moves, hasItem(Position.G1));
    }

    @Test
    void missingHalfAndFullMove_defaultsAreUsed() {
        FenGameState state = FenParser.fromFen("4k3/8/8/8/8/8/8/4K3 w - -");

        assertThat(state.halfMove(), is(0));
        assertThat(state.fullMove(), is(1));
    }

    @Test
    void extraWhitespace_isIgnored() {
        FenGameState state = FenParser.fromFen("  4k3/8/8/8/8/8/8/4K3   b  -  -  0  1  ");

        assertThat(state.isWhitesTurn(), is(false));
        assertThat(state.board().getAllPieces().size(), is(2));
    }


    // ---------- en passant ----------

    @Test
    void enPassantTarget_marksPawnAsTakeable() {
        Board board = FenParser.fromFen(EN_PASSANT_FEN).board();

        Pawn pawn = (Pawn) board.getAllPieces().get(Position.E4);

        assertThat(pawn.isEnPassantTakeable(), is(true));
    }

    @Test
    void enPassantTarget_blackPawnCanCaptureEnPassant() {
        Board board = FenParser.fromFen(EN_PASSANT_FEN).board();
        Piece blackPawn = board.getAllPieces().get(Position.D4);

        List<Position> moves = MoveValidator.getLegalMoves(board, blackPawn, Position.D4);

        assertThat(moves, hasItem(Position.E3));
    }

    @Test
    void enPassantTarget_flagIsClearedAfterOpponentMoves() {
        Board board = FenParser.fromFen(EN_PASSANT_FEN).board();
        Pawn whitePawn = (Pawn) board.getAllPieces().get(Position.E4);
        Piece blackKing = board.getAllPieces().get(Position.E8);

        // black doesn't take en passant, so the chance should be gone
        board.move(blackKing, Position.E8, Position.D8);

        assertThat(whitePawn.isEnPassantTakeable(), is(false));
    }


    // ---------- invalid input ----------

    @Test
    void nullFen_throws() {
        ApiException ex = assertThrows(ApiException.class, () -> FenParser.fromFen(null));
        assertThat(ex.getCode(), is(400));
    }

    @Test
    void blankFen_throws() {
        ApiException ex = assertThrows(ApiException.class, () -> FenParser.fromFen("   "));
        assertThat(ex.getCode(), is(400));
    }

    @Test
    void tooFewParts_throws() {
        ApiException ex = assertThrows(ApiException.class,
                () -> FenParser.fromFen("4k3/8/8/8/8/8/8/4K3 w -"));
        assertThat(ex.getCode(), is(400));
    }

    @Test
    void sevenRows_throws() {
        ApiException ex = assertThrows(ApiException.class,
                () -> FenParser.fromFen("4k3/8/8/8/8/8/4K3 w - - 0 1"));
        assertThat(ex.getCode(), is(400));
    }

    @Test
    void rowWithTooManyPieces_throws() {
        ApiException ex = assertThrows(ApiException.class,
                () -> FenParser.fromFen("4k3/ppppppppp/8/8/8/8/8/4K3 w - - 0 1"));
        assertThat(ex.getCode(), is(400));
    }

    @Test
    void rowWithTooManyEmptySquares_throws() {
        ApiException ex = assertThrows(ApiException.class,
                () -> FenParser.fromFen("4k3/9/8/8/8/8/8/4K3 w - - 0 1"));
        assertThat(ex.getCode(), is(400));
        assertThat(ex.getMessage(), containsString("Rank 7"));
    }

    @Test
    void rowTooShort_throws() {
        ApiException ex = assertThrows(ApiException.class,
                () -> FenParser.fromFen("4k3/7/8/8/8/8/8/4K3 w - - 0 1"));
        assertThat(ex.getCode(), is(400));
        assertThat(ex.getMessage(), containsString("Rank 7"));
    }

    @Test
    void invalidPieceLetter_throws() {
        ApiException ex = assertThrows(ApiException.class,
                () -> FenParser.fromFen("4k3/8/8/3x4/8/8/8/4K3 w - - 0 1"));
        assertThat(ex.getCode(), is(400));
        assertThat(ex.getMessage(), containsString("x"));
    }

    @Test
    void invalidTurn_throws() {
        ApiException ex = assertThrows(ApiException.class,
                () -> FenParser.fromFen("4k3/8/8/8/8/8/8/4K3 z - - 0 1"));
        assertThat(ex.getCode(), is(400));
        assertThat(ex.getMessage(), containsString("z"));
    }

    @Test
    void invalidCastlingLetter_throws() {
        ApiException ex = assertThrows(ApiException.class,
                () -> FenParser.fromFen("r3k2r/8/8/8/8/8/8/R3K2R w KX - 0 1"));
        assertThat(ex.getCode(), is(400));
    }

    @Test
    void castlingRightsWithoutRook_throws() {
        // FEN says white can castle king side, but there is no rook on H1
        ApiException ex = assertThrows(ApiException.class,
                () -> FenParser.fromFen("4k3/8/8/8/8/8/8/4K3 w K - 0 1"));
        assertThat(ex.getCode(), is(400));
    }

    @Test
    void enPassantOnWrongRank_throws() {
        ApiException ex = assertThrows(ApiException.class,
                () -> FenParser.fromFen("4k3/8/8/8/3pP3/8/8/4K3 b - e4 0 1"));
        assertThat(ex.getCode(), is(400));
    }

    @Test
    void enPassantWithoutPawn_throws() {
        ApiException ex = assertThrows(ApiException.class,
                () -> FenParser.fromFen("4k3/8/8/8/8/8/8/4K3 b - e3 0 1"));
        assertThat(ex.getCode(), is(400));
    }

    @Test
    void enPassantPawnWrongColor_throws() {
        // target e3 means a white pawn on E4, but the pawn there is black
        ApiException ex = assertThrows(ApiException.class,
                () -> FenParser.fromFen("4k3/8/8/8/4p3/8/8/4K3 b - e3 0 1"));
        assertThat(ex.getCode(), is(400));
    }

    @Test
    void halfMoveNotANumber_throws() {
        ApiException ex = assertThrows(ApiException.class,
                () -> FenParser.fromFen("4k3/8/8/8/8/8/8/4K3 w - - abc 1"));
        assertThat(ex.getCode(), is(400));
    }

    @Test
    void negativeHalfMove_throws() {
        ApiException ex = assertThrows(ApiException.class,
                () -> FenParser.fromFen("4k3/8/8/8/8/8/8/4K3 w - - -1 1"));
        assertThat(ex.getCode(), is(400));
    }

    @Test
    void fullMoveZero_throws() {
        ApiException ex = assertThrows(ApiException.class,
                () -> FenParser.fromFen("4k3/8/8/8/8/8/8/4K3 w - - 0 0"));
        assertThat(ex.getCode(), is(400));
    }
}