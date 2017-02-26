package ben.one.robots;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import ben.one.Awareness;

abstract class PassiveRobot<S extends RobotState<S>> extends Robot<S> {
    PassiveRobot(RobotController rc) {
        super(rc);
    }

    final void doTurn(Awareness awareness) throws GameActionException {
        if (awareness.isBullets()) {
            evadeBullets(awareness);
        } else if (awareness.isEnemy()) {
            randomMovement();
        } else {
            state = state.act(awareness);
        }
    }
}
