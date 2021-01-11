package camelmanplayer;

import battlecode.common.*;

public class Muckraker extends RobotPlayer{

    Muckraker(RobotController rc) {
        this.rc = rc;
    }

    void runMuckraker() throws GameActionException {
        // universal principles
        Team enemy = rc.getTeam().opponent();
        int actionRadius = rc.getType().actionRadiusSquared;
        for (RobotInfo robot : rc.senseNearbyRobots(actionRadius, enemy)) {
            if (robot.type.canBeExposed()) {
                // It's a slanderer... go get them!
                if (rc.canExpose(robot.location)) {
                    System.out.println("e x p o s e d");
                    rc.expose(robot.location);
                    return;
                }
            }
        }


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
