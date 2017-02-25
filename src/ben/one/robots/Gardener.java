package ben.one.robots;

import battlecode.common.*;
import ben.one.Awareness;

import java.util.List;
import java.util.Optional;

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

    @Override
    float getRadius() {
        return RobotType.GARDENER.bodyRadius;
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
            }
            Optional<TreeInfo> poorestTree = trees.stream().min((t1, t2) -> {
                float h1 = t1.getHealth() / t1.getMaxHealth();
                float h2 = t2.getHealth() / t2.getMaxHealth();

                if (h1 < h2) {
                    return -1;
                } else {
                    return 1;
                }
            });
            if (poorestTree.isPresent()) {
                TreeInfo pt = poorestTree.get();
                int poorestTreeId = pt.getID();
                if (rc.canInteractWithTree(poorestTreeId)) {
                    if (rc.canWater(poorestTreeId)) {
                        rc.water(poorestTreeId);
                    }
                } else {
                    MapLocation poorestTreeLocation = pt.getLocation();
                    if (rc.canMove(poorestTreeLocation)) {
                        rc.move(poorestTreeLocation);
                    }
                }
            }
            Direction d = randomDirection();
            if (trees.size() < numTrees) {
                if (rc.canPlantTree(d)) {
                    rc.plantTree(d);
                }
            } else {
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
