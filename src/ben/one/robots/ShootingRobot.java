package ben.one.robots;

import battlecode.common.RobotController;

abstract class ShootingRobot<S extends RobotState<S>> extends AggressiveRobot<S> {
    ShootingRobot(RobotController rc) {
        super(rc);
    }
}
