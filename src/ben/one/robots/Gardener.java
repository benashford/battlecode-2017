package ben.one.robots;

import battlecode.common.*;
import ben.one.Awareness;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import static battlecode.common.GameConstants.BULLET_TREE_RADIUS;
import static ben.one.Util.debug_outf;

public class Gardener extends PassiveRobot {
    private Deque<RobotType> buildStack = new ArrayDeque<>();

    private int buildCount = 0;

    public Gardener(RobotController rc) {
        super(rc);
        buildStack.add(RobotType.SOLDIER);
    }

    @Override
    RobotState initState() {
        return new FindSpace();
    }

    RobotState defaultState() {
        return new Garden(0f);
    }

    private RobotType nextBuildType() {
        if (buildStack.isEmpty()) {
            return RobotType.SOLDIER;
        } else {
            return buildStack.peekLast();
        }
    }

    private void doneBuild() {
        int turns = rc.getRoundNum();
        int limit = rc.getRoundLimit();
        buildCount++;
        if (Lumberjack.shouldBuild(buildCount, turns, limit)) {
            buildStack.add(RobotType.LUMBERJACK);
        }
        if (Tank.shouldBuild(buildCount, turns, limit)) {
            buildStack.add(RobotType.TANK);
        }
        if (Scout.shouldBuild(buildCount, turns, limit)) {
            buildStack.add(RobotType.SCOUT);
        }
    }

    private boolean build(Direction d) throws GameActionException {
        RobotType type = nextBuildType();
        if (rc.canBuildRobot(type, d)) {
            rc.buildRobot(type, d);
            if (!buildStack.isEmpty()) {
                buildStack.removeLast();
            }
            doneBuild();
            return true;
        } else {
            debug_outf("Cannot build Robot of Type: %s", type);
            return false;
        }
    }

    private final MapLocation locationAtPosition(float offset, int idx) {
        float angle = offset + (((float)Math.PI / 3f) * idx);
        return rc.getLocation().add(angle, rc.getType().bodyRadius + BULLET_TREE_RADIUS);
    }

    private class FindSpace extends PassiveRobotState {
        private float offset = 0f;

        private boolean isInSpace() throws GameActionException {
            for (int i = 0; i < 6; i++) {
                MapLocation loc = locationAtPosition(offset, i);
                if (rc.isCircleOccupied(loc, BULLET_TREE_RADIUS)) {
                    debug_spot(loc, 255, 0, 0);
                    return false;
                } else {
                    if (rc.isBuildReady()) {
                        build(rc.getLocation().directionTo(loc));
                    }
                    debug_spot(loc, 0, 255, 0);
                }
            }
            return true;
        }

        public RobotState act(Awareness awareness) throws GameActionException {
            if (isInSpace()) {
                return new Garden(offset);
            } else {
                offset += Math.random();
                // TODO: find direction
                randomMovement();
                return this;
            }
        }
    }

    private class Garden extends PassiveRobotState {
        private float offset;
        private int rota = 0;

        Garden(float offset) {
            this.offset = offset;
        }

        public RobotState act(Awareness awareness) throws GameActionException {
            List<TreeInfo> trees = awareness.findFriendTrees();

            if (!trees.isEmpty()) {
                TreeInfo poorestTree = null;
                float poorestTreeHealth = 100f;

                for (TreeInfo tree : trees) {
                    int treeId = tree.getID();
                    if (!rc.canInteractWithTree(treeId)) {
                        continue;
                    }
                    float treeHealth = tree.getHealth() / tree.getMaxHealth();
                    if (treeHealth < poorestTreeHealth) {
                        poorestTree = tree;
                        poorestTreeHealth = treeHealth;
                    }
                }

                if (poorestTreeHealth < 0.99f) {
                    int poorestTreeId = poorestTree.getID();
                    if (rc.canWater(poorestTreeId)) {
                        rc.water(poorestTreeId);
                    }
                }
            }

            for (int i = 0; i < 6; i++) {
                MapLocation loc = locationAtPosition(offset, (rota += i) % 6);
                if (rc.isCircleOccupied(loc, BULLET_TREE_RADIUS)) {
                    continue;
                }
                if (rc.canInteractWithTree(loc) && rc.canWater(loc)) {
                    debug_spot(loc, 0, 0, 255);
                    rc.water(loc);
                    break;
                }
                Direction d = rc.getLocation().directionTo(loc);
                if (trees.size() < 6 && rc.getTeamBullets() < 800) {
                    if (rc.canPlantTree(d)) {
                        rc.plantTree(d);
                    }
                    break;
                }
                if (build(d)) {
                    break;
                }
            }
            return this;
        }
    }
}
