package camelmanplayer;

import battlecode.common.*;

public class Muckraker extends RobotPlayer {

    Muckraker(RobotController rc) {
        this.rc = rc;
        loadMotherInfo();
    }

    void runMuckraker() throws Exception {

        // movements by stages
        while (true) {
            warPhase = nextPhase;
            switch (warPhase) {
                case SEARCH:
                    scanMapFast();
                    System.out.println("I am searching!");
                    break;
                case CONQUER:
                    mUniversalPrinciple();
                    randomMovement();
                    System.out.println("I am conquering!");
                    break;
                case ATTACK:
                    attackEnemyCenter();
                    System.out.println("I am attacking!");
                    break;
                case DEFEND:
                    getFlagFromMom();
                    mUniversalPrinciple();
                    System.out.println("I am defending!");
                    break;
                default:
                    getFlagFromMom();
                    randomMovement();
                    break;
            }
        }
    }

    void attackEnemyCenter() throws Exception{
        int actionRS = rc.getType().actionRadiusSquared;
        System.out.println("The target Enlightenment center is"+targetECenter);
        moveToDestination(targetECenter, actionRS);
        randomMovement();
    }
}

