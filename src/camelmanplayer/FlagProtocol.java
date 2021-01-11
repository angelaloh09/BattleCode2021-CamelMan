package camelmanplayer;

import battlecode.common.Team;
import org.omg.IOP.TAG_ALTERNATE_IIOP_ADDRESS;

/**
 * Placeholder for communication.
 * **/
public class FlagProtocol {

    static final String tag = "10101010";

    static Message decode(int color) {
        String biString = zeroExtend(color, 24);
        String tag = biString.substring(0, 8);

        if (tag.equals(FlagProtocol.tag)) {
            String warPhaseStr = biString.substring(8, 10);
            String teamStr = biString.substring(10, 12);
            String xCoorStr = biString.substring(12, 18);
            String yCoorStr = biString.substring(18, 24);

            RobotPlayer.WarPhase warPhase = biStringToWarPhase(warPhaseStr);
            Team team = biStringToTeam(teamStr);
            int xCoor = Integer.parseInt(xCoorStr, 2);
            int yCoor = Integer.parseInt(yCoorStr, 2);

            return new Message(warPhase, team, xCoor, yCoor);
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

    static int encode(Message msg) {
        if (msg != null) {
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

    static boolean biStringToBool(String bi) {
        return bi.equals("0") ? false : true;
    }

    static String boolToBiString(boolean bool) {
        return bool ? "1" : "0";
    }
}
