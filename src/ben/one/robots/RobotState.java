package ben.one.robots;

import battlecode.common.GameActionException;
import ben.one.Awareness;

abstract class RobotState {
    RobotState wrappedState;

    RobotState() {
        // default not wrapping anything
    }

    RobotState(RobotState wrappedState) {
        this.wrappedState = wrappedState;
    }

    abstract RobotState interrupt(Awareness awareness) throws GameActionException;
    abstract RobotState act(Awareness awareness) throws GameActionException;

    RobotState callWrappedState(Awareness awareness) throws GameActionException {
        if (wrappedState == null) {
            return null;
        }

        return wrappedState.act(awareness);
    }
}
