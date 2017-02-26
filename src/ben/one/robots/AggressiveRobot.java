package ben.one.robots;

import battlecode.common.RobotController;

abstract class AggressiveRobot<S extends RobotState<S>> extends Robot<S> {
    AggressiveRobot(RobotController rc) {
        super(rc);
    }
}
