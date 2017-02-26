package ben.one.comms;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

class DescendMsg extends LocationBasedMsg {
    DescendMsg(float x, float y) {
        super(x, y, MessageType.DESCEND);
    }

    static DescendMsg readMessage(RobotController rc, int pos) throws GameActionException {
        return new DescendMsg(rc.readBroadcastFloat(pos + 1), rc.readBroadcastFloat(pos + 2));
    }
}
