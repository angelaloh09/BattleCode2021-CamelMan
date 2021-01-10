package camelmanplayer;

import java.util.*;
import battlecode.common.*;

public class EnlightenmentCenter extends RobotPlayer{

    private static WarPhase warPhase;
    private static List<Integer> childRobotIds;
    private static int numOfRobotBuilt;
    private static int numOfRobotDied;

    EnlightenmentCenter(RobotController rc) {
        this.rc = rc;
        warPhase = WarPhase.SEARCH;
        childRobotIds = new LinkedList<>();
        numOfRobotBuilt = 0;
        numOfRobotDied = 0;
    }

    static void runElightenmentCenter() throws GameActionException {
        updateWarPhase();
        buildRobot();
        setFlag();
    }

    static void updateWarPhase() {

    }

    static void setFlag() {

    }

    // TODO: Determine the ratio of robot types for each war phase.
    static RobotType determineRobotType() {
        switch (warPhase) {
            case SEARCH:
                return randomRobotType(0.4,0.2,0.4);
            case CONQUER:
                return randomRobotType(0.8,0.1,0.1);
            case ATTACK:
                return randomRobotType(0.5,0.1,0.4);
            case DEFEND:
                return randomRobotType(0.5,0.5,0);
            default:
                return randomRobotType(0.33,0.33,0.33);
        }
    }

    static RobotType randomRobotType(double politicianPercent, double slandererPercent, double muckrakerPercent) {
        double randomNum = Math.random();

        if (randomNum < politicianPercent) {
            return RobotType.POLITICIAN;
        } else if (randomNum - politicianPercent < slandererPercent) {
            return RobotType.SLANDERER;
        } else {
            return RobotType.MUCKRAKER;
        }
    }

    static void buildRobot() throws GameActionException {
        // TODO: change the hardcoded influence.
        int influence = 50;
        RobotType toBuild = determineRobotType();

        for (Direction dir : directions) {
            if (rc.canBuildRobot(toBuild, dir, influence)) {
                rc.buildRobot(toBuild, dir, influence);
                updateChildRobotIds(dir);
                numOfRobotBuilt++;
            } else {
                break;
            }
        }
    }

    static void updateChildRobotIds(Direction dir) throws GameActionException {
        MapLocation robotLocation = rc.adjacentLocation(dir);
        RobotInfo robotInfo = rc.senseRobotAtLocation(robotLocation);
        childRobotIds.add(robotInfo.ID);
    }

    static void listenToChildRobots() throws GameActionException {
        List<Integer> removeList = new ArrayList<>();
        for (int robotID: childRobotIds) {
            if (rc.canGetFlag(robotID)) {
                int robotFlag = rc.getFlag(robotID);
                // TODO: decodeFlagMessage();
            } else {
                removeList.add(robotID);
            }
        }

        for (int removeID: removeList) {
            childRobotIds.remove(removeID);
        }

        numOfRobotDied += removeList.size();
    }

    // TODO: Move to RobotPlayer later
    enum WarPhase {
        SEARCH,
        CONQUER,
        ATTACK,
        DEFEND
    }
}