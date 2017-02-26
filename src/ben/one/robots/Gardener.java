package ben.one.robots;

import battlecode.common.*;
import ben.one.Awareness;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class Gardener extends PassiveRobot {
    private static final int NUM_TREES = 4;

    private Deque<RobotType> buildStack = new ArrayDeque<>();

    public Gardener(RobotController rc) {
        super(rc);
        state = new Garden(NUM_TREES);
    }

    private void plantRandomTree() throws GameActionException {
        Direction d = randomDirection();
        if (rc.canPlantTree(d)) {
            rc.plantTree(d);
        }
    }

    private RobotType nextBuildType() {
        if (buildStack.isEmpty()) {
            return RobotType.SOLDIER;
        } else {
            return buildStack.peekLast();
        }
    }

    private void build() throws GameActionException {
        Direction d = randomDirection();
        RobotType type = nextBuildType();
        if (rc.canBuildRobot(type, d)) {
            rc.buildRobot(type, d);
            if (!buildStack.isEmpty()) {
                buildStack.removeLast();
            }
        }
    }

    private class Garden implements GardenerState {
        private int numTrees;

        private Garden(int numTrees) {
            this.numTrees = numTrees;
        }

        @Override
        public GardenerState act(Awareness awareness) throws GameActionException {
            List<TreeInfo> trees = awareness.findTeamTrees();

            if (!trees.isEmpty()) {
                TreeInfo poorestTree = trees.get(0);
                float poorestTreeHealth = poorestTree.getHealth() / poorestTree.getMaxHealth();
                int numOfNearbyTrees = trees.size();
                for (int i = 1; i < numOfNearbyTrees; i++) {
                    TreeInfo tree = trees.get(i);
                    float treeHealth = tree.getHealth() / tree.getMaxHealth();
                    if (treeHealth < poorestTreeHealth) {
                        poorestTree = tree;
                        poorestTreeHealth = treeHealth;
                    }
                }
                if (poorestTreeHealth < 0.75f) {
                    int poorestTreeId = poorestTree.getID();
                    if (rc.canInteractWithTree(poorestTreeId)) {
                        if (rc.canWater(poorestTreeId)) {
                            rc.water(poorestTreeId);
                        }
                    } else {
                        MapLocation poorestTreeLocation = poorestTree.getLocation();
                        if (rc.canMove(poorestTreeLocation)) {
                            rc.move(poorestTreeLocation);
                        }
                    }
                }
            }

            if (trees.size() < numTrees) {
                plantRandomTree();
            } else {
                build();
            }
            if (!rc.hasMoved()) {
                randomMovement();
            }
            return this;
        }
    }
}

interface GardenerState extends RobotState<GardenerState> {

}
