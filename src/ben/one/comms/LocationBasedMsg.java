package ben.one.comms;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

abstract class LocationBasedMsg extends Message {
    private float x;
    private float y;
    private MessageType type;

    LocationBasedMsg(float x, float y, MessageType type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }

    @Override
    void writeMessage(int hwm, RobotController rc) throws GameActionException {
        rc.broadcast(hwm, this.type.ordinal());
        rc.broadcastFloat(hwm + 1, this.x);
        rc.broadcastFloat(hwm + 2, this.y);
    }

    DescendMsg makeDescendMsg() {
        return new DescendMsg(x, y);
    }

    MapLocation toLocation() {
        return new MapLocation(x, y);
    }
}
