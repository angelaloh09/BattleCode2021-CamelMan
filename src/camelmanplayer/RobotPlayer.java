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

    void tryMoveWithCatch(Direction dir) {
        try {
            tryMove(dir);
        } catch (GameActionException e) {
            System.out.println("oops, failed to move");
        }
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

    void moveStraightLine(Direction direction, MapLocation destination) {
        while (rc.getLocation() != destination) {
            tryMoveWithCatch(direction);
        }
    }

    void moveZigzag() {

    }

    // do the zig zag movements to scan 1/8 of a map
    // TODO: factor out math functions to work for robots in other quadrants of map.
    void scanMap() {
        // get starting location (E-center coordinates)
        MapLocation ELoc = MapLocation();
        int ELocx = ELoc.x;
        int ELocy = ELoc.y;

        for (int i = 0; i < directions.length; i ++) {
            // current direction
            Direction direction = directions[i];

            // TODO: maybe need to factor this move part out of the for loop?
            // first move one step forward in this direction
            tryMoveWithCatch(direction);

            //travel_distance = get travelled distance
            // current location
            MapLocation currLoc = MapLocation();
            int currLocx = currLoc.x;
            int currLocy = currLoc.y;
            int travelDist = Math.sqrt((currLocx - ELocx)**2 + (currLocy - ELocy)**2);

            // turn CCW 45 degrees
            int leftDirIdx = (i - 1) % directions.length;

            Direction newDir = directions[leftDirIdx];

            // first boundary location
            // TODO: need to change boundary calculation based on quadrant!!
            int fstBoundx = currLocx + travelDist * Math.cos(45);
            int fstBoundy = currLocy + travelDist * Math.sin(45);
            MapLocation fstBound = new MapLocation(fstBoundx, fstBoundy);

            // TODO optimization: have robot travel past boundary for overlap

            // keep moving until we reach the boundary
            moveStraightLine(newDir, fstBound);
            MapLocation adjacentLoc = rc.adjacentLocation(direction);

            // turn CW by 135 degrees
            int rightDirIdx = (i + 2) % directions.length;

            Direction newDir = directions[rightDirIdx];

            // calculate the second boundary
            //TODO: change based on quadrant!!
            int yDiff = 2 * Math.abs(fstBoundy - ELocy);
            int sndBoundy = fstBoundy - yDiff;
            MapLocation sndBound = new MapLocation (fstBoundx, sndBoundy);
            moveStraightLine(newDir, sndBound);

            // robot should currently be at second bound
            //next robot needs to return to line with either the same x or y as E-center.

            // then go all over again
        }
    }
}