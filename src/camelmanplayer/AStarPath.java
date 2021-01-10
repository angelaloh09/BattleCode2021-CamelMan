package camelmanplayer;

import battlecode.common.*;

import java.util.*;

public class AStarPath {

    static RobotController rc;
    static HashMap<MapLocation, LocationInfo> travelCostMap;
    static MapLocation destination;

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

    /**
     * Returns the shortest path from the current location to the destination.
     * If the destination is out of the sensing range of the robot, it returns a path to the edge of the robot's
     * sensing range with the lowest estimated distance to the destination.
     *
     * Implemented with reference of the tutorial: https://www.youtube.com/watch?v=-L-WgKMFuhE
     * **/
    static LinkedList<MapLocation> aStarPlanning(RobotController rc, MapLocation destination) throws Exception {
        AStarPath.rc = rc;
        travelCostMap = new HashMap<>();
        AStarPath.destination = destination;

        // Create empty open and closed sets.
        Set<MapLocation> open = new HashSet<>();
        Set<MapLocation> closed = new HashSet<>();

        // Add the initial location to the open set and travelCostMap
        MapLocation initLocation = rc.getLocation();
        LocationInfo initLocationInfo = new LocationInfo(initLocation, null);
        travelCostMap.put(initLocation, initLocationInfo);
        open.add(initLocation);

        MapLocation current;
        while (true) {
            // Choose the location of the lowest f_cost in the open set as current
            current = findLocationOfLowestCost(open, travelCostMap);
            open.remove(current);
            closed.add(current);

            // If current is the destination or a MapLocaiton at the edge of the robot's sensing range, break the loop.
            if (foundPath(current, initLocation)) {
                break;
            }
            branchOut(current, open, closed);
        }

        return generatePath(current);
    }

    static boolean foundPath(MapLocation current, MapLocation initLocation) {
        if (current == destination) return true;
        int maxSquaredDistance;
        switch (rc.getType()) {
            case POLITICIAN: maxSquaredDistance = 20; break;
            case SLANDERER: maxSquaredDistance = 15; break;
            case MUCKRAKER: maxSquaredDistance = 35; break;
            default: maxSquaredDistance = 0;
        }
        return !current.isWithinDistanceSquared(initLocation, maxSquaredDistance);
    }

    static LinkedList<MapLocation> generatePath(MapLocation end) {
        LinkedList<MapLocation> path = new LinkedList<>();

        MapLocation location = end;

        while (location != null) {
            path.add(location);
            location = travelCostMap.get(location).parentLocation;
        }

        return path;
    }


    static void branchOut(MapLocation location, Set<MapLocation> open, Set<MapLocation> closed) throws GameActionException {
        for (Direction direction: directions) {
            MapLocation adjacentLoc = rc.adjacentLocation(direction);

            if (adjacentLoc != null && !closed.contains(adjacentLoc)) {
                LocationInfo adjacentLocInfo = new LocationInfo(adjacentLoc, location);

                LocationInfo oldAdjacentLocInfo = travelCostMap.get(adjacentLoc);
                if (oldAdjacentLocInfo == null) {
                    travelCostMap.put(adjacentLoc, adjacentLocInfo);
                    open.add(adjacentLoc);
                } else if (oldAdjacentLocInfo.getCost() > adjacentLocInfo.getCost()) {
                    travelCostMap.put(adjacentLoc, adjacentLocInfo);
                }
            }
        }
    }

    static MapLocation findLocationOfLowestCost(Set<MapLocation> open, HashMap<MapLocation, LocationInfo> travelCostMap)
            throws Exception {
        int lowestCost = Integer.MAX_VALUE;
        MapLocation result = null;
        for (MapLocation location: open) {
            int fCost = travelCostMap.get(location).getCost();
            if (fCost < lowestCost) {
                lowestCost = fCost;
                result = location;
            }
        }

        if (result == null) throw new Exception("Empty open set");
        return result;
    }

    private static class LocationInfo {
        private final int gCost;
        private final int hCost;
        private final MapLocation parentLocation;

        private LocationInfo(MapLocation location, MapLocation parent) throws GameActionException {
            double passability = rc.sensePassability(location);

            // Some sketch algorithm to convert passability to distance
            if (parent == null) {
                gCost = (int) (0.5 / passability)^2;
            } else {
                gCost = (int) (0.5 / passability)^2 + travelCostMap.get(parent).gCost;
            }

            hCost = location.distanceSquaredTo(destination);
            parentLocation = parent;
        }

        private int getCost(){
            return gCost + hCost;
        }
    }
}
