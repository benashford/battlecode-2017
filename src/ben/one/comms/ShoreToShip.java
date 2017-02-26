package ben.one.comms;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

import java.util.ArrayList;
import java.util.List;

import static ben.one.Util.debug_outf;

public class ShoreToShip extends Radio {
    private static final int ALLOCATION_START = 2000;
    private static final int MAX_MESSAGES = 10;

    public ShoreToShip(RobotController rc) {
        super(rc, ALLOCATION_START, MAX_MESSAGES);
    }

    @Override
    Message readMessage(int pos) throws GameActionException {
        int controlCode = rc.readBroadcast(pos);
        if (controlCode == MessageType.DESCEND.ordinal()) {
            return DescendMsg.readMessage(rc, pos);
        } else {
            debug_outf("Unknown control code: %d", controlCode);
            return null;
        }
    }

    public List<MapLocation> readOrders() throws GameActionException {
        List<Message> messages = readMessages();
        List<MapLocation> orders = new ArrayList<>(messages.size());
        for (Message rawMessage : messages) {
            DescendMsg message = (DescendMsg)rawMessage;
            orders.add(message.toLocation());
        }
        return orders;
    }
}
