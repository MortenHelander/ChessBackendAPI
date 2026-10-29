package app.gameengine.gameservices;

import app.exceptions.ApiException;
import app.gameengine.Board;
import app.gameengine.FenGameState;
import app.gameengine.Position;
import app.gameengine.pieces.*;

import java.util.HashMap;
import java.util.Map;

public class FenParser {

    public static FenGameState fromFen(String fen) {
        if (fen == null || fen.isBlank()) {
            throw new ApiException(400, "FEN string is empty");
        }
        String[] parts = fen.trim().split("\\s+"); //fen example string: r1b2r2/1p1n1ppk/4p3/b2pB3/p2P4/q1P1R3/P2Q1PPP/R5K1 w - - 0 1
        if (parts.length < 4) {
            throw new ApiException(400, "FEN string is in complete");
        }
        String[] placements = parts[0].split("/"); //first part: r1b2r2/1p1n1ppk/4p3/b2pB3/p2P4/q1P1R3/P2Q1PPP/R5K1
        if (placements.length != 8) {
            throw new ApiException(400, "FEN string contains incorrect placement rows");
        }
        String turn = parts[1]; //w
        String castling = parts[2]; // -
        String enPassant = parts[3]; // -
        int halfMove;
        int fullMove;
        try {
            halfMove = parts.length > 4 ? Integer.parseInt(parts[4]) : 0; // 0 - a halfmove is the number of total moves since last capture or pawn advance
            fullMove = parts.length > 5 ? Integer.parseInt(parts[5]) : 1; // 1 - a fullmove increments by 1 each time black completes a turn. Reset to one for daily puzzles
        } catch (NumberFormatException e) {
            throw new ApiException(400, "Error in half and full move");
        }
        if (halfMove < 0 || fullMove < 1) {
            throw new ApiException(400, "Error in half and full move");
        }


        Map<Position, Piece> allPieces = new HashMap<>();
        for (int i = 0; i < 8; i++) {
            int y = 7 - i; //my position enum has A1 = (0,0) so rank 8 first one is fen is y=7
            int x = 0;
            for (char c : placements[i].toCharArray()) {
                if (Character.isDigit(c)) {
                    x += Character.getNumericValue(c);
                } else {
                    boolean isWhite = Character.isUpperCase(c);
                    Piece piece = createPiece(Character.toLowerCase(c), isWhite, y);
                    if (x > 7) { //x has to be between 0-7 to represent the 8 ranks
                        throw new ApiException(400, "X position out of bounds");
                    }
                    allPieces.put(PositionConverter.fromCoordinates(x, y), piece);
                    x++;
                }
            }
            if (x != 8) { // x has to be 8 after looping through the 8 ranks (0-7)
                throw new ApiException(400, "Rank " + (8 - i) + " does not cover 8 squares");
            }
        }

        boolean isWhitesTurn;
        if (turn.equals("w")) {
            isWhitesTurn = true;
        } else if (turn.equals("b")) {
            isWhitesTurn = false;
        } else {
            throw new ApiException(400, "Cannot read turn from fen: " + turn);
        }

        Board board = new Board();
        board.getAllPieces().putAll(allPieces);

        handleCastlingRights(castling, board);
        handleEnPassantRights(enPassant, board);
        return new FenGameState(board, isWhitesTurn, halfMove, fullMove);
    }

    private static Piece createPiece(char c, boolean isWhite, int y) {
        return switch (c) {
            case 'p' -> new Pawn(isWhite, "pawn", pawnHasMoved(isWhite, y), false);
            case 'r' -> new Rook(isWhite, "rook", true);
            case 'b' -> new Bishop(isWhite, "bishop");
            case 'n' -> new Knight(isWhite, "knight");
            case 'q' -> new Queen(isWhite, "queen");
            case 'k' -> new King(isWhite, "king", true);
            default -> throw new ApiException(400, "No matching piece to build from: " + c);
        };
    }

    private static boolean pawnHasMoved(boolean isWhite, int y) {
        if (isWhite && y == 1) {
            return false;
        } else if (!isWhite && y == 6) {
            return false;
        } else {
            return true;
        }
    }

    private static void handleCastlingRights(String castling, Board board) {
        if (castling.equals("-")) {
            return;
        }
        for (char c : castling.toCharArray()) {
            switch (c) {
                case 'K' -> applyCastling(board, Position.E1, Position.H1);
                case 'Q' -> applyCastling(board, Position.E1, Position.A1);
                case 'k' -> applyCastling(board, Position.E8, Position.H8);
                case 'q' -> applyCastling(board, Position.E8, Position.A8);
                default -> throw new ApiException(400, "Invalid castling field");
            }
        }
    }

    private static void applyCastling(Board board, Position kingSquare, Position rookSquare) {
        Piece k = board.getAllPieces().get(kingSquare);
        Piece r = board.getAllPieces().get(rookSquare);
        if (!(k instanceof King king) || !(r instanceof Rook rook)) {
            throw new ApiException(400, "FEN says castling rights, but no correct pieces found");
        }
        king.setHasMoved(false);
        rook.setHasMoved(false);
    }

    private static void handleEnPassantRights(String enPassant, Board board) {
        if (enPassant.equals("-")){
            return;
        }
        Position target = PositionConverter.fromString(enPassant);

        int pawnY;
        boolean expectWhitePawn;
        if (target.getY() == 2) {          //target on rank 3 -> white pawn is on rank 4
            pawnY = 3;
            expectWhitePawn = true;
        } else if (target.getY() == 5) {   //target on rank 6 -> black pawn is on rank 5
            pawnY = 4;
            expectWhitePawn = false;
        } else {
            throw new ApiException(400, "Invalid en passant square: " + enPassant);
        }

        Position pawnSquare = PositionConverter.fromCoordinates(target.getX(), pawnY);
        if (!(board.getAllPieces().get(pawnSquare) instanceof Pawn pawn)){
            throw new ApiException(400, "No pawn on " + pawnSquare + " for en passant target " + enPassant);
        }
        if (pawn.isWhite() != expectWhitePawn){
            throw new ApiException(400, "Pawn is incorrect color");
        }
        pawn.setEnPassantTakeable(true);
        board.getTurnHelper().setLastMovedPiece(pawn);
    }
}