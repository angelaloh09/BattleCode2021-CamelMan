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
            warPhase = nextPhase;
            switch (warPhase) {
                case SEARCH:
                    System.out.println("I am at turn number: "+turnCount);
                    if (turnCount < 60) {
                        randomMoveAroundMother();
                        pUniversalPrinciple();
                        turnCount++;
                    } else {
                        System.out.println("I am scanning!");
                        scanMapFast();
                    }
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
