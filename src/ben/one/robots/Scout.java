package ben.one.robots;

import battlecode.common.RobotController;
import battlecode.common.RobotType;

import java.util.HashMap;
import java.util.Map;

public class Scout extends ShootingRobot {
    private static final Map<RobotType, Float> ATTRACTIONS = new HashMap<>();

    static {
        ATTRACTIONS.put(RobotType.ARCHON, 0f);
        ATTRACTIONS.put(RobotType.SOLDIER, -10f);
        ATTRACTIONS.put(RobotType.GARDENER, 50f);
        ATTRACTIONS.put(RobotType.LUMBERJACK, -100f);
        ATTRACTIONS.put(RobotType.SCOUT, 0f);
        ATTRACTIONS.put(RobotType.TANK, -50f);
    }

    public Scout(RobotController rc) {
        super(rc, ATTRACTIONS);
    }

    @Override
    RobotState buildRoamer() {
        return new Roam();
    }

    static boolean shouldBuild(int buildCount, int roundNum, int limit) {
        return buildCount % 15 == 0;
    }
}
