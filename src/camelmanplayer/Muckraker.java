package camelmanplayer;

import battlecode.common.*;

public class Muckraker extends RobotPlayer {

    Muckraker(RobotController rc) {
        this.rc = rc;
    }

    void runMuckraker() throws GameActionException {

        // movements by stages
        while (true) {
            switch (warPhase) {
                case SEARCH:
                    scanMap();
                    break;
                case CONQUER:
                case ATTACK:
                case DEFEND:
                    mUniversalPrinciple();
                    break;
                default:
                    randomMovement();
                    break;
            }
        }
    }
}

