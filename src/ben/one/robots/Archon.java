package ben.one.robots;

import battlecode.common.*;
import ben.one.Awareness;

public class Archon extends Robot {
    private static final int DEFAULT_GARDENERS = 2;

    private ArchonState state = new HireGardeners(DEFAULT_GARDENERS);

    public Archon(RobotController rc) {
        super(rc);
    }

    void doTurn(Awareness awareness) throws GameActionException {
        if (awareness.isBullets()) {
            evadeBullets(awareness);
        } else if (awareness.isEnemy()) {
            randomMovement();
        } else {
            state = state.act(awareness);
        }
    }

    private class HireGardeners implements ArchonState {
        private int gardenersToHire;

        HireGardeners(int numOfGardeners) {
            this.gardenersToHire = numOfGardeners;
        }

        public ArchonState act(Awareness awareness) throws GameActionException {
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

    // TODO - probably disable this
    private class Roamer implements ArchonState {
        private int turnCount = 0;

        @Override
        public ArchonState act(Awareness awareness) throws GameActionException {
            defaultMovement(awareness);
            turnCount++;
            if (turnCount < (rc.getRoundNum() * 2)) {
                return this;
            } else {
                return new HireGardeners(1);
            }
        }
    }
}

interface ArchonState extends RobotState<ArchonState> {

}

