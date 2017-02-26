package ben.one.robots;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.TreeInfo;
import ben.one.Awareness;

public class Lumberjack extends AggressiveRobot<LumberjackState> {
    public Lumberjack(RobotController rc) {
        super(rc);
        resetState();
    }

    @Override
    void doTurn(Awareness awareness) throws GameActionException {
        if (awareness.isBullets()) {
            evadeBullets(awareness);
            resetState();
        } else {
            state = state.act(awareness);
        }
    }

    private void resetState() {
        state = new ChopTrees();
    }

    private void chopTrees(Awareness awareness) throws GameActionException {
        TreeInfo tree = awareness.findNearestTreeWithRobot();
        if (tree != null) {
            if (rc.canInteractWithTree(tree.getID())) {
                rc.chop(tree.getID());
            } else {
                Direction d = rc.getLocation().directionTo(tree.getLocation());
                if (rc.canMove(d)) {
                    rc.move(d);
                }
            }
        }
        TreeInfo nearestTree = awareness.findNearestTree();
        if (nearestTree != null) {
            if (rc.canInteractWithTree(nearestTree.getID()) && !rc.hasAttacked()) {
                rc.chop(nearestTree.getID());
            }
        }
        if (!rc.hasMoved()) {
            defaultMovement(awareness);
        }
    }

    private class ChopTrees implements LumberjackState {
        @Override
        public LumberjackState act(Awareness awareness) throws GameActionException {
            chopTrees(awareness);
            return this;
        }
    }
}

interface LumberjackState extends RobotState<LumberjackState> {

}
