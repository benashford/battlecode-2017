package ben.one.comms;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

class BulletMsg extends LocationBasedMsg {
    BulletMsg(float x, float y) {
        super(x, y, MessageType.BULLET);
    }

    static BulletMsg readMessage(RobotController rc, int pos) throws GameActionException {
        return new BulletMsg(rc.readBroadcastFloat(pos + 1), rc.readBroadcastFloat(pos + 2));
    }
}
