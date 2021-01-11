package camelmanplayer;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public class Slanderer extends RobotPlayer{

    Slanderer(RobotController rc) {
        this.rc = rc;
    }

    void runSlanderer() throws GameActionException {
        // general principle

        // movements by stages
        if (tryMove(randomDirection()))
            System.out.println("I moved!");
    }
}
