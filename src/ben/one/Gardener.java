package ben.one;

import battlecode.common.*;

public class Gardener extends Robot {
    Gardener(RobotController rc) {
        super(rc);
    }

    void run() throws GameActionException {
        while (true) {
            Direction d = randomDirection();
            if (rc.canBuildRobot(RobotType.SOLDIER, d)) {
                rc.buildRobot(RobotType.SOLDIER, d);
            }
            Clock.yield();
        }
    }
}
