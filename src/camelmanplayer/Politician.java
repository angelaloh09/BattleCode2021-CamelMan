package camelmanplayer;

import battlecode.common.*;

public class Politician extends RobotPlayer{

    Politician(RobotController rc) {
        this.rc = rc;
    }

    void runPolitician() throws GameActionException {
        // try move
        Team enemy = rc.getTeam().opponent();
        int actionRadius = rc.getType().actionRadiusSquared;
        RobotInfo[] attackable = rc.senseNearbyRobots(actionRadius, enemy);
        if (attackable.length != 0 && rc.canEmpower(actionRadius)) {
            System.out.println("empowering...");
            rc.empower(actionRadius);
            System.out.println("empowered");
            return;
        }

        // movements by stages
        while (true) {
            turnCount += 1;
            if (turnCount <= 300) {
                scanMap();
            } else {
                if (tryMove(randomDirection()))
                    System.out.println("I moved!");
                Clock.yield();
            }
        }
    }
}
