package ben.one.robots;

import battlecode.common.RobotController;
import battlecode.common.RobotType;

import java.util.HashMap;
import java.util.Map;

public class Tank extends ShootingRobot {
    private static final Map<RobotType, Float> ATTRACTIONS = new HashMap<>();

    static {
        ATTRACTIONS.put(RobotType.ARCHON, 100f);
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
    RobotState defaultState() {
        throw new IllegalStateException("Unimplemented");
    }

    static boolean shouldBuild(int buildCount, int round, int limit) {
        return false;
        //return buildCount % 10 == 0;
    }
}
