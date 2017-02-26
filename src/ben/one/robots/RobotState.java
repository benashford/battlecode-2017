package ben.one.robots;

import battlecode.common.GameActionException;
import ben.one.Awareness;

interface RobotState {
    RobotState act(Awareness awareness) throws GameActionException;
}
