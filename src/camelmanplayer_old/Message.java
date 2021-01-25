package camelmanplayer_old;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.Team;

import java.util.Map;

class Message {
    //TODO: include Robot ID
    Team team;
    RobotPlayer.WarPhase warPhase;
    RobotPlayer.MessageType msgType;
    int relativeX;
    int relativeY;

//    Message(RobotPlayer.WarPhase warP, Team team, int x, int y) {
//        this.team = team;
//        warPhase = warP;
//        relativeX = x;
//        relativeY = y;
//    }

//    Message(RobotPlayer.WarPhase warP, Team team) {
//        this.team = team;
//        warPhase = warP;
//        relativeX = 0;
//        relativeY = 0;
//    }

//    Message(RobotPlayer.WarPhase warP, Team team, MapLocation targetLocation, MapLocation eCenterLocation) {
//        this.team = team;
//        warPhase = warP;
//        relativeX = targetLocation.x-eCenterLocation.x;
//        relativeY = targetLocation.y-eCenterLocation.y;
//    }



    Message(RobotPlayer.MessageType msgType, RobotPlayer.WarPhase warP, Team team) {
        this.team = team;
        this.msgType = msgType;
        warPhase = warP;
        relativeX = 0;
        relativeY = 0;
    }

    // message sent when scout is in danger (includes message type)
    Message(RobotPlayer.MessageType mType, RobotPlayer.WarPhase warP, Team team, int xCoor, int yCoor){
        this.team = team;
        msgType = mType;
        warPhase = warP;
        relativeX = xCoor;
        relativeY = yCoor;
    }

    Message(RobotPlayer.MessageType mType, RobotPlayer.WarPhase warP, Team team, MapLocation currLoc,
            MapLocation eCenterLocation){
        this.team = team;
        msgType = mType;
        warPhase = warP;
        relativeX = currLoc.x - eCenterLocation.x;
        relativeY = currLoc.y - eCenterLocation.y;
    }


    MapLocation getMapLocation(MapLocation eCenterLocation){
        return new MapLocation(eCenterLocation.x+relativeX, eCenterLocation.y+relativeY);
    }
}
