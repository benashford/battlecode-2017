package ben.one.robots;

import battlecode.common.*;
import ben.one.Awareness;

abstract class Robot {
    RobotController rc;
    private Team enemy;

    Robot(RobotController rc) {
        this.rc = rc;
        this.enemy = rc.getTeam().opponent();
    }

    abstract void doTurn(Awareness awareness) throws GameActionException;

    public final void run() throws GameActionException {
        while (true) {
            Awareness awareness = new Awareness(rc);
            doTurn(awareness);
            Clock.yield();
        }
    }

    /**
     * Don't stand still...
     */
    static Direction randomDirection() {
        return new Direction((float)Math.random() * 2 * (float)Math.PI);
    }

    void randomMovement() throws GameActionException {
        Direction d = randomDirection();
        if (rc.canMove(d)) {
            rc.move(d);
        }
    }

    /**
     * Sensable opponents
     *
     * @deprecated
     *
     * TODO: remove this
     */
    RobotInfo[] senseEnemies() {
        return rc.senseNearbyRobots(-1, enemy);
    }
}
