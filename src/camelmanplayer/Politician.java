package camelmanplayer;

import battlecode.common.*;

public class Politician extends RobotPlayer{

    Politician(RobotController rc) {
        this.rc = rc;
        loadMotherInfo();
    }

    void runPolitician() throws Exception {

        // movements by stages
        while (true) {
            switch (warPhase) {
                case SEARCH:
                    System.out.println("I am scanning!");
                    randomScan();
                    break;
                case CONQUER:
                    System.out.println("I am conquering!");
                    goToECenter();
                    break;
                case ATTACK:
                    System.out.println("I am attacking!");
                    goToECenter();
                    break;
                case DEFEND:
                    System.out.println("I am defending!");
                    goToECenter();
                    break;
                default:
                    System.out.println("I am moving randomly!");
                    randomMovement();
                    break;
            }
        }
    }
}
