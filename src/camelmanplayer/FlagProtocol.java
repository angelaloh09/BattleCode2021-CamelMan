package camelmanplayer;

import battlecode.common.Team;
import org.omg.IOP.TAG_ALTERNATE_IIOP_ADDRESS;

/**
 * Placeholder for communication.
 * **/
public class FlagProtocol {

    //TODO: keep on updating location so mother knows where the kids are

    static final String tag = "10101010";
    static Message decode(int color) {
        String biString = zeroExtend(color, 24);
//        String tag = biString.substring(0, 8);
        String tag = biString.substring(0, 6); // UPDATED


        if (tag.equals(FlagProtocol.tag)) {
            String msgTypeStr = biString.substring(6, 8); // UPDATED
            String warPhaseStr = biString.substring(8, 10);
            String teamStr = biString.substring(10, 12);
            String xCoorStr = biString.substring(12, 18);
            String yCoorStr = biString.substring(18, 24);

            RobotPlayer.MessageType msgType = biStringToMessageType(msgTypeStr);
            RobotPlayer.WarPhase warPhase = biStringToWarPhase(warPhaseStr);
            Team team = biStringToTeam(teamStr);
            int xCoor = Integer.parseInt(xCoorStr, 2);
            int yCoor = Integer.parseInt(yCoorStr, 2);

//            return new Message(warPhase, team, xCoor, yCoor);

            return new Message(msgType, warPhase, team, xCoor, yCoor); //UPDATED
        }
        return null;
    }


    static String zeroExtend(int num, int length) {
        String rawBiString = Integer.toBinaryString(num);
        String zeros = new String(new char[length-rawBiString.length()]).replace("\0", "0");
        return zeros+rawBiString;
    }

    static Team biStringToTeam(String teamStr) {
        switch (teamStr) {
            case "00": return Team.A;
            case "01": return Team.B;
            default: return Team.NEUTRAL;
        }
    }

    static String teamToBiString(Team team) {
        switch (team) {
            case A: return "00";
            case B: return "01";
            default: return "10";
        }
    }

    static RobotPlayer.WarPhase biStringToWarPhase(String warPhaseStr) {
        switch (warPhaseStr) {
            case "00":
                return RobotPlayer.WarPhase.SEARCH;
            case "01":
                return RobotPlayer.WarPhase.CONQUER;
//            case "10":
//                return RobotPlayer.WarPhase.ATTACK; break;
            case "11":
                return RobotPlayer.WarPhase.DEFEND;
            default:
                return RobotPlayer.WarPhase.ATTACK;
        }
    }

    // UPDATED
    static RobotPlayer.MessageType biStringToMessageType(String msgTypeStr) {
        switch (msgTypeStr) {
            case "01":
                return RobotPlayer.MessageType.SCOUTDANGER;
            case "10":
                return RobotPlayer.MessageType.WALL;
            case "11":
                return RobotPlayer.MessageType.CORNER;
            default:
                return RobotPlayer.MessageType.ECENTER;
        }
    }

    static int encode(Message msg) {
        if (msg != null) {
            String msgTypeStr = msgTypeToBiString(msg.msgType); //UPDATED
            String warPhaseStr = warPhaseToBiString(msg.warPhase);
            String teamStr = teamToBiString(msg.team);
            String xCoorStr = zeroExtend(msg.relativeX, 6);
            String yCoorStr = zeroExtend(msg.relativeY, 6);
            return Integer.parseInt(tag + warPhaseStr + teamStr + xCoorStr + yCoorStr, 2);
        } else {
            return 0;
        }
    }

    static String warPhaseToBiString(RobotPlayer.WarPhase warPhase) {
        switch (warPhase){
            case SEARCH: return "00";
            case CONQUER: return "01";
//            case ATTACK: return "10"; break;
            case DEFEND: return "11";
            default: return "10";
        }
    }

    // UPDATED:
    static String msgTypeToBiString(RobotPlayer.MessageType msgType){
        switch (msgType){
            // case ECENTER: return "00";
            case SCOUTDANGER: return "01";
            case WALL: return "10";
            case CORNER: return "11";
            default: return "00";
        }
    }

    static boolean biStringToBool(String bi) {
        return bi.equals("0") ? false : true;
    }

    static String boolToBiString(boolean bool) {
        return bool ? "1" : "0";
    }
}
