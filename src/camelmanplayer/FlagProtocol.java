package camelmanplayer;

import battlecode.common.Team;
import org.omg.IOP.TAG_ALTERNATE_IIOP_ADDRESS;

/**
 * Placeholder for communication.
 * **/
public class FlagProtocol {

    static final String tag = "101010";

    static Message decode(int color) {
        String biString = Integer.toBinaryString(color);
        String tag = biString.substring(0, 6);

        if (tag.equals(FlagProtocol.tag)) {
            String warPhaseStr = biString.substring(6, 8);
            String teamStr = biString.substring(8, 10);
            String xCoorStr = biString.substring(10, 17);
            String yCoorStr = biString.substring(17, 24);

            RobotPlayer.WarPhase warPhase = biStringToWarPhase(warPhaseStr);
            Team team = biStringToTeam(teamStr);
            int xCoor = decodeSignedNum(xCoorStr);
            int yCoor = decodeSignedNum(yCoorStr);

            return new Message(warPhase, team, xCoor, yCoor);
        }
        return null;
    }

    static int decodeSignedNum(String signedBiString) {
        int absVal = Integer.parseInt(signedBiString.substring(1), 2);
        if (signedBiString.charAt(0) == '0'){
            return absVal;
        } else {
            return -absVal;
        }
    }

    static String signExtend(int num, int length) {
        int absVal = Math.abs(num);
        String rawBiString = Integer.toBinaryString(absVal);
        int numOfZeros = length-rawBiString.length()-1;
        String zeros = "";
        if (numOfZeros > 0) zeros = new String(new char[numOfZeros]).replace("\0", "0");
        if (num < 0) {
            return "1"+zeros+rawBiString;
        } else {
            return "0"+zeros+rawBiString;
        }
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
            String xCoorStr = signExtend(msg.relativeX, 7);
            String yCoorStr = signExtend(msg.relativeY, 7);
            return Integer.parseInt(tag + warPhaseStr + teamStr + xCoorStr + yCoorStr, 2);
        } else {
            return 8388608;
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
