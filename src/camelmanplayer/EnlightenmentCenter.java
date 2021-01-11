package camelmanplayer;

import java.util.*;
import battlecode.common.*;

public class EnlightenmentCenter extends RobotPlayer{

    private static WarPhase warPhase;
    private static List<Integer> childRobotIds;
    private static int numOfRobotBuilt;
    private static int numOfRobotDied;
    private static HashMap<MapLocation, Team> enlightenmentCenters;
    private static Team opponent;
    private static int directionIndex;

    EnlightenmentCenter(RobotController rc) {
        this.rc = rc;
        warPhase = WarPhase.SEARCH;
        childRobotIds = new LinkedList<>();
        numOfRobotBuilt = 0;
        numOfRobotDied = 0;
        enlightenmentCenters = new HashMap<>();
        enlightenmentCenters.put(rc.getLocation(), rc.getTeam());
        opponent = rc.getTeam().opponent();
        directionIndex = 0;
    }

    static void runEnlightenmentCenter() throws GameActionException {

        while (true) {
            warPhase = updateWarPhase();
            buildRobot();
            listenToChildRobots();
            setFlag();
            Clock.yield();
        }
    }

    static double survivalRate() {
        return 1 - (double) numOfRobotDied / (double) numOfRobotBuilt;
    }

    static WarPhase updateWarPhase() {
        for (MapLocation location: enlightenmentCenters.keySet()) {
            if (enlightenmentCenters.get(location) == Team.NEUTRAL) {
                return WarPhase.CONQUER;
            }
        }

        if (enlightenmentCenters.size() <= 2 && rc.getRoundNum() < 1000) {
            return WarPhase.SEARCH;
        }

        if (rc.getConviction() < 50 || survivalRate() < 0.1) {
            //TODO: make smarter thresholds
            return WarPhase.DEFEND;
        }

        if (enlightenmentCenters.size() == 1) return WarPhase.SEARCH;
        else return WarPhase.ATTACK;
    }

    static void setFlag() throws GameActionException {
        Message msgToSend = null;
        switch (warPhase) {
            case SEARCH: msgToSend = new Message(WarPhase.SEARCH); break;
            case CONQUER:
                MapLocation neutralLocation = getClosestEnlightmentCenter(Team.NEUTRAL);
                msgToSend = new Message(WarPhase.CONQUER, Team.NEUTRAL, neutralLocation, rc.getLocation());
                break;
            case ATTACK:
                MapLocation enemyLocation = getClosestEnlightmentCenter(opponent);
                msgToSend = new Message(WarPhase.ATTACK, opponent, enemyLocation, rc.getLocation());
                break;
            case DEFEND: msgToSend = new Message(WarPhase.DEFEND); break;
        }
        int newFlag = FlagProtocol.encode(msgToSend);
        if (newFlag != rc.getFlag(rc.getID())) {
            if (rc.canSetFlag(newFlag)) rc.setFlag(newFlag);
        }
    }

    private static MapLocation getClosestEnlightmentCenter(Team team) {
        MapLocation result = null;
        int minDistanceSquared = Integer.MAX_VALUE;
        for (MapLocation location: enlightenmentCenters.keySet()) {
            int distanceSquared = location.distanceSquaredTo(rc.getLocation());
            if (enlightenmentCenters.get(location) == team && distanceSquared < minDistanceSquared) {
                result = location;
                minDistanceSquared = distanceSquared;
            }
        }
        return result;
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

        for (int i = 0; i < directions.length; i++) {
            Direction dir = directions[(directionIndex+i) % directions.length];
            MapLocation targetLoc = rc.adjacentLocation(dir);
            if (!rc.onTheMap(targetLoc)) {
                directionIndex = (directionIndex + 1) % directions.length;
                break;
            }
            if (rc.canBuildRobot(toBuild, dir, influence)) {
                if (i == 0 && toBuild != RobotType.SLANDERER) {
                    directionIndex = (directionIndex + 1) % directions.length;
                }
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
                Message msg = FlagProtocol.decode(robotFlag);
                if (msg != null) {
                    MapLocation msgLocation = msg.getMapLocation(rc.getLocation());
                    enlightenmentCenters.put(msgLocation,msg.team);
                }
            } else {
                removeList.add(robotID);
            }
        }

        for (int removeID: removeList) {
            childRobotIds.remove(removeID);
        }

        numOfRobotDied += removeList.size();
    }
}