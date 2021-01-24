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

    /**
     * Main function for moving the slanderer to a parking spot,
     * i.e. diagonal lines around the e-center
     */
    private void parkSlanderer() throws GameActionException {
        // According to the direction that the slanderer was built, get an array of general directions
        MapLocation startLoc = rc.getLocation();
        Direction initDirection = motherLoc.directionTo(startLoc);
        Direction[] generalDirections = getGeneralDirections(initDirection);

        // Pass the 10 frozen round with Clock.yield();
        while (!moveInGeneralDirection(generalDirections)) {
            Clock.yield();
        }

        while (true) {
            try {
                // Move one step towards the generalDirections
                moveInGeneralDirection(generalDirections);

                // If the current location is a parking lot, done.
                if (isParkingLot(rc.getLocation())) return;

                // Try to park to the nearby parking lots
                if (tryPark(generalDirections)) return;
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }

    /**
     * Move the robot in the general directions for one step.
     * Start with a random one in the Array, and loop through all of them.
     * Return if the move was successful.
     */
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

    /**
     * Move the robot to an adjacent parking lot in the general directions.
     * Return if the move was successful.
     */
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

    /**
     * Return whether the targetLocation is a parkingLot.
     */
    private boolean isParkingLot(MapLocation targetLocation) {

        // Is it on the diagonal?
        int diff = targetLocation.x + targetLocation.y - motherLoc.x - motherLoc.y;
        if (diff % 2 != 0) return false;

        // Is it too close to the E-center?
        if (targetLocation.isWithinDistanceSquared(motherLoc, 2)) return false;
        System.out.println(targetLocation+" is a parking lot");
        return true;
    }

    /**
     * Return an Array of length 4, which is the four direction next to Direction dir.
     * e.g. if the initial direction is NORTH, general directions will be NORTH, NORTHEAST, EAST, EASTSOUTH.
     */
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
