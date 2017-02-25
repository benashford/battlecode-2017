package ben.one.robots;

import battlecode.common.*;
import ben.one.Awareness;

import java.util.List;

public class Gardener extends Robot {
    private static final int NUM_TREES = 3;

    private GardenerState state = new Garden(NUM_TREES);

    public Gardener(RobotController rc) {
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

    private void plantRandomTree() throws GameActionException {
        Direction d = randomDirection();
        if (rc.canPlantTree(d)) {
            rc.plantTree(d);
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
            if (trees.isEmpty()) {
                plantRandomTree();
                randomMovement();
            } else {
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
                Direction d = randomDirection();
                if (rc.canBuildRobot(RobotType.SOLDIER, d)) {
                    rc.buildRobot(RobotType.SOLDIER, d);
                }
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
