package app.gameengine;

import app.gameengine.move_logic.PromotionHelper;
import app.gameengine.pieces.*;
import lombok.Getter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Getter

public class Board {
    private TurnHelper turnHelper;
    private MoveExecutor moveExecutor;
    private Map<Position, Piece> allPieces = new HashMap<>();

    public void initializeNewBoard() throws IOException {
        turnHelper = new TurnHelper();
        moveExecutor = new MoveExecutor();
        char letter = 'A';

        //white pawns
        for (int i = 1; i<9; i++ ){
            int number = 2;
            //using char to go from A to H
            String position = String.valueOf(letter)+number;
            Pawn pawn = new Pawn(true, "pawn", false, false);
            letter++;
            allPieces.put(Position.valueOf(position), pawn);
        }

        //white rooks
        Rook wRook = new Rook(true, "rook", false);
        allPieces.put(Position.A1, wRook);
        Rook wRook2 = new Rook(true, "rook", false);
        allPieces.put(Position.H1, wRook2);

        //white knights
        Knight wKnight = new Knight( true, "knight");
        allPieces.put(Position.B1, wKnight);
        Knight wKnight2 = new Knight( true, "knight");
        allPieces.put(Position.G1, wKnight2);

        //white bishops
        Bishop wBishop = new Bishop( true, "bishop");
        allPieces.put(Position.C1, wBishop);
        Bishop wBishop2 = new Bishop( true, "bishop");
        allPieces.put(Position.F1, wBishop2);

        //white queen
        Queen wQueen = new Queen( true, "queen");
        allPieces.put(Position.D1, wQueen);

        //white king
        King wKing = new King(true, "king", false);
        allPieces.put(Position.E1, wKing);

        //black pawns
        letter = 'A';
        for (int i = 1; i<9; i++ ){
            int number = 7;
            String position = String.valueOf(letter)+number;
            Pawn pawn = new Pawn(false, "pawn", false, false);
            letter++;
            allPieces.put(Position.valueOf(position), pawn);
        }

        //black rooks
        Rook bRook = new Rook(false, "rook", false);
        allPieces.put(Position.A8, bRook);
        Rook bRook2 = new Rook( false, "rook", false);
        allPieces.put(Position.H8, bRook2);

        //black knights
        Knight bKnight = new Knight( false, "knight");
        allPieces.put(Position.B8, bKnight);
        Knight bKnight2 = new Knight( false, "knight");
        allPieces.put(Position.G8, bKnight2);

        //white bishops
        Bishop bBishop = new Bishop( false, "bishop");
        allPieces.put(Position.C8, bBishop);
        Bishop bBishop2 = new Bishop( false, "bishop");
        allPieces.put(Position.F8, bBishop2);

        //black queen
        Queen bQueen = new Queen( false, "queen");
        allPieces.put(Position.D8, bQueen);

        //black king
        King bKing = new King( false, "king", false);
        allPieces.put(Position.E8, bKing);
    }


    public MoveResult move(Piece piece, Position oldPosition, Position newPosition){

        MoveType moveType = MoveIdentifier.identifyMove(this, piece, oldPosition, newPosition);
        if (moveType == null){
            return MoveResult.failed();
        }
        if (moveType == MoveType.PROMOTION){
            moveExecutor.executeMove(this, piece, MoveType.NORMAL, oldPosition, newPosition);
            return MoveResult.awaitingPromotion(newPosition);
        }
        boolean moved = moveExecutor.executeMove(this, piece, moveType, oldPosition, newPosition);
        if (moved){
            turnHelper.endTurn(piece, newPosition);
        }
        return MoveResult.completed();
    }

    public MoveResult promotion(Promotion promotion){

        MoveResult moveResult;
        Piece promotionPiece = PromotionHelper.getPromotionPiece(promotion);
        moveExecutor.promotion(this, promotionPiece, promotion.promotionSquare());
        moveResult = MoveResult.completed();
        turnHelper.endTurn(promotionPiece, promotion.promotionSquare());
        return moveResult;
    }

    public Map<Position, Piece> getAllWhitePieces(){
        Map<Position, Piece> whitePieces = new HashMap<>();
        for (Map.Entry<Position, Piece> piecePositionEntry : allPieces.entrySet()) {
            if (piecePositionEntry.getValue().isWhite()){
                whitePieces.put(piecePositionEntry.getKey(), piecePositionEntry.getValue());
            }
        }
        return whitePieces;
    }

    public Map<Position, Piece> getAllBlackPieces(){
        Map<Position, Piece> blackPieces = new HashMap<>();
        for (Map.Entry<Position, Piece> piecePositionEntry : allPieces.entrySet()) {
            if (!piecePositionEntry.getValue().isWhite()){
                blackPieces.put(piecePositionEntry.getKey(), piecePositionEntry.getValue());
            }
        }
        return blackPieces;
    }
}