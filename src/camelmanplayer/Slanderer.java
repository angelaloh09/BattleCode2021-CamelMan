package camelmanplayer;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Slanderer extends RobotPlayer {

    Slanderer(RobotController rc) {
        this.rc = rc;
        loadMotherInfo();
    }

    void runSlanderer() throws Exception {
        boolean stop = false;
        int politicianTurn = 0;
        while (true) {
            if (rc.getType() == RobotType.SLANDERER) {
                if (stop) {
                    System.out.println("I am parking.");
                    Clock.yield();
                } else {
                    parkRobot(motherLoc);
                    stop = true;
                }
            } else {
                // Move as a politician
                warPhase = nextPhase;

                if (politicianTurn < 40) {
                    randomMoveAroundMother();
                    pUniversalPrinciple();
                    politicianTurn++;
                } else {
                    switch (warPhase) {
                        case SEARCH:
                            System.out.println("I am scanning!");
                            scanMapFast();
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

}