package app.testutils;

import app.gameengine.Position;
import app.gameengine.pieces.*;

public class PiecesTestFactory {

    public static Pawn whitePawn(boolean hasMoved, boolean isEnPassantTakeable){
        return new Pawn(true, "pawn", hasMoved, isEnPassantTakeable);
    }

    public static Pawn blackPawn(boolean hasMoved, boolean isEnPassantTakeable){
        return new Pawn(false, "pawn", hasMoved, isEnPassantTakeable);
    }

    public static Bishop whiteBishop(){
        return new Bishop(true, "bishop");
    }

    public static Bishop blackBishop(){
        return new Bishop(false, "bishop");
    }

    public static King whiteKing(boolean hasMoved){
        return new King(true, "king", hasMoved);
    }

    public static King blackKing(boolean hasMoved){
        return new King(false, "king", hasMoved);
    }

    public static Knight whiteKnight(){
        return new Knight(true, "knight");
    }

    public static Knight blackKnight(){
        return new Knight(false, "knight");
    }

    public static Queen whiteQueen(){
        return new Queen(true, "queen");
    }

    public static Queen blackQueen(){
        return new Queen(false, "queen");
    }

    public static Rook whiteRook(boolean hasMoved){
        return new Rook(true, "rook", hasMoved);
    }

    public static Rook blackRook(boolean hasMoved){
        return new Rook(false, "rook", hasMoved);
    }
}
