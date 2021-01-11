package camelmanplayer;

import battlecode.common.*;

public class Muckraker extends RobotPlayer {

    Muckraker(RobotController rc) {
        this.rc = rc;
    }

    void runMuckraker() throws Exception {

        // movements by stages
        while (true) {
            switch (warPhase) {
                case SEARCH:
                    scanMap();
                    break;
                case CONQUER:
                    mUniversalPrinciple();
                    break;
                case ATTACK:
                    attackEnemyCenter();
                    break;
                case DEFEND:
                    mUniversalPrinciple();
                    break;
                default:
                    randomMovement();
                    break;
            }
        }
    }

    void attackEnemyCenter() throws Exception{
        int actionRS = rc.getType().actionRadiusSquared;
        moveToDestination(targetECenter, actionRS);
        randomMovement();
    }
}

