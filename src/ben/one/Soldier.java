package ben.one;

import battlecode.common.*;

public class Soldier extends Robot {
    Soldier(RobotController rc) {
        super(rc);
    }

    void run() throws GameActionException {
        while (true) {
            RobotInfo[] enemies = senseEnemies();
            Direction d = randomDirection();
            if (enemies.length == 0) {
                if (rc.canMove(d)) {
                    rc.move(d);
                }
            } else {
                Direction toEnemy = rc.getLocation().directionTo(enemies[0].location);

                if (rc.canFireSingleShot()) {
                    rc.fireSingleShot(toEnemy);
                }

                Direction awayFromEnemy = toEnemy.opposite();
                if (rc.canMove(awayFromEnemy)) {
                    rc.move(awayFromEnemy);
                } else {
                    if (rc.canMove(d)) {
                        rc.move(d);
                    }
                }
            }
            Clock.yield();
        }
    }
}
