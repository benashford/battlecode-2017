package ben.one.robots;

import battlecode.common.*;
import ben.one.Awareness;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Tank extends ShootingRobot {
    private static final Map<RobotType, Float> ATTRACTIONS = new HashMap<>();

    static {
        ATTRACTIONS.put(RobotType.ARCHON, 0f);
        ATTRACTIONS.put(RobotType.SOLDIER, 10f);
        ATTRACTIONS.put(RobotType.GARDENER, 50f);
        ATTRACTIONS.put(RobotType.LUMBERJACK, 10f);
        ATTRACTIONS.put(RobotType.SCOUT, 10f);
        ATTRACTIONS.put(RobotType.TANK, 0f);
    }

    public Tank(RobotController rc) {
        super(rc, ATTRACTIONS);
    }

    @Override
    RobotState buildRoamer() {
        return new TankRoam();
    }

    static boolean shouldBuild(int buildCount, int round, int limit) {
        //return false;
        return buildCount % 4 == 0;
    }

    class TankRoam extends Roam {
        @Override
        RobotState onBullets() {
            return this;
        }

        RobotState onOrder(MapLocation target) {
            return new TankMoveTo(target);
        }
    }

    class TankMoveTo extends MoveTo {
        TankMoveTo(MapLocation target) {
            super(target);
        }

        @Override
        RobotState onBullets() {
            return this;
        }

        /**
         * By default tanks will roll-over trees, try and avoid friendly trees where possible
         */
        @Override
        boolean canMove(Awareness awareness, Direction dir) {
            if (!super.canMove(awareness, dir)) {
                return false;
            }

            List<TreeInfo> friendlyTrees = awareness.findFriendTrees();
            if (friendlyTrees.isEmpty()) {
                return true;
            }
            MapLocation currentLocation = rc.getLocation();
            RobotType tankType = rc.getType();
            MapLocation targetLocation = currentLocation.add(dir, tankType.strideRadius);
            float tankRadius = tankType.bodyRadius;

            for (TreeInfo tree : friendlyTrees) {
                float distance = targetLocation.distanceTo(tree.getLocation());
                if (distance < tankRadius + tree.getRadius()) {
                    return false;
                }
            }
            return true;
        }
    }
}
