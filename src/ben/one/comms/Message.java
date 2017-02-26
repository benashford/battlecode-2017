package ben.one.comms;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

abstract class Message {
    /**
     * Every message is this size.
     */
    static final int MESSAGE_SIZE = 3;

    abstract void writeMessage(int hwm, RobotController rc) throws GameActionException;
}
