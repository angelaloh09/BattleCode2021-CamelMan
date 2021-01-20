package camelmanplayer;

import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public class Slanderer extends RobotPlayer{

    Slanderer(RobotController rc) {
        this.rc = rc;
    }

    // TODO: control it as a politician later
    void runSlanderer() throws GameActionException {
        while (true) {
            if (tryMove(randomDirection())) System.out.println("I moved!");
            Clock.yield();
            turnCount++;
        }
    }
}
