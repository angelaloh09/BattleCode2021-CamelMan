package camelmanplayer;

import battlecode.common.*;

public class Politician extends RobotPlayer{

    Politician(RobotController rc) {
        this.rc = rc;
    }

    void runPolitician() throws Exception {

        // movements by stages
        while (true) {
            switch (warPhase) {
                case SEARCH:
                    scanMap();
                    break;
                case CONQUER:
                case ATTACK:
                case DEFEND:
                    goToECenter();
                    break;
                default:
                    randomMovement();
                    break;
            }
        }
    }
}
