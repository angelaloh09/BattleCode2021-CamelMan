package camelmanplayer;

import battlecode.common.*;

import javax.sound.midi.SysexMessage;
import java.awt.*;

public class Slanderer extends RobotPlayer {

    Slanderer(RobotController rc) {
        this.rc = rc;
        loadMotherInfo();
    }

    void runSlanderer() throws GameActionException {
        while (true) {
            if (rc.getType() == RobotType.SLANDERER) {
                // Move around mother

                Direction randomDir = randomDirection();
                MapLocation nextLoc = rc.adjacentLocation(randomDir);
                System.out.println("NextLoc is " + nextLoc);
                System.out.println("My mother is at " + motherLoc);
                System.out.println("Distance between them is " + nextLoc.distanceSquaredTo(motherLoc));
                System.out.println("Within: " + nextLoc.isWithinDistanceSquared(motherLoc, RobotType.SLANDERER.actionRadiusSquared));
                System.out.println("Can move: " + randomDir);
                if (nextLoc.isWithinDistanceSquared(motherLoc, RobotType.SLANDERER.sensorRadiusSquared) && rc.canMove(randomDir)) {
                    rc.move(randomDir);
                    Clock.yield();
                    turnCount++;
                }
            } else {
                // TODO: run as Politician
                if (tryMove(randomDirection())) System.out.println("I moved!");
                Clock.yield();
                turnCount++;
            }
        }
    }
}
