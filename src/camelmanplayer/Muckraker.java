package camelmanplayer;

import battlecode.common.*;

public class Muckraker extends RobotPlayer{

    Muckraker(RobotController rc) {
        this.rc = rc;
    }

    void runMuckraker() throws GameActionException {


        // movements by stages
        // TODO: change the stages
        while (true) {
            turnCount += 1;
            if (turnCount <= 300) {
                scanMap();
            } else {
                // do random movements
                if (tryMove(randomDirection()))
                    System.out.println("I moved!");
                Clock.yield();
            }
        }
    }
}
