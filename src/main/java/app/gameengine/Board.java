package app.gameengine;

import app.gameengine.pieces.*;
import lombok.Getter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Getter

public class Board {
    private Piece lastMovedPieceWhite;
    private Piece lastMovedPieceBlack;
    private Map<Position, Piece> allPieces = new HashMap<>();

    public void initializeNewBoard() throws IOException {
        char letter = 'A';

        //white pawns
        for (int i = 1; i<9; i++ ){
            int number = 2;
            String position = String.valueOf(letter)+number;
            Pawn pawn = new Pawn(PieceSVG.PAWN.loadSVG("white"), true, "pawn", false, false);
            letter++;
            allPieces.put(Position.valueOf(position), pawn);
        }

        //white rooks
        Rook wRook = new Rook(PieceSVG.ROOK.loadSVG("white"),true, "rook", false);
        allPieces.put(Position.A1, wRook);
        Rook wRook2 = new Rook(PieceSVG.ROOK.loadSVG("white"), true, "rook", false);
        allPieces.put(Position.H1, wRook2);

        //white knights
        Knight wKnight = new Knight(PieceSVG.KNIGHT.loadSVG("white"), true, "knight");
        allPieces.put(Position.B1, wKnight);
        Knight wKnight2 = new Knight(PieceSVG.KNIGHT.loadSVG("white"), true, "knight");
        allPieces.put(Position.G1, wKnight2);

        //white bishops
        Bishop wBishop = new Bishop(PieceSVG.BISHOP.loadSVG("white"), true, "bishop");
        allPieces.put(Position.C1, wBishop);
        Bishop wBishop2 = new Bishop(PieceSVG.BISHOP.loadSVG("white"), true, "bishop");
        allPieces.put(Position.F1, wBishop2);

        //white queen
        Queen wQueen = new Queen(PieceSVG.QUEEN.loadSVG("white"), true, "queen");
        allPieces.put(Position.D1, wQueen);

        //white king
        King wKing = new King(PieceSVG.KING.loadSVG("white"), true, "king", false);
        allPieces.put(Position.E1, wKing);

        //black pawns
        letter = 'A';
        for (int i = 1; i<9; i++ ){
            int number = 7;
            String position = String.valueOf(letter)+number;
            Pawn pawn = new Pawn(PieceSVG.PAWN.loadSVG("black"), false, "pawn", false, false);
            letter++;
            allPieces.put(Position.valueOf(position), pawn);
        }

        //black rooks
        Rook bRook = new Rook(PieceSVG.ROOK.loadSVG("black"),false, "rook", false);
        allPieces.put(Position.A8, bRook);
        Rook bRook2 = new Rook(PieceSVG.ROOK.loadSVG("black"), false, "rook", false);
        allPieces.put(Position.H8, bRook2);

        //black knights
        Knight bKnight = new Knight(PieceSVG.KNIGHT.loadSVG("black"), false, "knight");
        allPieces.put(Position.B8, bKnight);
        Knight bKnight2 = new Knight(PieceSVG.KNIGHT.loadSVG("black"), false, "knight");
        allPieces.put(Position.G8, bKnight2);

        //white bishops
        Bishop bBishop = new Bishop(PieceSVG.BISHOP.loadSVG("black"), false, "bishop");
        allPieces.put(Position.C8, bBishop);
        Bishop bBishop2 = new Bishop(PieceSVG.BISHOP.loadSVG("black"), false, "bishop");
        allPieces.put(Position.F8, bBishop2);

        //black queen
        Queen bQueen = new Queen(PieceSVG.QUEEN.loadSVG("black"), false, "queen");
        allPieces.put(Position.D8, bQueen);

        //black king
        King bKing = new King(PieceSVG.KING.loadSVG("black"), false, "king", false);
        allPieces.put(Position.E8, bKing);
    }


    public boolean move(Piece piece, Position oldPosition, Position newPosition){

        //check if move is castling
        if (CastlingHelper.isCastlingMove(this, piece, oldPosition, newPosition)){
            endTurn(piece, newPosition);
            return castlingMove(piece, oldPosition, newPosition);
        }
        //check if move is en passant
        if (PawnMoveHelper.isEnPassantMove(this, piece, oldPosition, newPosition)){
            endTurn(piece, newPosition);
            return enPassantMove(piece, oldPosition, newPosition);
        }
        //check if correctly called
        if (allPieces.containsValue(piece)){
            endTurn(piece, newPosition);
            allPieces.put(newPosition, piece);
            return true;
        } else {
            return false;
        }
    }

    private boolean castlingMove(Piece king, Position kingPosition, Position rookPosition){

        //if castling right
        if (kingPosition.getX() < rookPosition.getX()){
            Piece rook = PieceFinder.findPiece(this, PositionConverter.fromCoordinates(kingPosition.getX()+3, kingPosition.getY()));
            Position newKingPosition = PositionConverter.fromCoordinates(kingPosition.getX()+2, kingPosition.getY());
            Position newRookPosition = PositionConverter.fromCoordinates(kingPosition.getX()+1, kingPosition.getY());
            if (allPieces.containsValue(king) && allPieces.containsValue(rook)) {
                allPieces.put(newKingPosition, king);
                allPieces.put(newRookPosition, rook);
                endTurn(king, kingPosition);
                return true;
            }

            //if castling left
        }else if (kingPosition.getX() > rookPosition.getX()){
            Piece rook = PieceFinder.findPiece(this, PositionConverter.fromCoordinates(kingPosition.getX()-4, kingPosition.getY()));
            Position newKingPosition = PositionConverter.fromCoordinates(kingPosition.getX()-2, kingPosition.getY());
            Position newRookPosition = PositionConverter.fromCoordinates(kingPosition.getX()-1, kingPosition.getY());
            if (allPieces.containsValue(king) && allPieces.containsValue(rook)) {
                allPieces.put(newKingPosition, king);
                allPieces.put(newRookPosition, rook);
                endTurn(king, kingPosition);
                return true;
            }
        }
        return false;
    }

    private boolean enPassantMove(Piece piece, Position oldPosition, Position newPosition){
        if (piece instanceof Pawn pawn){
            //if en passant right
            if (oldPosition.getX() < newPosition.getX()){
                Position enemyPiecePosition = PositionConverter.fromCoordinates(oldPosition.getX()+1, oldPosition.getY());
                Piece enemyPiece = PieceFinder.findPiece(this, enemyPiecePosition);
                if (enemyPiece instanceof Pawn enemyPawn && enemyPawn.isEnPassantTakeable()){
                    allPieces.put(piece, newPosition);
                    allPieces.remove(enemyPiece, enemyPiecePosition);
                    return true;
                }
                //if en passant left
            } else if (oldPosition.getX() > newPosition.getX()){
                Position enemyPiecePosition = PositionConverter.fromCoordinates(oldPosition.getX()-1, oldPosition.getY());
                Piece enemyPiece = PieceFinder.findPiece(this, enemyPiecePosition);
                if (enemyPiece instanceof Pawn enemyPawn && enemyPawn.isEnPassantTakeable()){
                    allPieces.put(piece, newPosition);
                    allPieces.remove(enemyPiece, enemyPiecePosition);
                    return true;
                }
            }
        }
        return false;
    }

    public void promotion(Piece piece, Position position){

        if (piece != null){
            Piece enemyPieceCandidate = PieceFinder.findPiece(this, position);
            if (enemyPieceCandidate != null){
                allPieces.remove(enemyPieceCandidate, position);
            }
            allPieces.put(piece, position);
            endTurn(piece, position);
        }
    }

    private void endTurn(Piece piece, Position newPosition){
        CastlingHelper.setCastlingPieceMoveStatus(piece);
        handleLastPieceStatus(piece);
        PawnMoveHelper.setPawnMoveStatusMovedThisRound(piece, newPosition);

    }

    private void handleLastPieceStatus(Piece piece){

        if (piece instanceof Pawn pawn && pawn.isEnPassantTakeable()){
            PawnMoveHelper.setPawnMoveStatusMovedLastRound(pawn);
        }
        if (piece.isWhite()){
            lastMovedPieceWhite = piece;
        } else {
            lastMovedPieceBlack = piece;
        }
    }

    public Map<Piece, Position> getAllWhitePieces(){
        Map<Piece, Position> whitePieces = new HashMap<>();
        for (Map.Entry<Piece, Position> piecePositionEntry : allPieces.entrySet()) {
            if (piecePositionEntry.getKey().isWhite()){
                whitePieces.put(piecePositionEntry.getKey(), piecePositionEntry.getValue());
            }
        }
        return whitePieces;
    }

    public Map<Piece, Position> getAllBlackPieces(){
        Map<Piece, Position> blackPieces = new HashMap<>();
        for (Map.Entry<Piece, Position> piecePositionEntry : allPieces.entrySet()) {
            if (!piecePositionEntry.getKey().isWhite()){
                blackPieces.put(piecePositionEntry.getKey(), piecePositionEntry.getValue());
            }
        }
        return blackPieces;
    }
}