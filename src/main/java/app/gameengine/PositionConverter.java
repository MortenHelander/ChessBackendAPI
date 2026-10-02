package app.gameengine;

import app.exceptions.ApiException;

public class PositionConverter {
    public static Position fromCoordinates(int x, int y){
        Position[] positions = Position.values();
        for (Position position : positions) {
            if (position.getX() == x && position.getY() == y){
                return position;
            }

        }
        return null;
    }

    public static Position fromString(String raw) {
        if (raw == null || raw.isBlank())
            throw new ApiException(400, "Position is required");
        try {
            return Position.valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ApiException(400, "Invalid position: " + raw);
        }
    }
}
