package camelmanplayer;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.HashMap;
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

    static final double[][] scanFstMoveLeftXYRatio = new double[][] {
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
        // TODO: every while loop is a round now, so we need to change the declaration
        while (true) {
            turnCount += 1;
            // Try/catch blocks stop unhandled exceptions, which cause your robot to freeze
            try {
                // Here, we've separated the controls into a different method for each RobotType.
                // You may rewrite this into your own control structure if you wish.
                System.out.println("I'm a " + rc.getType() + "! Location " + rc.getLocation());
                switch (rc.getType()) {
                    case ENLIGHTENMENT_CENTER:
                        EnlightenmentCenter myEnlightenmentCenter = new EnlightenmentCenter(rc);
                        myEnlightenmentCenter.runElightenmentCenter();
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

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();
            }
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

    static void aStarPlanning(MapLocation destination) throws GameActionException {
        HashMap<Direction, Integer> adjacentLocMap = new HashMap<>();

        for (Direction direction: directions) {
            MapLocation adjacentLoc = rc.adjacentLocation(direction);

            if (adjacentLoc != null) {
                double passability = rc.sensePassability(adjacentLoc);
                int gCost = (int) (0.5 / passability)^2; // Convert passibility to disctance
                int hCost = adjacentLoc.distanceSquaredTo(destination);
                adjacentLocMap.put(direction, gCost + hCost);
            }
        }
    }

    void tryMoveWithCatch(Direction dir) {
        try {
            tryMove(dir);
        } catch (GameActionException e) {
            // TODO: take the wall as a boundary
            System.out.println("oops, just bumped into the wall");
        }
    }

    void moveToDestination(Direction direction, MapLocation destination) {
        MapLocation currLoc = rc.getLocation();
        int currxDist = currLoc.x - destination.x;
        int curryDist = currLoc.y - destination.y;
        // while we haven't exceeded the destination, keep moving forward
        while ((rc.getLocation().x - destination.x) / currxDist >= 0
                && (rc.getLocation().y - destination.y) / curryDist >= 0) {
            tryMoveWithCatch(direction);
        }
    }

    void moveByDist(Direction direction, int distance) {
        while (distance > 0) {
            tryMoveWithCatch(direction);
            distance -= 1;
        }
    }


    // scanning algorithm

    /** let the bot make the first turn when it's scanning its section */
    void scanFstMoveLeft(int i, int travelDist, Direction dir) {
        int currLocx = rc.getLocation().x;
        int currLocy = rc.getLocation().y;

        // first left boundary location
        int fstBoundx = (int) (currLocx + travelDist * scanFstMoveLeftXYRatio[i][0]);
        int fstBoundy = (int) (currLocy + travelDist * scanFstMoveLeftXYRatio[i][1]);
        MapLocation fstBound = new MapLocation(fstBoundx, fstBoundy);

        // keep moving until we reach the boundary
        moveToDestination(dir, fstBound);
    }

    /** turn 135 degree CW and move the bot to the right boundary */
    void scanMoveRight(int i, MapLocation lastMainAxisLoc, Direction dir) {
        int currLocx = rc.getLocation().x;
        int currLocy = rc.getLocation().y;

        // calculate the second boundary
        int rightBoundx = (int) (currLocx + Math.abs(lastMainAxisLoc.x - currLocx) * scanMoveRightXYRatio[i][0]);
        int rightBoundy = (int) (currLocy + Math.abs(lastMainAxisLoc.y - currLocy) * scanMoveRightXYRatio[i][1]);
        MapLocation rightBound = new MapLocation (rightBoundx, rightBoundy);

        moveToDestination(dir, rightBound);
    }

    /** turn 45 degree CCW and move the bot to the left boundary */
    void scanMoveLeft(int i, MapLocation ECenterLoc, Direction dir) {
        int currLocx = rc.getLocation().x;
        int currLocy = rc.getLocation().y;

        // calculate the distance between the current location and the destination on th left boundary
        int xDiff = Math.abs(currLocx - ECenterLoc.x);
        int yDiff = Math.abs(currLocy - ECenterLoc.y);
        int dist = (int) Math.sqrt((xDiff^2 + yDiff^2));

        moveByDist(dir, dist);
    }

    /** make the bot make zigzag movements while it's scanning its section */
    void scanMoveZigzag(int fstTravelDist, int i, MapLocation lastMainAxisLoc, MapLocation ECenterLoc) {
        // left direction
        int leftDirIdx = (i - 1) % directions.length;
        Direction leftDir = directions[leftDirIdx];

        // right direction
        int rightDirIdx = (i + 2) % directions.length;
        Direction rightDir = directions[rightDirIdx];

        // move to the left boundary from the central line
        scanFstMoveLeft(i, fstTravelDist, leftDir);

        // TODO: figure out the condition, could try editing tryMoveWithCatch()
        while (true) {
            // first move right
            scanMoveRight(i, lastMainAxisLoc, rightDir);

            // then move left
            scanMoveLeft(i, ECenterLoc, leftDir);
        }
    }

    /** scan the entire map at the start of the game */
    // TODO: stop all the bots when an E-Center is found
    void scanMap() {
        // initialize the movement by getting the E-center coordinates
        MapLocation ECenterLoc = rc.getLocation();

        int ELocx = ECenterLoc.x;
        int ELocy = ECenterLoc.y;

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
            int fstTravelDist = (int) Math.sqrt((currLocx - ELocx)^2 + (currLocy - ELocy)^2);

            scanMoveZigzag(fstTravelDist, i, rc.getLocation(), ECenterLoc);
        }
    }
}