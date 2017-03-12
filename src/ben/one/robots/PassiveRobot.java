package ben.one.robots;

import battlecode.common.RobotController;
import ben.one.Awareness;

abstract class PassiveRobot extends Robot {
    PassiveRobot(RobotController rc) {
        super(rc);
    }

    abstract class PassiveRobotState extends RobotState {
        /**
         * All passive states only work in peacetime
         */
        public RobotState interrupt(Awareness awareness) {
            if (awareness.isDangerousBullets()) {
                return new Evade(this);
            }
            if (awareness.isEnemy()) {
                // TODO: runAway();
                return this;
            }
            return this;
        }
    }
}
