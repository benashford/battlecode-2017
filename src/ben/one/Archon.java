package ben.one;

import battlecode.common.*;

public class Archon extends Robot {
    Archon(RobotController rc) {
        super(rc);
    }

    void run() throws GameActionException {
        while (true) {
            Direction d = randomDirection();
            if (rc.canMove(d)) {
                rc.move(d);
            }
            Direction d2 = randomDirection();
            if (rc.canBuildRobot(RobotType.GARDENER, d2)) {
                rc.buildRobot(RobotType.GARDENER, d2);
            }
            Clock.yield();
        }
    }
}
