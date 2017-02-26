package ben.one.comms;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

class EnemyMsg extends LocationBasedMsg {
    EnemyMsg(float x, float y) {
        super(x, y, MessageType.ENEMY);
    }

    static EnemyMsg readMessage(RobotController rc, int pos) throws GameActionException {
        return new EnemyMsg(rc.readBroadcastFloat(pos + 1), rc.readBroadcastFloat(pos + 2));
    }
}
