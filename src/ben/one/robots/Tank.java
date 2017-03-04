package ben.one.robots;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

import java.util.HashMap;
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
    }
}
