package camelmanplayer;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.Team;

import java.util.Map;

class Message {
    //TODO: include Robot ID
    Team team;
    RobotPlayer.WarPhase warPhase;
    int relativeX;
    int relativeY;

    Message(RobotPlayer.WarPhase warP, Team team, int x, int y) {
        this.team = team;
        warPhase = warP;
        relativeX = x;
        relativeY = y;
    }

    Message(RobotPlayer.WarPhase warP, Team team) {
        this.team = team;
        warPhase = warP;
        relativeX = 0;
        relativeY = 0;
    }

    Message(RobotPlayer.WarPhase warP, Team team, MapLocation targetLocation, MapLocation eCenterLocation) {
        this.team = team;
        warPhase = warP;
        relativeX = targetLocation.x-eCenterLocation.x;
        relativeY = targetLocation.y-eCenterLocation.y;
    }

    MapLocation getMapLocation(MapLocation eCenterLocation){
        return new MapLocation(eCenterLocation.x+relativeX, eCenterLocation.y+relativeY);
    }
}
