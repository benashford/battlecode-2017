package ben.one.robots;

import battlecode.common.GameActionException;
import ben.one.Awareness;

interface RobotState<S extends RobotState> {
    S act(Awareness awareness) throws GameActionException;
}
