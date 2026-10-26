package app.gameengine.gameservices;

import app.entities.Move;
import app.exceptions.ApiException;
import app.gameengine.Position;
import app.gameengine.game_logic.Promotion;

import java.util.ArrayList;
import java.util.List;

public class UciParser {

    public static Move toMove(String uci, int moveNumber){
        if (uci == null || uci.length() < 4 || uci.length() > 5 ){
            throw new ApiException(400, "Invalid UCI move " + uci);
        }
        Position from = PositionConverter.fromString(uci.substring(0,2));
        Position to = PositionConverter.fromString(uci.substring(2,4));
        String promotion = uci.length() == 5 ? uci.substring(4):null;
        Move move = new Move(from, to, promotion, false); // only used for replay; givesCheck is never read there
        move.setUci(uci);
        move.setMoveNumber(moveNumber);
        return move;
    }

    public static List<Move> toMoves(List<String> solution){
        List<Move> moves = new ArrayList<>();
        for (int i = 0; i<solution.size(); i++){
            moves.add(toMove(solution.get(i), i+1));
        }
        return moves;
    }
}
