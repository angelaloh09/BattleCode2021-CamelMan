package camelmanplayer;

import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public class Slanderer extends RobotPlayer{

    Slanderer(RobotController rc) {
        this.rc = rc;
    }

    void runSlanderer() throws GameActionException {
        while (true) {
            warPhase = nextPhase;
            if (tryMove(randomDirection())) System.out.println("I moved!");
            Clock.yield();
            turnCount++;
        }
    }
}
