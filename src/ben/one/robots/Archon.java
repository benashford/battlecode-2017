package ben.one.robots;

import battlecode.common.*;
import ben.one.Awareness;
import ben.one.comms.ArchonMessageBroker;

public class Archon extends PassiveRobot {
    private static final int START_BUYING_VICTORY_POINTS = 1000;
    private static final int DEFAULT_GARDENERS = 2;

    private ArchonMessageBroker broker;

    public Archon(RobotController rc) {
        super(rc);
        state = new HireGardeners(DEFAULT_GARDENERS);
        broker = new ArchonMessageBroker(radio, orders);
    }

    @Override
    void doTurn(Awareness awareness) throws GameActionException {
        super.doTurn(awareness);
        broker.brokerMessages();
        float bullets = rc.getTeamBullets();
        if (bullets > START_BUYING_VICTORY_POINTS) {
            float donation = bullets - START_BUYING_VICTORY_POINTS;
            float victoryPointCost = rc.getVictoryPointCost();
            int numPoints = (int)(donation / victoryPointCost);
            rc.donate(numPoints * victoryPointCost);
        }
    }

    private class HireGardeners implements RobotState {
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
                randomMovement();
            }
            if (gardenersToHire == 0) {
                return new Roamer();
            } else {
                return this;
            }
        }
    }

    private class Roamer implements RobotState {
        private int turnCount = 0;

        @Override
        public RobotState act(Awareness awareness) throws GameActionException {
            defaultMovement(awareness);
            turnCount++;
            if (turnCount < (rc.getRoundNum() / 2)) {
                return this;
            } else {
                return new HireGardeners(1);
            }
        }
    }
}

