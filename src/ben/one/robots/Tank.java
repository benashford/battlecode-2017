package ben.one.robots;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import ben.one.Awareness;

import java.util.HashMap;
import java.util.Map;

public class Tank extends ShootingRobot<TankState> {
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
        resetState();
    }

    @Override
    void resetState() {
        state = new Roam();
    }

    private class Roam implements TankState {
        @Override
        public TankState act(Awareness awareness) throws GameActionException {
            if (!rc.hasMoved()) {
                defaultMovement(awareness);
            }
            return this;
        }
    }

    static boolean shouldBuild(int buildCount, int round, int limit) {
        return buildCount % 6 == 0;
    }
}

interface TankState extends RobotState<TankState> {

}
