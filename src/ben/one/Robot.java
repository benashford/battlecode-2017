package ben.one;

import battlecode.common.Direction;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.Team;

abstract class Robot {
    RobotController rc;
    private Team enemy;

    Robot(RobotController rc) {
        this.rc = rc;
        this.enemy = rc.getTeam().opponent();
    }

    /**
     * Don't stand still...
     */
    static Direction randomDirection() {
        return new Direction((float)Math.random() * 2 * (float)Math.PI);
    }

    /**
     * Sensable opponents
     */
    RobotInfo[] senseEnemies() {
        return rc.senseNearbyRobots(-1, enemy);
    }
}
