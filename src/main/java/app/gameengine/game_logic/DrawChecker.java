package app.gameengine.game_logic;

import app.entities.Move;
import app.gameengine.Board;
import app.gameengine.Position;
import app.gameengine.gameservices.PositionConverter;
import app.gameengine.pieces.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DrawChecker {


    private static final int FIFTY_MOVE_LIMIT = 100; // 50 moves each = 100 half-moves
    private static final int REPETITION_LIMIT = 3;

    // ---------- stalemate ----------

    // no legal moves, but the king is not in check
    public static boolean isStaleMate(Board board, boolean isWhitesTurn) {
        return CheckChecker.hasNoLegalMoves(board, isWhitesTurn) && !CheckChecker.isSideInCheck(board, isWhitesTurn);
    }

    public static boolean isInsufficientMaterial(Board board) {
        List<Map.Entry<Position, Piece>> nonKingPieces = new ArrayList<>();
        for (Map.Entry<Position, Piece> entry : board.getAllPieces().entrySet()) {
            if (!(entry.getValue() instanceof King)) {
                nonKingPieces.add(entry); //adds everything but the kings since those are always present
            }
        }
        if (nonKingPieces.isEmpty()) {
            return true;
        }
        if (nonKingPieces.size() == 1 && (nonKingPieces.getFirst().getValue() instanceof Knight || nonKingPieces.getFirst().getValue() instanceof Bishop)) {
            return true;
        }
        if (nonKingPieces.size() == 2 && nonKingPieces.getFirst().getValue() instanceof Bishop bishop && nonKingPieces.get(1).getValue()
                instanceof Bishop bishop2 && bishop.isWhite() != bishop2.isWhite()) {
            Position p1 = nonKingPieces.getFirst().getKey();
            Position p2 = nonKingPieces.get(1).getKey();

            //modolus to find even or uneven
            int p1SquareColor = (p1.getX() + p1.getY()) % 2;
            int p2SquareColor = (p2.getX() + p2.getY()) % 2;
            return p1SquareColor == p2SquareColor;
        }
        return false;
    }

    public static boolean isFiftyMoveRule(List<Move> moves) {
        Board board = new Board();
        board.initializeNewBoard();
        int halfMoveClock = 0;

        for (Move move : moves) {
            Piece piece = board.getAllPieces().get(move.getFrom());
            boolean isPawnMove = piece instanceof Pawn;
            boolean isCapture = board.getAllPieces().containsKey(move.getTo());

            if (isPawnMove || isCapture) {
                halfMoveClock = 0;
            } else {
                halfMoveClock++;
            }
            applyMove(board, move);
        }
        return halfMoveClock >= FIFTY_MOVE_LIMIT;
    }

    // ---------- threefold repetition ----------

    // the same position (pieces, side to move, castling rights, en passant) has occurred 3 times
    public static boolean isThreefoldRepetition(List<Move> moves) {
        Board board = new Board();
        board.initializeNewBoard();
        boolean isWhitesTurn = true;

        Map<String, Integer> seenPositions = new HashMap<>();
        seenPositions.put(positionKey(board, isWhitesTurn), 1); // the start position counts once

        for (Move move : moves) {
            applyMove(board, move);
            isWhitesTurn = !isWhitesTurn;

            String key = positionKey(board, isWhitesTurn);
            int timesSeen = seenPositions.getOrDefault(key, 0) + 1;
            seenPositions.put(key, timesSeen);

            if (timesSeen >= REPETITION_LIMIT) {
                return true;
            }
        }
        return false;
    }

    // ---------- helpers ----------

    // makes one stored move on the board, including promotion
    private static void applyMove(Board board, Move move) {
        Piece piece = board.getAllPieces().get(move.getFrom());
        MoveResult result = board.move(piece, move.getFrom(), move.getTo());

        if (result.isAwaitingPromotion()) {
            String pieceType = PromotionHelper.getPromotionPieceType(move.getPromotionLetter());
            board.promotion(new Promotion(pieceType, move.getTo()));
        }
    }

    // a text that is equal for two positions exactly when they count as "the same position"
    private static String positionKey(Board board, boolean isWhitesTurn) {
        StringBuilder key = new StringBuilder();

        for (Position position : Position.values()) {
            Piece piece = board.getAllPieces().get(position);
            key.append(pieceLetter(piece));
        }

        key.append(isWhitesTurn ? " w " : " b ");

        if (hasNotMoved(board, Position.E1) && hasNotMoved(board, Position.H1)) key.append("K");
        if (hasNotMoved(board, Position.E1) && hasNotMoved(board, Position.A1)) key.append("Q");
        if (hasNotMoved(board, Position.E8) && hasNotMoved(board, Position.H8)) key.append("k");
        if (hasNotMoved(board, Position.E8) && hasNotMoved(board, Position.A8)) key.append("q");

        for (Map.Entry<Position, Piece> entry : board.getAllPieces().entrySet()) {
            if (entry.getValue() instanceof Pawn pawn && pawn.isEnPassantTakeable()
                    && hasEnemyPawnBeside(board, entry.getKey(), pawn)) {
                key.append(" ep:").append(entry.getKey());
            }
        }
        return key.toString();
    }

    // en passant only matters for the position if an enemy pawn stands right next to the pawn
    private static boolean hasEnemyPawnBeside(Board board, Position position, Pawn pawn) {
        Position left = PositionConverter.fromCoordinates(position.getX() - 1, position.getY());
        Position right = PositionConverter.fromCoordinates(position.getX() + 1, position.getY());
        return isEnemyPawn(board, left, pawn) || isEnemyPawn(board, right, pawn);
    }

    private static boolean isEnemyPawn(Board board, Position position, Pawn pawn) {
        if (position == null) { // off the board
            return false;
        }
        Piece piece = board.getAllPieces().get(position);
        return piece instanceof Pawn && piece.isWhite() != pawn.isWhite();
    }

    // uppercase for white, lowercase for black, "." for an empty square
    private static char pieceLetter(Piece piece) {
        if (piece == null) {
            return '.';
        }
        char letter;
        if (piece instanceof Pawn) letter = 'p';
        else if (piece instanceof Knight) letter = 'n';
        else if (piece instanceof Bishop) letter = 'b';
        else if (piece instanceof Rook) letter = 'r';
        else if (piece instanceof Queen) letter = 'q';
        else letter = 'k';

        return piece.isWhite() ? Character.toUpperCase(letter) : letter;
    }

    // true if the square holds a king or rook that has never moved
    private static boolean hasNotMoved(Board board, Position position) {
        Piece piece = board.getAllPieces().get(position);
        if (piece instanceof King king) return !king.isHasMoved();
        if (piece instanceof Rook rook) return !rook.isHasMoved();
        return false;
    }
}
