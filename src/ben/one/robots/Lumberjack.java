package ben.one.robots;

import battlecode.common.*;
import ben.one.Awareness;

import java.util.HashMap;
import java.util.Map;

public class Lumberjack extends AggressiveRobot<LumberjackState> {
    private static final Map<RobotType, Float> ATTRACTIONS = new HashMap<>();

    static {
        ATTRACTIONS.put(RobotType.ARCHON, 20f);
        ATTRACTIONS.put(RobotType.SOLDIER, -20f);
        ATTRACTIONS.put(RobotType.GARDENER, 10f);
        ATTRACTIONS.put(RobotType.LUMBERJACK, -20f);
        ATTRACTIONS.put(RobotType.SCOUT, -20f);
        ATTRACTIONS.put(RobotType.TANK, -40f);
    }

    public Lumberjack(RobotController rc) {
        super(rc, ATTRACTIONS);
        resetState();
    }

    @Override
    void resetState() {
        state = new ChopTrees();
    }

    @Override
    void attackEnemy(Awareness awareness) throws GameActionException {
        // TODO: implement me
        debug_outf("ATTACK! NOT YET IMPLEMENTED");
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
