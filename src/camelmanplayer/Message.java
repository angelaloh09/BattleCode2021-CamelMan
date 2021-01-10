package camelmanplayer;

class Message {
    boolean isNeutral;
    boolean isXCoordinate;
    int coordinate;

    Message(boolean isNeu, boolean isX, int coor) {
        isNeutral = isNeu;
        isXCoordinate = isX;
        coordinate = coor;
    }
}
