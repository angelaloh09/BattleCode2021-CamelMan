package camelmanplayer;

import battlecode.common.*;

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


    // UPDATED
    enum MessageType{
        ECENTER,
        SCOUTDANGER,
        WALL,
        CORNER
    }

    static final double[][] scanMoveLeftXYRatio = new double[][] {
            {-Math.sqrt(1.0/2.0), Math.sqrt(1.0/2.0)},
            {0.0, 1.0},
            {Math.sqrt(1.0/2.0), Math.sqrt(1.0/2.0)},
            {1.0, 0.0},
            {Math.sqrt(1.0/2.0), -Math.sqrt(1.0/2.0)},
            {0.0, -1.0},
            {-Math.sqrt(1.0/2.0), -Math.sqrt(1.0/2.0)},
            {-1.0, 0.0}
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

    static WarPhase warPhase;

    // TODO: figure out how to prioritize each ECenter
    static HashMap<MapLocation, Team> enlightenmentCenters = new HashMap<>();

    static MapLocation targetECenter;

    static WarPhase nextPhase = WarPhase.SEARCH;


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

        // Try/catch blocks stop unhandled exceptions, which cause your robot to freeze
        try {
            // Here, we've separated the controls into a different method for each RobotType.
            // You may rewrite this into your own control structure if you wish.
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
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        } else return false;
    }

    // UPDATED
    static void setMessageFlag(MessageType mType, WarPhase warPhase, Team team, MapLocation currLoc)
    throws GameActionException {
        Message msg = new Message(mType, warPhase, team, currLoc, motherLoc);
        int flag = FlagProtocol.encode(msg);
        if (rc.canSetFlag(flag)) {
            rc.setFlag(flag);
        }
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

    void senseNewEC() throws GameActionException {
        RobotInfo[] rInfoLst = rc.senseNearbyRobots();
        // make sure that this ECenter is not the original one
        for (RobotInfo rInfo : rInfoLst) {
            RobotType type = rInfo.getType();
            MapLocation loc = rInfo.getLocation();
            if (!loc.equals(motherLoc) && type == RobotType.ENLIGHTENMENT_CENTER) {
                // send the info of this ECenter to mom
                System.out.println("I found an enlightenment center!");
                Message msg = new Message(WarPhase.SEARCH, rInfo.getTeam(), rInfo.getLocation(), motherLoc);
                int flag = FlagProtocol.encode(msg);
                if (rc.canSetFlag(flag)) {
                    rc.setFlag(flag);
                }
            }
        }
    }

    // UPDATED:
//    static Direction diverge(int enemyCount, int enemyThreshold) throws GameActionException {
//        Team team = rc.getTeam();
//        MapLocation currLoc = rc.getLocation();
//        if (enemyCount > enemyThreshold) {
//            System.out.println("I am in danger:O !");
//            setMessageFlag(MessageType.SCOUTDANGER, WarPhase.SEARCH, team, currLoc);
//        }
//        // avoid dead-lock regardless of team
//        Direction randomDir =  randomDirection();
//        return randomDir;
//    }

    String tryMoveWithCatch(Direction dir) throws Exception {
        applyUP();
        // for each step, scan the surrounding first
        // TODO: make sure this function is called every round
        senseNewEC();

        Team team = rc.getTeam();
        MapLocation currLoc = rc.getLocation();

        try {
            rc.move(dir);
            // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
            getFlagFromMom();
            turnCount += 1;
            Clock.yield();
            System.out.println("I've been alive for "+turnCount+" turns!");
            return "moved";
        } catch (GameActionException cannotMove) {
            System.out.println("oops, cannot move");

            MapLocation adjacentLoc = rc.adjacentLocation(dir);

            // check if at least 5 of the adjacent nodes are not on the map
            boolean corner = false;
            int boundCounter = 0;
            for (Direction direction: directions){
                if (!rc.onTheMap(rc.adjacentLocation(direction))){
                    boundCounter ++;
                }
                if (boundCounter == 5){
                    corner = true;
                }
            }
            // case 1: Scout in corner
            if (corner){
                setMessageFlag(MessageType.CORNER, WarPhase.SEARCH, team, currLoc);
                Direction random_dir = directions[(int) (directions.length * Math.random())];
                tryMoveWithCatch(random_dir);
            }
            // case 2: Scout bumps into wall
            else if (!rc.onTheMap(adjacentLoc)) {
                System.out.println("oops, just bumped into the wall");
                setMessageFlag(MessageType.WALL, WarPhase.SEARCH, team, currLoc);
                Direction random_dir = directions[(int) (directions.length * Math.random())];
                tryMoveWithCatch(random_dir);
            }
            // case 3: location is occupied
            else if (rc.isLocationOccupied(adjacentLoc)) {
                System.out.println("the adjacent location is occupied ;-;");
                String msg = "";
                while (msg != "moved") {
                    Direction random_dir = directions[(int) (directions.length * Math.random())];
                    msg = tryMoveWithCatch(random_dir);
                }
                return "didRandomMove";
            } else {
                System.out.println("cool-down turns:" + rc.getCooldownTurns());
            }
            getFlagFromMom();
            turnCount += 1;
            Clock.yield();
            return "bye path planning";
        }
    }

    // search phase
    /** keep the bot moving when it hasn't reached the destination
     while scanning the surrounding for new ECenter */
    void moveToDestination (MapLocation destination, int squaredDis) throws Exception {
        while (rc.getLocation().distanceSquaredTo(destination) > squaredDis) {
            // a list of locations to go to the location closest to the destination
            LinkedList<MapLocation> locs = AStarPath.aStarPlanning(rc, destination);
            while (!locs.isEmpty()) {
                // if we haven't reached the closest location, keep going
                MapLocation tail = locs.getLast();
                Direction nextDir = rc.getLocation().directionTo(tail);
                String moveMsg = tryMoveWithCatch(nextDir);
                if (moveMsg == "moved") {
                    // if we've moved to the first node successfully, remove the first node
                    locs.removeLast();
                }
                if (moveMsg == "didRandomMove") {
                    locs = AStarPath.aStarPlanning(rc, destination);
                }
                if (nextPhase != warPhase) return;
            }
        }
    }

    /** a higher order function for scanMoveLeft and scanMoveRight */
    void scanMove(int xDiff, int yDiff) throws Exception {
        System.out.println("inside scan move");
        System.out.println("xDiff: " + xDiff);
        System.out.println("yDiff: " + yDiff);
        MapLocation currLoc = rc.getLocation();
        MapLocation dest = currLoc.translate(xDiff, yDiff);
        moveToDestination(dest, 0);
    }

    /** let the bot make the first turn when it's scanning its section */
    void scanFstMoveLeft(int i, double travelDist) throws Exception {
        System.out.println("inside scan fst move left");
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

        System.out.println("i direction" + i);

        int xDiffToECenter = Math.abs(currLoc.x - motherLoc.x);
        int yDiffToECenter = Math.abs(currLoc.y - motherLoc.y);
        System.out.println("xDiffToEC: " + xDiffToECenter);
        System.out.println("yDiffToEC: " + yDiffToECenter);

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
        // move to the left boundary from the central line
        scanFstMoveLeft(i, fstTravelDist);

        // get info from mom to see if we need to keep scanning
        getFlagFromMom();

        while (warPhase == WarPhase.SEARCH) {
            // first move right
            scanMoveRight(i, lastMainAxisLoc);

            // then move left
            scanMoveLeft(i, ECenterLoc);
        }

    }

    void loadMotherInfo() {
        RobotInfo[] rinfolst =  rc.senseNearbyRobots(2);
        for (RobotInfo rinfo : rinfolst) {
            if (rinfo.getType() == RobotType.ENLIGHTENMENT_CENTER) {
                motherLoc = rinfo.getLocation();
                motherId = rinfo.getID();
                break;
            }
        }
    }

    /** scan 1/8 of the map at the start of the game */
    void scanMap() {
        // initialize the movement by getting the E-center coordinates
        // main direction of scanning
        Direction direction;
        MapLocation startLoc = rc.getLocation();
        direction = motherLoc.directionTo(startLoc);

        try {
            // first move one step forward in this direction
            int movedSteps = 0;
            while (movedSteps < 3) {
                if (tryMoveWithCatch(direction).equals("moved")) movedSteps++;
            }

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

    // TODO: add a while loop to try all 8 directions
    void randomMovement() throws GameActionException {
        System.out.println("doing random movement");
        if (tryMove(randomDirection())) {
            turnCount += 1;
        } else {
            // if it cannot move in this direction, try to move in another random direction
        }
        getFlagFromMom();
        Clock.yield();
    }

    // For Politician bot to move to ECenter
    void movePoliticianToECenter() throws Exception {
        // listen to mom and move to the target ECenter
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
