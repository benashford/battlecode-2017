package ben.one.robots;

import battlecode.common.*;
import ben.one.Awareness;
import ben.one.comms.ArchonMessageBroker;

public class Archon extends PassiveRobot {
    private static final int START_BUYING_VICTORY_POINTS = 1000;

    // TODO: revisit this, temporary bodge to force Archon's to produce as many Gardeners as possible
    // but only until they're fired upon.
    private static final int DEFAULT_GARDENERS = 2;

    private ArchonMessageBroker broker;

    public Archon(RobotController rc) {
        super(rc);
        broker = new ArchonMessageBroker(radio, orders);
    }

    RobotState defaultState() {
        return new Roamer();
    }

    @Override
    RobotState initState() {
        return new HireGardeners(DEFAULT_GARDENERS);
    }

    /**
     * Will be performed once-per-turn by all states
     */
    private void defaultActions() throws GameActionException {
        broker.brokerMessages();
        float bullets = rc.getTeamBullets();
        if (bullets > START_BUYING_VICTORY_POINTS) {
            float donation = bullets - START_BUYING_VICTORY_POINTS;
            float victoryPointCost = rc.getVictoryPointCost();
            int numPoints = (int)(donation / victoryPointCost);
            rc.donate(numPoints * victoryPointCost);
        }
    }

    private class HireGardeners extends PassiveRobotState {
        private int gardenersToHire;

        HireGardeners(int numOfGardeners) {
            this.gardenersToHire = numOfGardeners;
        }

        public RobotState act(Awareness awareness) throws GameActionException {
            Direction d2 = randomDirection();
            if (rc.canHireGardener(d2)) {
                rc.hireGardener(d2);
                gardenersToHire--;
            } else {
                defaultMovement(awareness);
            }
            defaultActions();
            if (gardenersToHire == 0) {
                return new Roamer();
            } else {
                return this;
            }
        }
    }

    private class Roamer extends PassiveRobotState {
        private int turnCount = 0;

        @Override
        public RobotState act(Awareness awareness) throws GameActionException {
            defaultMovement(awareness);
            defaultActions();
            turnCount++;
            if (turnCount < (rc.getRoundNum() / 25)) {
                return this;
            } else {
                return new HireGardeners(1);
            }
        }
    }
}

