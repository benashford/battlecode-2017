package ben.one.comms;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static ben.one.Util.debug_outf;

abstract class Radio {
    private final int allocationStart;
    private final int maxMessages;

    RobotController rc;

    private int readPos;

    Radio(RobotController rc, int allocationStart, int maxMessages) {
        this.rc = rc;
        this.allocationStart = allocationStart;
        this.maxMessages = maxMessages;
        this.readPos = allocationStart + 1;
    }

    private int getHighWaterMark() throws GameActionException {
        int hwm = rc.readBroadcast(allocationStart);
        if (hwm <= allocationStart) {
            return allocationStart + 1;
        } else {
            return hwm;
        }
    }

    private int wrap(int hwm) {
        if (hwm > allocationStart + 1 + maxMessages * Message.MESSAGE_SIZE) {
            return allocationStart + 1;
        } else {
            return hwm;
        }
    }

    private void setHighWaterMark(int hwm) throws GameActionException {
        rc.broadcast(allocationStart, wrap(hwm));
    }

    void broadcastMessage(Message msg) throws GameActionException {
        int hwm = getHighWaterMark();
        if (hwm >= allocationStart + 1 + (maxMessages * Message.MESSAGE_SIZE)) {
            hwm = allocationStart + 1;
        }
        debug_outf("Writing message: %s, at pos: %d", msg, hwm);
        msg.writeMessage(hwm, rc);
        setHighWaterMark(hwm + Message.MESSAGE_SIZE);
    }

    abstract Message readMessage(int pos) throws GameActionException;

    List<Message> readMessages() throws GameActionException {
        int hwm = getHighWaterMark();
        if (hwm == readPos) {
            return Collections.emptyList();
        } else {
            List<Message> messages = new ArrayList<>(maxMessages);
            for (; readPos != hwm;) {
                Message msg = readMessage(readPos);
                readPos += Message.MESSAGE_SIZE;
                if (msg != null) {
                    messages.add(msg);
                }
                readPos = wrap(readPos);
            }
            return messages;
        }
    }
}
