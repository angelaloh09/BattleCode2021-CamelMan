package camelmanplayer;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public strictfp class RobotPlayer {
    static RobotController rc;

    static final RobotType[] spawnableRobot = {
            RobotType.POLITICIAN,
            RobotType.SLANDERER,
            RobotType.MUCKRAKER,
    };

    static final Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
    };

    enum WarPhase {
        SEARCH,
        CONQUER,
        ATTACK,
        DEFEND
    }

    static final double[][] scanMoveLeftXYRatio = new double[][] {
            {-Math.sqrt(1/2), Math.sqrt(1/2)},
            {0, 1},
            {Math.sqrt(1/2), Math.sqrt(1/2)},
            {1, 0},
            {Math.sqrt(1/2), -Math.sqrt(1/2)},
            {0, -1},
            {-Math.sqrt(1/2), -Math.sqrt(1/2)},
            {-1, 0}
    };

    static final double[][] scanMoveRightXYRatio = new double[][] {
            {2, 0},
            {1, -1},
            {0, -2},
            {-1, -1},
            {-2, 0},
            {-1, 1},
            {0, 2},
            {1, 1}
    };

    static int turnCount;

    static List<MapLocation> path;

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

        // This is the RobotController object. You use it to perform actions from this robot,
        // and to get information on its current status.
        RobotPlayer.rc = rc;
        RobotPlayer.path = new ArrayList<>();

        turnCount = 0;

        System.out.println("I'm a " + rc.getType() + " and I just got created!");
        // Try/catch blocks stop unhandled exceptions, which cause your robot to freeze
        try {
            // Here, we've separated the controls into a different method for each RobotType.
            // You may rewrite this into your own control structure if you wish.
            System.out.println("I'm a " + rc.getType() + "! Location " + rc.getLocation());
            switch (rc.getType()) {
                case ENLIGHTENMENT_CENTER:
                    EnlightenmentCenter myEnlightenmentCenter = new EnlightenmentCenter(rc);
                    myEnlightenmentCenter.runEnlightenmentCenter();
                    break;
                case POLITICIAN:
                    Politician myPolitician = new Politician(rc);
                    myPolitician.runPolitician();
                    break;
                case SLANDERER:
                    Slanderer mySlanderer = new Slanderer(rc);
                    mySlanderer.runSlanderer();
                    break;
                case MUCKRAKER:
                    Muckraker myMuckraker = new Muckraker(rc);
                    myMuckraker.runMuckraker();
                    break;
            }

        } catch (Exception e) {
            System.out.println(rc.getType() + " Exception");
            e.printStackTrace();
        }
    }

    /**
     * Returns a random Direction.
     *
     * @return a random Direction
     */
    static Direction randomDirection() {
        return directions[(int) (Math.random() * directions.length)];
    }

    /**
     * Returns a random spawnable RobotType
     *
     * @return a random RobotType
     */
    static RobotType randomSpawnableRobotType() {
        return spawnableRobot[(int) (Math.random() * spawnableRobot.length)];
    }

    /**
     * Attempts to move in a given direction.
     *
     * @param dir The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryMove(Direction dir) throws GameActionException {
        System.out.println("I am trying to move " + dir + "; " + rc.isReady() + " " + rc.getCooldownTurns() + " " + rc.canMove(dir));
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        } else return false;
    }

    boolean tryMoveWithCatch(Direction dir) throws Exception {
        try {
            tryMove(dir);
            // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
            Clock.yield();
            return true;
        } catch (GameActionException cannotMove) {
            System.out.println("oops, cannot move");

            MapLocation adjacentLoc = rc.adjacentLocation(dir);

            if (!rc.onTheMap(adjacentLoc)) {
                System.out.println("oops, just bumped into the wall");
                // TODO: take the wall as a boundary

            } else if (rc.isLocationOccupied(adjacentLoc)) {
                System.out.println("the adjacent location is occupied ;-;");
                // TODO: process rInfo, should come up with some responses that these scouts do specifically in scanning

                RobotInfo rInfo;
                rInfo = rc.senseRobotAtLocation(adjacentLoc);

            } else {
                System.out.println("cool-down turns:" + rc.getCooldownTurns());
                System.out.println("still in cool down or something weird happened");
            }

            Clock.yield();
            return false;

        }
    }

    // scanning algorithm

    /** keep the bot moving when it hasn't reached the destination */
    void moveToDestination (MapLocation destination) throws Exception {
        while (!rc.getLocation().equals(destination)) {
            // a list of locations to go to the location closest to the destination
            LinkedList<MapLocation> locs = AStarPath.aStarPlanning(rc, destination);
            while (!locs.isEmpty()) {
                // if we haven't reached the closest location, keep going
                MapLocation head = locs.peek();
                Direction nextDir = rc.getLocation().directionTo(head);
                boolean moved = tryMoveWithCatch(nextDir);
                if (moved) {
                    // if we've moved to the first node successfully, remove the first node
                    locs.remove();
                }
            }
        }
    }

    void scanMove(int xDiff, int yDiff) throws Exception {
        MapLocation currLoc = rc.getLocation();
        MapLocation dest = currLoc.translate(xDiff, yDiff);
        moveToDestination(dest);
    }

    /** let the bot make the first turn when it's scanning its section */
    void scanFstMoveLeft(int i, double travelDist) throws Exception {
        // first left boundary location
        int xDiff = (int) (travelDist * scanMoveLeftXYRatio[i][0]);
        int yDiff = (int) (travelDist * scanMoveLeftXYRatio[i][1]);
        scanMove(xDiff, yDiff);
    }

    /** turn 135 degree CW and move the bot to the right boundary */
    void scanMoveRight(int i, MapLocation lastMainAxisLoc) throws Exception {
        MapLocation currLoc = rc.getLocation();
        // calculate the second boundary
        int xDiff = (int) (Math.abs(lastMainAxisLoc.x - currLoc.x) * scanMoveRightXYRatio[i][0]);
        int yDiff = (int) (Math.abs(lastMainAxisLoc.y - currLoc.y) * scanMoveRightXYRatio[i][1]);
        scanMove(xDiff, yDiff);
    }

    /** turn 45 degree CCW and move the bot to the left boundary */
    void scanMoveLeft(int i, MapLocation ECenterLoc) throws Exception {
        MapLocation currLoc = rc.getLocation();

        int xDiffToECenter = Math.abs(currLoc.x - ECenterLoc.x);
        int yDiffToECenter = Math.abs(currLoc.y - ECenterLoc.y);

        // the distance between the current location and the destination on th left boundary
        double travelDist = yDiffToECenter + (1 + Math.sqrt(2)) * xDiffToECenter;

        // the coordinate difference to the destination
        int xDiff = (int) (travelDist * scanMoveLeftXYRatio[i][0]);
        int yDiff = (int) (travelDist * scanMoveLeftXYRatio[i][1]);

        scanMove(xDiff, yDiff);
    }

    /** make the bot make zigzag movements while it's scanning its section */
    void scanMoveZigzag(double fstTravelDist, int i, MapLocation lastMainAxisLoc, MapLocation ECenterLoc) throws Exception{
        // left direction
        int leftDirIdx = (i - 1) % directions.length;
        Direction leftDir = directions[leftDirIdx];

        // move to the left boundary from the central line
        scanFstMoveLeft(i, fstTravelDist);

        // TODO: check the info from ECenter every round and stop when a new ECenter is found
        while (true) {
            // first move right
            scanMoveRight(i, lastMainAxisLoc);

            // then move left
            scanMoveLeft(i, ECenterLoc);
        }

    }

    /** scan 1/8 of the map at the start of the game */
    void scanMap() {
        // initialize the movement by getting the E-center coordinates
        MapLocation ECenterLoc = rc.getLocation();

        int ELocx = ECenterLoc.x;
        int ELocy = ECenterLoc.y;

        try {
            // TODO: move the for loop to ECenter, this class only controls a single robot
            for (int i = 0; i < directions.length; i ++) {
                // current direction
                Direction direction = directions[i];

                // first move one step forward in this direction
                tryMoveWithCatch(direction);

                // current location
                MapLocation currLoc = rc.getLocation();
                int currLocx = currLoc.x;
                int currLocy = currLoc.y;

                // the distance travelled from the starting point of the scan (E-Center)
                double fstTravelDist = Math.sqrt((currLocx - ELocx) ^ 2 + (currLocy - ELocy) ^ 2);

                scanMoveZigzag(fstTravelDist, i, rc.getLocation(), ECenterLoc);

            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}