package camelmanplayer;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public class Slanderer extends RobotPlayer{

    Slanderer(RobotController rc) {
        this.rc = rc;
    }

    static void runSlanderer() throws GameActionException {
        if (tryMove(randomDirection()))
            System.out.println("I moved!");
    }
}
