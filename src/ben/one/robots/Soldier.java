package ben.one.robots;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import ben.one.Awareness;

import java.util.HashMap;
import java.util.Map;

public class Soldier extends ShootingRobot<SoldierState> {
    private static final Map<RobotType, Float> ATTRACTIONS = new HashMap<>();

    static {
        ATTRACTIONS.put(RobotType.ARCHON, 100f);
        ATTRACTIONS.put(RobotType.SOLDIER, 0f);
        ATTRACTIONS.put(RobotType.GARDENER, 50f);
        ATTRACTIONS.put(RobotType.LUMBERJACK, -10f);
        ATTRACTIONS.put(RobotType.SCOUT, 100f);
        ATTRACTIONS.put(RobotType.TANK, -50f);
    }

    public Soldier(RobotController rc) {
        super(rc, ATTRACTIONS);
        resetState();
    }

    @Override
    void resetState() {
        state = new Roam();
    }

    private class Roam implements SoldierState {
        @Override
        public SoldierState act(Awareness awareness) throws GameActionException {
            if (!rc.hasMoved()) {
                defaultMovement(awareness);
            }
            return this;
        }
    }
}

interface SoldierState extends RobotState<SoldierState> {

}