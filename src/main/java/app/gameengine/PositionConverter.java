package app.gameengine;

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
}
