package camelmanplayer;

import battlecode.common.*;
import sun.jvm.hotspot.memory.SymbolTable;

import java.util.*;

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

    static MapLocation motherLoc;

    static int motherId;

    static WarPhase warPhase = WarPhase.SEARCH;

    static HashMap<MapLocation, Team> enlightenmentCenters = new HashMap<>();

    static MapLocation targetECenter;

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

    // apply the universal principle
    void applyUP() throws GameActionException {
        switch (rc.getType()) {
            case POLITICIAN:
                if (warPhase != WarPhase.SEARCH) {
                    pUniversalPrinciple();
                }
                break;
            case MUCKRAKER:
                mUniversalPrinciple();
                break;
            default:
                break;
        }
    }

    boolean tryMoveWithCatch(Direction dir) throws Exception {
        applyUP();

        try {
            tryMove(dir);
            // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
            getFlagFromMom();
            turnCount += 1;
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

            getFlagFromMom();
            turnCount += 1;
            Clock.yield();
            return false;

        }
    }

    // search phase

    /** keep the bot moving when it hasn't reached the destination */
    void moveToDestination (MapLocation destination, int squaredDis) throws Exception {
        while (rc.getLocation().distanceSquaredTo(destination) > squaredDis) {
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
        moveToDestination(dest, 0);
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

    void getFlagFromMom() throws GameActionException {
        if (rc.canGetFlag(motherId)) {
            Message motherMsg = FlagProtocol.decode(rc.getFlag(motherId));
            if (motherMsg != null) {
                targetECenter = motherMsg.getMapLocation(motherLoc);
                warPhase = motherMsg.warPhase;
                enlightenmentCenters.put(targetECenter, motherMsg.team);
            }
        } else {
            warPhase = WarPhase.DEFEND;
            targetECenter = motherLoc;
        }
    }

    /** make the bot make zigzag movements while it's scanning its section */
    void scanMoveZigzag(double fstTravelDist, int i, MapLocation lastMainAxisLoc, MapLocation ECenterLoc) throws Exception{
        // left direction
        int leftDirIdx = (i - 1) % directions.length;
        Direction leftDir = directions[leftDirIdx];

        // move to the left boundary from the central line
        scanFstMoveLeft(i, fstTravelDist);

        RobotInfo[] rInfoLst = rc.senseNearbyRobots();
        // make sure that this ECenter is not the original one
        for (RobotInfo rInfo : rInfoLst) {
            RobotType type = rInfo.getType();
            MapLocation loc = rInfo.getLocation();
            if (!loc.equals(motherLoc) && type == RobotType.ENLIGHTENMENT_CENTER) {
                // send the info of this ECenter to mom
                Message msg = new Message(WarPhase.SEARCH, rInfo.getTeam(), rInfo.getLocation(), motherLoc);
                int flag = FlagProtocol.encode(msg);
                if (rc.canSetFlag(flag)) {
                    rc.setFlag(flag);
                }
            }
        }

        // get info from mom to see if we need to keep scanning
        getFlagFromMom();

        while (warPhase == WarPhase.SEARCH) {

            // first move right
            scanMoveRight(i, lastMainAxisLoc);

            // then move left
            scanMoveLeft(i, ECenterLoc);
        }

    }

    /** scan 1/8 of the map at the start of the game */
    void scanMap() {
        // initialize the movement by getting the E-center coordinates
        MapLocation startLoc = rc.getLocation();

        try {
            Direction direction = directions[(int) (directions.length * Math.random())];
            RobotInfo[] rinfolst =  rc.senseNearbyRobots(1);
            for (RobotInfo rinfo : rinfolst) {
                if (rinfo.getType() == RobotType.ENLIGHTENMENT_CENTER) {
                    MapLocation ELoc = rinfo.getLocation();
                    motherLoc = ELoc;
                    motherId = rinfo.getID();
                    // main direction of scanning
                    direction = ELoc.directionTo(startLoc);
                }
            }

            // first move one step forward in this direction
            tryMoveWithCatch(direction);

            // current location
            MapLocation currLoc = rc.getLocation();
            int currLocx = currLoc.x;
            int currLocy = currLoc.y;

            // the distance travelled from the starting point of the scan (E-Center)
            double fstTravelDist = Math.sqrt((currLocx - startLoc.x) ^ 2 + (currLocy - startLoc.y) ^ 2);

            int i = Arrays.asList(directions).indexOf(direction);
            scanMoveZigzag(fstTravelDist, i, rc.getLocation(), startLoc);

        } catch (Exception e) {
            System.out.println(e);
        }
    }


    // conquer and attack phases

    void pUniversalPrinciple() throws GameActionException {
        // try move
        Team enemy = rc.getTeam().opponent();
        int actionRadius = rc.getType().actionRadiusSquared;
        RobotInfo[] attackable = rc.senseNearbyRobots(actionRadius, enemy);
        if (attackable.length != 0 && rc.canEmpower(actionRadius)) {
            System.out.println("empowering...");
            rc.empower(actionRadius);
            System.out.println("empowered");
            return;
        }
    }

    void mUniversalPrinciple() throws GameActionException {
        Team enemy = rc.getTeam().opponent();
        int actionRadius = rc.getType().actionRadiusSquared;
        for (RobotInfo robot : rc.senseNearbyRobots(actionRadius, enemy)) {
            if (robot.type.canBeExposed()) {
                // It's a slanderer... go get them!
                if (rc.canExpose(robot.location)) {
                    System.out.println("e x p o s e d");
                    rc.expose(robot.location);
                    return;
                }
            }
        }
    }

    void randomMovement() throws GameActionException {
        if (tryMove(randomDirection()))
            System.out.println("I moved!");
        turnCount += 1;
        getFlagFromMom();
        Clock.yield();
    }

    void goToECenter() throws Exception {
        // listen to mom and move to the target ECenter
        // TODO: what if we cannot get there
        int actionRS = rc.getType().actionRadiusSquared;
        moveToDestination(targetECenter, actionRS);

        if (rc.canEmpower(actionRS)) {
            // empower and die gloriously
            turnCount += 1;
            rc.empower(actionRS);
            Clock.yield();
        } else {
            // move randomly like a soldier who doesn't know the meaning of his life
            randomMovement();
        }
    }
}