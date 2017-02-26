package ben.one.comms;

import battlecode.common.GameActionException;

import java.util.List;

import static ben.one.Util.debug_outf;

public class ArchonMessageBroker {
    private ShipToShore radio;
    private ShoreToShip orders;

    public ArchonMessageBroker(ShipToShore radio, ShoreToShip orders) {
        this.radio = radio;
        this.orders = orders;
    }

    public void brokerMessages() throws GameActionException {
        List<Message> newMessages = radio.readMessages();
        debug_outf("%d new messages", newMessages.size());
        // TODO - intelligently choose which messages to forward, but for now...

        if (newMessages.isEmpty()) {
            return;
        }

        LocationBasedMsg message = (LocationBasedMsg) newMessages.iterator().next();
        orders.broadcastMessage(message.makeDescendMsg());
    }
}
