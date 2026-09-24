package app.gameengine.move_logic;

import app.gameengine.exceptions.InvalidPromotionSquareException;
import app.gameengine.exceptions.InvalidPromotionTypeException;
import app.gameengine.Position;
import app.gameengine.Promotion;
import app.gameengine.pieces.*;

public class PromotionHelper {

    public static boolean isPromotionMove(Piece piece, Position position) {

        if (piece.isWhite() && piece instanceof Pawn && position.getY() == 7) {
            return true;
        } else if (!piece.isWhite() && piece instanceof Pawn && position.getY() == 0) {
            return true;
        }
        return false;
    }


    public static Piece getPromotionPiece(Promotion promotion) {

        String pieceType = promotion.pieceType();
        Position position = promotion.promotionSquare();
        Piece piece = null;

        if (position.getY() == 7) {
            switch (pieceType) {
                case "queen":
                    piece = new Queen(true, "queen");
                    break;
                case "knight":
                    piece = new Knight(true, "knight");
                    break;
                case "bishop":
                    piece = new Bishop(true, "bishop");
                    break;
                case "rook":
                    piece = new Rook(true, "rook", true);
                    break;
                default:
                    throw new InvalidPromotionTypeException(promotion.pieceType());
            }
        } else if (position.getY() == 0) {
            switch (pieceType) {
                case "queen":
                    piece = new Queen(false, "queen");
                    break;
                case "knight":
                    piece = new Knight(false, "knight");
                    break;
                case "bishop":
                    piece = new Bishop(false, "bishop");
                    break;
                case "rook":
                    piece = new Rook(false, "rook", true);
                    break;
                default:
                    throw new InvalidPromotionTypeException(promotion.pieceType());
            }
        }
        if (position.getY() > 0 && position.getY() < 7) {
            throw new InvalidPromotionSquareException(position);
        }
        return piece;
    }

    public static String getPromotionPieceLetter(String pieceType) {
        String letter;

        switch (pieceType) {
            case "queen":
                letter = "q";
                break;
            case "knight":
                letter = "n";
                break;
            case "bishop":
                letter = "b";
                break;
            case "rook":
                letter = "r";
                break;
            default:
                throw new InvalidPromotionTypeException(pieceType);
        }
        return letter;
    }
}
