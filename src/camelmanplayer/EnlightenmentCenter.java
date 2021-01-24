package camelmanplayer;

import java.util.*;
import battlecode.common.*;

public class EnlightenmentCenter extends RobotPlayer{

    private static WarPhase warPhase;
    private static int numOfRobotBuilt;
    private static int numOfRobotDied;
    private static HashMap<Integer, RobotInfo> robotAlive = new HashMap<>();
    private static HashMap<RobotType, Integer> robotTypeCount = new HashMap<>();
    private static HashMap<MapLocation, Team> enlightenmentCenters;
    private static Team opponent;
    private static int directionIndex;
    private static final double[][] robotRatio = {
            {0.2, 0.5, 0.3},
            {0.6, 0.3, 0.1},
            {0.5, 0.2, 0.3},
            {0.5, 0.5, 0.0}
    };


    EnlightenmentCenter(RobotController rc) {
        this.rc = rc;
        robotTypeCount.put(RobotType.POLITICIAN, 0);
        robotTypeCount.put(RobotType.SLANDERER, 0);
        robotTypeCount.put(RobotType.MUCKRAKER, 0);
        warPhase = WarPhase.SEARCH;
        numOfRobotBuilt = 0;
        numOfRobotDied = 0;
        enlightenmentCenters = new HashMap<>();
        enlightenmentCenters.put(rc.getLocation(), rc.getTeam());
        opponent = rc.getTeam().opponent();
        directionIndex = 0;
    }

    static void runEnlightenmentCenter() throws Exception {
        while (true) {
            setFlag();
            warPhase = updateWarPhase();
            buildRobot();
            listenToChildRobots();
            randomBid();
            System.out.println(enlightenmentCenters.size());
            Clock.yield();
        }
    }

    static void randomBid() throws GameActionException {
        int influenceForBid = (int) (rc.getInfluence() * 0.04);
        if (rc.canBid(influenceForBid)) {
            rc.bid(influenceForBid);
        }
    }

    static double survivalRate() {
        return 1 - (double) numOfRobotDied / (double) numOfRobotBuilt;
    }

    static boolean surroundedByEnemy() {
        RobotInfo[] nearbyRobots = rc.senseNearbyRobots();
        int numOfEnemy = 0;
        int numOfUs = 0;

        for (RobotInfo rInfo: nearbyRobots) {
            if (rInfo.team == opponent) {
                numOfEnemy++;
            } else {
                numOfUs++;
            }
        }
        return numOfEnemy > (numOfUs / 3);
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

        if ((rc.getConviction() < 20 || survivalRate() < 0.1) && surroundedByEnemy()) {
            //TODO: make smarter thresholds
            return WarPhase.DEFEND;
        }

        if (enlightenmentCenters.size() == 1) {
            return WarPhase.SEARCH;
        }
        else if (getClosestEnlightmentCenter(opponent) != null) {
            return WarPhase.ATTACK;
        }
        return WarPhase.SEARCH;
    }

    static void setFlag() throws GameActionException {
        Message msgToSend = null;
        switch (warPhase) {
            case SEARCH:
                System.out.println("Time to Search!");
                msgToSend = new Message(MessageType.ECENTER, WarPhase.SEARCH, rc.getTeam()); break;
            case CONQUER:
                MapLocation neutralLocation = getClosestEnlightmentCenter(Team.NEUTRAL);
                if (neutralLocation != null) {
                    msgToSend = new Message(MessageType.ECENTER, WarPhase.CONQUER, Team.NEUTRAL, neutralLocation, rc.getLocation());
                    System.out.println("Time to Conquer "+neutralLocation);
                }
                break;
            case ATTACK:
                System.out.println("Time to Attack!");
                MapLocation enemyLocation = getClosestEnlightmentCenter(opponent);
                msgToSend = new Message(MessageType.ECENTER, WarPhase.ATTACK, opponent, enemyLocation, rc.getLocation());
                break;
            case DEFEND:
                System.out.println("Time to Defend!");
                msgToSend = new Message(MessageType.ECENTER, WarPhase.DEFEND, rc.getTeam());
                break;
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
                return getToBuild(robotRatio[0]);
//                return randomRobotType(0.3,0.3,0.4);
            case CONQUER:
                return getToBuild(robotRatio[1]);
//                return randomRobotType(0.7,0.2,0.1);
            case ATTACK:
                return getToBuild(robotRatio[2]);
//                return randomRobotType(0.5,0.2,0.3);
            case DEFEND:
                return getToBuild(robotRatio[3]);
//                return randomRobotType(0.5,0.5,0);
            default:
                return randomRobotType(0.33,0.33,0.33);
        }
    }

    static RobotType getToBuild(double[] referenceRatios) {
        double numOfRobotAlive = robotAlive.size();
        double politicianOff = referenceRatios[0] - robotTypeCount.get(RobotType.POLITICIAN) / numOfRobotAlive;
        double slandererOff = referenceRatios[1] - robotTypeCount.get(RobotType.SLANDERER) / numOfRobotAlive;
        double muckrackerOff = referenceRatios[2] - robotTypeCount.get(RobotType.MUCKRAKER) / numOfRobotAlive;

        if (politicianOff > slandererOff && politicianOff > muckrackerOff) return RobotType.POLITICIAN;
        if (slandererOff > muckrackerOff) return RobotType.SLANDERER;
        return RobotType.MUCKRAKER;

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
        int influence = 0;
        int motherInfluence = rc.getInfluence();
        RobotType rType = rc.getType();
//        switch (warPhase){
//            case SEARCH:
                if (rType == RobotType.SLANDERER){
                    influence =  (motherInfluence * 2) / 3;
                }
                else if (rType == RobotType.POLITICIAN){
                    influence = motherInfluence/4;
                }
                else {
                    influence = 1;
                }
//                break;
//            case CONQUER:
//            case ATTACK:
//            case DEFEND:
//                if (rType == RobotType.SLANDERER){
//                    influence =  (motherInfluence * 2) / 3;
//                }
//                else if (rType == RobotType.POLITICIAN){
//                    influence = motherInfluence/3;
//                }
//                else {
//                    influence = 10;
//                }
//                break;
//        }

        influence = Math.min(influence, motherInfluence - 22);
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
                updateChildRobotInfo(dir);
                numOfRobotBuilt++;
                int newNum = robotTypeCount.get(toBuild) + 1;
                robotTypeCount.put(toBuild, newNum);
            } else {
                break;
            }
        }
    }

    static void updateChildRobotInfo(Direction dir) throws GameActionException {
        MapLocation robotLocation = rc.adjacentLocation(dir);
        RobotInfo robotInfo = rc.senseRobotAtLocation(robotLocation);
        robotAlive.put(robotInfo.ID, robotInfo);
    }

    static void listenToChildRobots() throws GameActionException {
        List<Integer> removeList = new ArrayList<>();
        for (int robotID: robotAlive.keySet()) {
            if (rc.canGetFlag(robotID)) {
                int robotFlag = rc.getFlag(robotID);
                Message msg = FlagProtocol.decode(robotFlag);
                if (msg != null) {
                    switch (msg.msgType) {
                        case ECENTER:
                            MapLocation msgLocation = msg.getMapLocation(rc.getLocation());
                            enlightenmentCenters.put(msgLocation,msg.team);
                            break;
                            //TODO: figure out how to react
                        case WALL:
                        case CORNER:

                            break;
                    }
                }
            } else {
                removeList.add(robotID);
            }
        }

        for (int removeID: removeList) {
            RobotType robotType = robotAlive.get(removeID).type;
            int newNum = robotTypeCount.get(robotType) - 1;
            robotTypeCount.put(robotType, newNum);
            robotAlive.remove(new Integer(removeID));

        }

        numOfRobotDied += removeList.size();
        System.out.println("Finished listening to all children at round number: "+turnCount);
    }


}