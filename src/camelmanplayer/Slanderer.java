package camelmanplayer;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Slanderer extends RobotPlayer{

    Slanderer(RobotController rc) {
        this.rc = rc;
        loadMotherInfo();
    }

    void runSlanderer() throws Exception {
        boolean stop = false;
        while (true) {
            if (rc.getType() == RobotType.SLANDERER) {
                if (stop) {
                    System.out.println("I am parking.");
                    Clock.yield();
                } else {
                    parkSlanderer();
                    stop = true;
                }
            } else {
                // Move as a politician
                warPhase = nextPhase;
                switch (warPhase) {
                    case SEARCH:
                        System.out.println("I am scanning!");
                        scanMap();
                        break;
                    case CONQUER:
                        System.out.println("I am conquering!");
                        movePoliticianToECenter();
                        break;
                    case ATTACK:
                        System.out.println("I am attacking!");
                        movePoliticianToECenter();
                        break;
                    case DEFEND:
                        System.out.println("I am defending!");
                        movePoliticianToECenter();
                        break;
                    default:
                        System.out.println("I am moving randomly!");
                        getFlagFromMom();
                        randomMovement();
                        break;
                }
            }
        }
    }

    private void parkSlanderer() throws GameActionException {
        MapLocation startLoc = rc.getLocation();
        Direction initDirection = motherLoc.directionTo(startLoc);
        Direction[] generalDirections = getGeneralDirections(initDirection);

        while (!moveInGeneralDirection(generalDirections)) {
            Clock.yield();
        }

        while (true) {
            //TODO: what if cannot move for a long time?
            try {
                moveInGeneralDirection(generalDirections);

                if (isParkingLot(rc.getLocation())) return;
                if (tryPark(generalDirections)) return;
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }

    private boolean moveInGeneralDirection(Direction[] generalDirections) throws GameActionException {
        int offSet = (int) (Math.random() * 4);

        for (int i = 0; i < 4; i++) {
            int index = (i + offSet + 4) % 4;
            Direction dir = generalDirections[index];
            if (tryMove(dir)) {
                Clock.yield();
                return true;
            }
        }
        return false;
    }

    private boolean tryPark(Direction[] generalDirections) throws GameActionException {
        for (Direction dir: generalDirections) {
            MapLocation targetLoc = rc.adjacentLocation(dir);
            if (isParkingLot(targetLoc)) {
                if (rc.canMove(dir)) {
                    System.out.println("I am heading "+dir+" to "+targetLoc);
                    rc.move(dir);
                    Clock.yield();
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isParkingLot(MapLocation targetLocation) {

        // Is it on the diagonal?
        int diff = targetLocation.x + targetLocation.y - motherLoc.x - motherLoc.y;
        if (diff % 2 != 0) return false;

        // Is it too close to the E-center?
        if (targetLocation.isWithinDistanceSquared(motherLoc, 2)) return false;
        System.out.println(targetLocation+" is a parking lot");
        return true;
    }

    private Direction[] getGeneralDirections(Direction dir) {
        ArrayList<Direction> directionList = new ArrayList<>();

        for (int i = 0; i < 16; i++) {
            int index = i % 8;
            directionList.add(directions[index]);
        }

        int indexOfDir = directionList.indexOf(dir);
        List<Direction> generalDirectionList = directionList.subList(indexOfDir, indexOfDir + 4);
        Direction[] generalDirectionArray = new Direction[4];
        generalDirectionList.toArray(generalDirectionArray);

        return generalDirectionArray;
    }
}
