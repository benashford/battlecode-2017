package ben.one.robots;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import ben.one.Awareness;

abstract class PassiveRobot extends Robot {
    PassiveRobot(RobotController rc) {
        super(rc);
    }

    void doTurn(Awareness awareness) throws GameActionException {
        if (awareness.isBullets()) {
            evadeBullets(awareness);
        } else if (awareness.isEnemy()) {
            broadcastEnemies(awareness.findEnemy());
            // TODO - runAway();
            randomMovement();
        } else {
            state = state.act(awareness);
        }
    }
}
