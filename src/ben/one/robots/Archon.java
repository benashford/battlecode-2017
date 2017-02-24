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
        if (awareness.isDanger()) {
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

    private class Roamer implements ArchonState {
        @Override
        public ArchonState act(Awareness awareness) throws GameActionException {
            TreeInfo tree = awareness.findNearestTreeWithBullets();
            if (tree != null) {
                if (rc.canInteractWithTree(tree.getID())) {
                    rc.shake(tree.getID());
                    return this;
                } else {
                    if (rc.canMove(tree.getLocation())) {
                        rc.move(tree.getLocation());
                        return this;
                    }
                }
            }
            randomMovement();
            return this;
        }
    }
}

interface ArchonState extends RobotState<ArchonState> {

}

