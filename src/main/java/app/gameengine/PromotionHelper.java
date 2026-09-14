package app.gameengine;

import app.gameengine.pieces.*;

import java.io.IOException;

public class PromotionHelper {

    public static boolean isPromotionMove(Piece piece, Position position){

        if (piece.isWhite() && piece instanceof Pawn && position.getY() == 7){
            return true;
        } else if (!piece.isWhite() && piece instanceof Pawn && position.getY() == 0){
            return true;
        }
        return false;
    }


    public static Piece getPromotionPiece(Promotion promotion) {

        String pieceType = promotion.pieceType();
        Position position = promotion.promotionSquare();
        Piece piece = null;

        if (position.getY() == 7){
            switch (pieceType) {
                case "queen":
                    piece = new Queen(true, "queen");
                    break;
                case "knight":
                    piece = new Knight(true, "knight");
                    break;
                case "bishop":
                    piece = new Bishop( true, "bishop");
                    break;
                case "rook":
                    piece = new Rook(true, "rook", true);
                    break;
            }
        }else if (position.getY() == 0){
            switch (pieceType) {
                case "queen":
                    piece= new Queen(false, "queen");
                    break;
                case "knight":
                    piece = new Knight( false, "knight");
                    break;
                case "bishop":
                    piece = new Bishop( false, "bishop");
                    break;
                case "rook":
                    piece = new Rook( false, "rook", true);
                    break;
            }
        }
        return piece;
    }

    public static String getPromotionPieceLetter(String pieceType){
        String letter = "";

        switch (pieceType){
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
        }
        return letter;
    }
}
