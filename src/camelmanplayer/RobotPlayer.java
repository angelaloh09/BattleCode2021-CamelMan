package camelmanplayer;

import battlecode.common.*;

import java.util.*;

// TODO:
// 1) Slanderer diagonol positioning: SMART RUYU
// 2) Muckracker 8 searchers + no zigzag for remaining searches: BECKY
// 3) Politician Swarm attack: ANGELA
// 4) elegant bidding solution --> a) scale according to the mother enlightenment center b) other options...


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
        ECENTERTOCHILD,
        ECENTERTOECENTER,
        WALL,
        CORNER,

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
                System.out.println("I found an enlightenment center: "+loc);
                Message msg = new Message(MessageType.ECENTERTOCHILD, WarPhase.SEARCH, rInfo.getTeam(), rInfo.getLocation(), motherLoc);
                int flag = FlagProtocol.encode(msg);
                if (rc.canSetFlag(flag)) {
                    System.out.println("I set the flag!");
                    rc.setFlag(flag);
                }
            }
        }
    }

    /** whenever a robot is moved, call this function */
    void terminateRound() throws GameActionException {
        getFlagFromMom();
        turnCount += 1;
        Clock.yield();
    }

    boolean tryMoveWithCatch(Direction dir) throws Exception {
        applyUP();
        // for each step, scan the surrounding first
        // TODO: make sure this function is called every round
        senseNewEC();

//        Team team = rc.getTeam();
//        MapLocation currLoc = rc.getLocation();

        try {
            rc.move(dir);
            // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
            return true;
        } catch (GameActionException cannotMove) {
            System.out.println("oops, cannot move");
            if (rc.getCooldownTurns() >= 1) {
                System.out.println("cool down turns");
                return true;
            }
            return false;

//            MapLocation adjacentLoc = rc.adjacentLocation(dir);
//
//            // check if at least 5 of the adjacent nodes are not on the map
//            boolean corner = false;
//            int boundCounter = 0;
//            for (Direction direction: directions){
//                if (!rc.onTheMap(rc.adjacentLocation(direction))){
//                    boundCounter ++;
//                }
//                if (boundCounter == 5){
//                    corner = true;
//                }
//            }
//            // case 1: Scout in corner
//            if (corner){
//                setMessageFlag(MessageType.CORNER, WarPhase.SEARCH, team, currLoc);
//                Direction random_dir = directions[(int) (directions.length * Math.random())];
//                tryMoveWithCatch(random_dir);
//            }
//            // case 2: Scout bumps into wall
//            else if (!rc.onTheMap(adjacentLoc)) {
//                System.out.println("oops, just bumped into the wall");
//                setMessageFlag(MessageType.WALL, WarPhase.SEARCH, team, currLoc);
//                Direction random_dir = directions[(int) (directions.length * Math.random())];
//                tryMoveWithCatch(random_dir);
//            }
//            // case 3: location is occupied
//            else if (rc.isLocationOccupied(adjacentLoc)) {
//                System.out.println("the adjacent location is occupied ;-;");
//                String msg = "";
//                while (msg != "moved") {
//                    Direction random_dir = directions[(int) (directions.length * Math.random())];
//                    msg = tryMoveWithCatch(random_dir);
//                }
//                return "didRandomMove";
//            } else {
//                System.out.println("cool-down turns:" + rc.getCooldownTurns());
//            }
//            getFlagFromMom();
//            turnCount += 1;
//            Clock.yield();
//            return "bye path planning";
        }
    }

    // search phase
    /** keep the bot moving when it hasn't reached the destination
     while scanning the surrounding for new ECenter */
//    void moveToDestination (MapLocation destination, int squaredDis) throws Exception {
//        while (rc.getLocation().distanceSquaredTo(destination) > squaredDis) {
//            // a list of locations to go to the location closest to the destination
//            LinkedList<MapLocation> locs = AStarPath.aStarPlanning(rc, destination);
//            while (!locs.isEmpty()) {
//                // if we haven't reached the closest location, keep going
//                MapLocation tail = locs.getLast();
//                Direction nextDir = rc.getLocation().directionTo(tail);
//                String moveMsg = tryMoveWithCatch(nextDir);
//                if (moveMsg == "moved") {
//                    // if we've moved to the first node successfully, remove the first node
//                    locs.removeLast();
//                }
//                if (moveMsg == "didRandomMove") {
//                    locs = AStarPath.aStarPlanning(rc, destination);
//                }
//                if (nextPhase != warPhase) return;
//            }
//        }
//    }

    void moveToDestination (MapLocation destination, int squaredDis) throws Exception {
        int count = 0;
        System.out.println("I am going to: "+destination);
        while (rc.getLocation().distanceSquaredTo(destination) > squaredDis) {
            applyUP();
            senseNewEC();
//            getFlagFromMom();
            Direction dir = rc.getLocation().directionTo(destination);
            if (!tryMoveWithCatch(dir)) {
                count++;
            }
            if (count > 5) {
                randomMovement();
                count = 0;
            }
            if (nextPhase != warPhase) return;
            terminateRound();
        }
        System.out.println("I arrived at: "+destination);
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
                nextPhase = motherMsg.warPhase;
                enlightenmentCenters.put(targetECenter, motherMsg.team);
                System.out.println("Mother told me: "+warPhase+" with target "+targetECenter);
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
        if (nextPhase != warPhase) return;

        // get info from mom to see if we need to keep scanning
        getFlagFromMom();

        while (warPhase == WarPhase.SEARCH) {
            // first move right
            scanMoveRight(i, lastMainAxisLoc);

            // then move left
            scanMoveLeft(i, ECenterLoc);
            if (nextPhase != warPhase) return;
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
    void scanMapSlow() {
        // initialize the movement by getting the E-center coordinates
        // main direction of scanning
        Direction direction;
        MapLocation startLoc = rc.getLocation();
        direction = motherLoc.directionTo(startLoc);

        try {
            // first move one step forward in this direction
            int movedSteps = 0;
            while (movedSteps < 3) {
//                if (tryMoveWithCatch(direction).equals("moved")) movedSteps++;
                if (tryMoveWithCatch(direction)) movedSteps++;
                System.out.println("I am in the while loop QWQ");
                System.out.println("moveSteps: "+movedSteps);
                if (nextPhase != warPhase) return;
            }

            // current location
            MapLocation currLoc = rc.getLocation();
            int currLocx = currLoc.x;
            int currLocy = currLoc.y;

            // the distance travelled from the starting point of the scan (E-Center)
            double fstTravelDist = Math.sqrt((currLocx - startLoc.x) ^ 2 + (currLocy - startLoc.y) ^ 2);

            int i = Arrays.asList(directions).indexOf(direction);
            scanMoveZigzag(fstTravelDist, i, rc.getLocation(), startLoc);
            if (nextPhase != warPhase) return;

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    Direction newDirection() throws Exception {
        // if in cool down, stay still
        while (rc.getCooldownTurns() >= 1) {
            terminateRound();
        }

        // put all the passability of next two
        // tiles in all moveable direction in an array
        ArrayList<Double> moveable = new ArrayList<>();
        for (int i = 0; i < directions.length; i ++) {
            Direction dir = directions[i];
            if (rc.canMove(dir)) {
                MapLocation adjacent1 = rc.adjacentLocation(dir);
                MapLocation adjacent2 = adjacent1.add(dir);
                double passability1 = rc.sensePassability(adjacent1);
                double passability2 = 0.0;
                if (rc.onTheMap(adjacent2)) {
                    passability2 = rc.sensePassability(adjacent2);
                }
                double nextTwoPass = passability1 + passability2;
                moveable.add(nextTwoPass);
            }
        }

        // if all neighbors are occupied stay still
        while (moveable.size() == 0) terminateRound();

        // create an array of directions that have the biggest passability
        ArrayList<Direction> bestDirs = new ArrayList<>();
        double maxPass = -1.0;
        for (int i = 0; i < moveable.size(); i ++) {
            if (moveable.get(i) > maxPass) {
                maxPass = moveable.get(i);
                bestDirs = new ArrayList<>();
                bestDirs.add(directions[i]);
            }
            if (moveable.get(i) == maxPass) bestDirs.add(directions[i]);
        }

        // randomly generate one direction among bestDirs
        int numBestDir = bestDirs.size();
        System.out.println("numbestdir" + numBestDir);
        int dirIdx = (int) Math.floor(numBestDir * Math.random());
        System.out.println("next index" + dirIdx);
        return bestDirs.get(dirIdx);

    }

    void scanMapFast() {

        try {
            Direction direction;
            MapLocation startLoc = rc.getLocation();
            direction = motherLoc.directionTo(startLoc);

            while (nextPhase == warPhase) {
                int steps = 0;
                // at most move 10 steps in one direction
                while (steps < 10) {
                    // TODO: consider tile color
                    boolean moved = tryMoveWithCatch(direction);
                    // if moved successfully, keep going
                    // if something is blocking the way (wall/robot), change direction
                    if (moved) {
                        System.out.println("moved, step++");
                        steps++;
                        terminateRound();
                    }
                    else steps = 10;

                }
                direction = newDirection();
            }
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

    void randomMovement() throws Exception {
        System.out.println("doing random movement");
        Direction dir = newDirection();
        tryMoveWithCatch(dir);
        terminateRound();
    }

    // For Politician bot to move to ECenter
    void movePoliticianToECenter() throws Exception {
        // listen to mom and move to the target ECenter
        int actionRS = rc.getType().actionRadiusSquared;
        moveToDestination(targetECenter, actionRS);

        if (rc.canEmpower(actionRS)) {
            // empower and die gloriously
            rc.empower(actionRS);
            terminateRound();
        } else {
            // move randomly like a soldier who doesn't know the meaning of his life
            randomMovement();
        }
    }


    /**
     * Main function for moving the slanderer to a parking spot,
     * i.e. diagonal lines around the e-center
     */
    public void parkRobot(MapLocation eCenterLoc) throws GameActionException {

        // According to the direction that the slanderer was built, get an array of general directions

        MapLocation robotLoc = rc.getLocation();
        Direction initDirection = eCenterLoc.directionTo(robotLoc);
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
                if (isParkingLot(rc.getLocation(), eCenterLoc)) return;

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
            if (isParkingLot(targetLoc, motherLoc)) {
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
    private boolean isParkingLot(MapLocation targetLocation, MapLocation eCenterLoc) {

        // Is it on the diagonal?
        int diff = targetLocation.x + targetLocation.y - eCenterLoc.x - eCenterLoc.y;
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





