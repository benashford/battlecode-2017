package ben.one.robots;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import ben.one.Awareness;

import java.util.HashMap;
import java.util.Map;

public class Scout extends ShootingRobot<ScoutState> {
    private static final Map<RobotType, Float> ATTRACTIONS = new HashMap<>();

    static {
        ATTRACTIONS.put(RobotType.ARCHON, 100f);
        ATTRACTIONS.put(RobotType.SOLDIER, -10f);
        ATTRACTIONS.put(RobotType.GARDENER, 50f);
        ATTRACTIONS.put(RobotType.LUMBERJACK, 0f);
        ATTRACTIONS.put(RobotType.SCOUT, 0f);
        ATTRACTIONS.put(RobotType.TANK, -50f);
    }

    public Scout(RobotController rc) {
        super(rc, ATTRACTIONS);
        resetState();
    }

    @Override
    void resetState() {
        state = new Roam();
    }

    private class Roam implements ScoutState {
        @Override
        public ScoutState act(Awareness awareness) throws GameActionException {
            if (!rc.hasMoved()) {
                defaultMovement(awareness);
            }
            return this;
        }
    }

    static boolean shouldBuild(int buildCount, int roundNum, int limit) {
        return buildCount % 15 == 0;
    }
}

interface ScoutState extends RobotState<ScoutState> {

}
