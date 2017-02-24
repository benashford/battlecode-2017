package ben.one.robots;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import ben.one.Awareness;

public class Soldier extends Robot {
    private SoldierState state = new Roam();

    public Soldier(RobotController rc) {
        super(rc);
    }

    void doTurn(Awareness awareness) throws GameActionException {
        // TODO: defensiveness/state switching
        state = state.act(awareness);
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