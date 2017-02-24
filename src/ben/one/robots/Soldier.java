package ben.one.robots;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import ben.one.Awareness;

public class Soldier extends Robot {
    public Soldier(RobotController rc) {
        super(rc);
    }

    void doTurn(Awareness awareness) throws GameActionException {
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
    }
}
