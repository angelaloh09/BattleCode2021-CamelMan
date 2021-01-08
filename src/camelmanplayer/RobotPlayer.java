package camelmanplayer;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
}
