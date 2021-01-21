package camelmanplayer;

import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class Slanderer extends RobotPlayer{

    Slanderer(RobotController rc) {
        this.rc = rc;
        loadMotherInfo();
    }

    // TODO: control it as a politician later
    void runSlanderer() throws Exception {
        while (true) {
            warPhase = nextPhase;
            if (rc.getType() == RobotType.SLANDERER) {
                if (tryMove(randomDirection())) {
                    System.out.println("I moved!");
                    getFlagFromMom();
                }
                Clock.yield();
                turnCount++;
            } else {
                switch (warPhase) {
                    case SEARCH:
                        System.out.println("I am scanning!");
                        scanMap();
                        break;
                    case CONQUER:
                        System.out.println("I am conquering!");
                        movePoliticianToECenter();
                        break;
                    case ATTACK:
                        System.out.println("I am attacking!");
                        movePoliticianToECenter();
                        break;
                    case DEFEND:
                        System.out.println("I am defending!");
                        movePoliticianToECenter();
                        break;
                    default:
                        System.out.println("I am moving randomly!");
                        getFlagFromMom();
                        randomMovement();
                        break;
                }
            }
        }
    }
}
