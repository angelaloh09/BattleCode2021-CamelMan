package camelmanplayer;

/**
 * Placeholder for communication.
 * **/
public class FlagProtocol {

    static final String tag = "1010101";

    static Message decode(int color) {
        String rawBiString = Integer.toBinaryString(color);
        String zeros = new String(new char[24-rawBiString.length()]).replace("\0", "0");
        String biString = zeros+rawBiString;
        String tag = biString.substring(0, 7);

        if (tag.equals(FlagProtocol.tag)) {
            String isNeuStr = biString.substring(7,8);
            String isXCoorStr = biString.substring(8,9);
            String coorStr = biString.substring(10);
            boolean isNeutral = biStringToBool(isNeuStr);
            boolean isXCoordinate = biStringToBool(isXCoorStr);
            int coordinate = Integer.parseInt(coorStr, 2);

            return new Message(isNeutral, isXCoordinate, coordinate);
        }
        return null;
    }

    static int encode(Message msg) {
        String isNeuStr = boolToBiString(msg.isNeutral);
        String isXCoorStr = boolToBiString(msg.isXCoordinate);
        String coorStr = Integer.toBinaryString(msg.coordinate);
        return Integer.parseInt(tag+isNeuStr+isXCoorStr+coorStr, 2);
    }

    static boolean biStringToBool(String bi) {
        return bi.equals("0") ? false : true;
    }

    static String boolToBiString(boolean bool) {
        return bool ? "1" : "0";
    }
}
