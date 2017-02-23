package ben.one;

import battlecode.common.*;

public class Gardener extends Robot {
    private static final int TREE_FREQ = 10;

    private int turnCounter = 0;

    Gardener(RobotController rc) {
        super(rc);
    }

    void run() throws GameActionException {
        while (true) {
            Direction d = randomDirection();
            if (turnCounter % TREE_FREQ == 0 && rc.canPlantTree(d)) {
                rc.plantTree(d);
            } else {
                if (rc.canBuildRobot(RobotType.SOLDIER, d)) {
                    rc.buildRobot(RobotType.SOLDIER, d);
                }
            }
            d = randomDirection();
            if (rc.canMove(d)) {
                rc.move(d);
            }
            turnCounter++;
            Clock.yield();
        }
    }
}
