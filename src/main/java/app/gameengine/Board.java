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
    private Map<Piece, Position> allPieces = new HashMap<>();

    public void initializeNewBoard() throws IOException {
        char letter = 'A';

        //white pawns
        for (int i = 1; i<9; i++ ){
            int number = 2;
            String position = String.valueOf(letter)+number;
            Pawn pawn = new Pawn(PieceSVG.PAWN.loadSVG("white"), true, "pawn", false, false);
            letter++;
            allPieces.put(pawn, Position.valueOf(position));
        }

        //white rooks
        Rook wRook = new Rook(PieceSVG.ROOK.loadSVG("white"),true, "rook", false);
        allPieces.put(wRook, Position.A1);
        Rook wRook2 = new Rook(PieceSVG.ROOK.loadSVG("white"), true, "rook", false);
        allPieces.put(wRook2, Position.H1);

        //white knights
        Knight wKnight = new Knight(PieceSVG.KNIGHT.loadSVG("white"), true, "knight");
        allPieces.put(wKnight, Position.B1);
        Knight wKnight2 = new Knight(PieceSVG.KNIGHT.loadSVG("white"), true, "knight");
        allPieces.put(wKnight2, Position.G1);

        //white bishops
        Bishop wBishop = new Bishop(PieceSVG.BISHOP.loadSVG("white"), true, "bishop");
        allPieces.put(wBishop, Position.C1);
        Bishop wBishop2 = new Bishop(PieceSVG.BISHOP.loadSVG("white"), true, "bishop");
        allPieces.put(wBishop2, Position.F1);

        //white queen
        Queen wQueen = new Queen(PieceSVG.QUEEN.loadSVG("white"), true, "queen");
        allPieces.put(wQueen, Position.D1);

        //white king
        King wKing = new King(PieceSVG.KING.loadSVG("white"), true, "king", false);
        allPieces.put(wKing, Position.E1);

        //black pawns
        letter = 'A';
        for (int i = 1; i<9; i++ ){
            int number = 7;
            String position = String.valueOf(letter)+number;
            Pawn pawn = new Pawn(PieceSVG.PAWN.loadSVG("black"), false, "pawn", false, false);
            letter++;
            allPieces.put(pawn, Position.valueOf(position));
        }

        //black rooks
        Rook bRook = new Rook(PieceSVG.ROOK.loadSVG("black"),false, "rook", false);
        allPieces.put(bRook, Position.A8);
        Rook bRook2 = new Rook(PieceSVG.ROOK.loadSVG("black"), false, "rook", false);
        allPieces.put(bRook2, Position.H8);

        //black knights
        Knight bKnight = new Knight(PieceSVG.KNIGHT.loadSVG("black"), false, "knight");
        allPieces.put(bKnight, Position.B8);
        Knight bKnight2 = new Knight(PieceSVG.KNIGHT.loadSVG("black"), false, "knight");
        allPieces.put(bKnight2, Position.G8);

        //white bishops
        Bishop bBishop = new Bishop(PieceSVG.BISHOP.loadSVG("black"), false, "bishop");
        allPieces.put(bBishop, Position.C8);
        Bishop bBishop2 = new Bishop(PieceSVG.BISHOP.loadSVG("black"), false, "bishop");
        allPieces.put(bBishop2, Position.F8);

        //black queen
        Queen bQueen = new Queen(PieceSVG.QUEEN.loadSVG("black"), false, "queen");
        allPieces.put(bQueen, Position.D8);

        //black king
        King bKing = new King(PieceSVG.KING.loadSVG("black"), false, "king", false);
        allPieces.put(bKing, Position.E8);
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
        if (allPieces.containsKey(piece)){
            //remove enemy piece if there is one
            if (allPieces.containsValue(newPosition)){
                allPieces.remove(PieceFinder.findPiece(this, newPosition));
            }
            endTurn(piece, newPosition);
            allPieces.put(piece, newPosition);
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
            if (allPieces.containsKey(king) && allPieces.containsKey(rook)) {
                allPieces.put(king, newKingPosition);
                allPieces.put(rook, newRookPosition);
                endTurn(king, kingPosition);
                return true;
            }

            //if castling left
        }else if (kingPosition.getX() > rookPosition.getX()){
            Piece rook = PieceFinder.findPiece(this, PositionConverter.fromCoordinates(kingPosition.getX()-4, kingPosition.getY()));
            Position newKingPosition = PositionConverter.fromCoordinates(kingPosition.getX()-2, kingPosition.getY());
            Position newRookPosition = PositionConverter.fromCoordinates(kingPosition.getX()-1, kingPosition.getY());
            if (allPieces.containsKey(king) && allPieces.containsKey(rook)) {
                allPieces.put(king, newKingPosition);
                allPieces.put(rook, newRookPosition);
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