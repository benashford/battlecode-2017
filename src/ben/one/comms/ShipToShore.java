package ben.one.comms;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

import static ben.one.Util.debug_outf;

public class ShipToShore extends Radio {
    private static final int ALLOCATION_START = 1000;
    private static final int MAX_MESSAGES = 100;

    public ShipToShore(RobotController rc) {
        super(rc, ALLOCATION_START, MAX_MESSAGES);
    }

    public void broadcastBullet(MapLocation location) throws GameActionException {
        broadcastMessage(new BulletMsg(location.x, location.y));
    }

    public void broadcastEnemy(MapLocation location) throws GameActionException {
        broadcastMessage(new EnemyMsg(location.x, location.y));
    }

    Message readMessage(int pos) throws GameActionException {
        int controlCode = rc.readBroadcast(pos);
        if (controlCode == MessageType.BULLET.ordinal()) {
            return BulletMsg.readMessage(rc, pos);
        } else if (controlCode == MessageType.ENEMY.ordinal()) {
            return EnemyMsg.readMessage(rc, pos);
        } else {
            debug_outf("Unknown control code: %d", controlCode);
            return null;
        }
    }
}
