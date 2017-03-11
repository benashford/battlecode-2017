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
        float angle = offset + (((float)Math.PI / 3f) * idx + 0.0f);
        return rc.getLocation().add(angle, rc.getType().bodyRadius + 0.01f + BULLET_TREE_RADIUS);
    }

    private class FindSpace extends PassiveRobotState {
        private float offset = (float)(Math.random() * 2 * Math.PI);
        private float direction = (float)(Math.random() * 2 * Math.PI);

        private boolean isInSpace() throws GameActionException {
            boolean isSpace = true;
            for (int i = 0; i < 6; i++) {
                MapLocation loc = locationAtPosition(offset, i);
                if (rc.isCircleOccupied(loc, BULLET_TREE_RADIUS)) {
                    debug_spot(loc, 255, 0, 0);
                    isSpace = false;
                } else {
                    if (rc.isBuildReady()) {
                        build(rc.getLocation().directionTo(loc));
                    }
                    debug_spot(loc, 127, 255, 63);
                }
            }
            return isSpace;
        }

        public RobotState act(Awareness awareness) throws GameActionException {
            if (isInSpace() && awareness.findFriendTrees().isEmpty()) {
                debug_outf("Found space, beginning to garden");
                return new Garden(offset);
            } else {
                if (!rc.hasMoved()) {
                    MapLocation loc = rc.getLocation().add(direction);
                    if (rc.canMove(loc)) {
                        rc.move(loc);
                    } else {
                        direction = (float) (Math.random() * 2 * Math.PI);
                    }
                }
                offset += (float)(Math.random() * (Math.PI / 6));
            }
            return this;
        }
    }

    private class Garden extends PassiveRobotState {
        private float offset;
        private int rota = 0;

        Garden(float offset) {
            this.offset = offset;
        }

        public RobotState act(Awareness awareness) throws GameActionException {
            int trees = awareness.findFriendTrees().size();
            for (int i = 0; i < 6; i++) {
                MapLocation loc = locationAtPosition(offset, (i + rota) % 6);
                if (rc.canInteractWithTree(loc)) {
                    if (rc.canWater(loc)) {
                        debug_spot(loc, 0, 0, 255);
                        rc.water(loc);
                    }
                }
                if (rc.isCircleOccupied(loc, BULLET_TREE_RADIUS)) {
                    continue;
                }
                Direction d = rc.getLocation().directionTo(loc);
                if (trees < 5 && rc.getTeamBullets() < 800) {
                    if (rc.canPlantTree(d)) {
                        rc.plantTree(d);
                    }
                    break;
                }
                if (build(d)) {
                    break;
                }
            }
            rota += 1;
            return this;
        }
    }
}
