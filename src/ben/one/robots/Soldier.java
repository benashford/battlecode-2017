package ben.one.robots;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import ben.one.Awareness;

import java.util.List;

public class Soldier extends Robot {
    private SoldierState state = new Roam();

    public Soldier(RobotController rc) {
        super(rc);
    }

    void doTurn(Awareness awareness) throws GameActionException {
        if (awareness.isBullets()) {
            evadeBullets(awareness);
            resetState();
        }
        if (awareness.isEnemy()) {
            moveAndFire(awareness);
            resetState();
            if (!rc.hasMoved()) { // TODO - remove
                randomMovement(); // TODO - remove
            }
        }
        if (!awareness.isDanger()) {
            state = state.act(awareness);
        }
    }

    private void resetState() {
        state = new Roam();
    }

    private void moveAndFire(Awareness awareness) {
        List<RobotInfo> enemy = awareness.findEnemy();
        debug_outf("Found: %d enemy", enemy.size());
    }

    private class Roam implements SoldierState {
        @Override
        public SoldierState act(Awareness awareness) throws GameActionException {
            defaultMovement(awareness);
            return this;
        }
    }
}

interface SoldierState extends RobotState<SoldierState> {

}