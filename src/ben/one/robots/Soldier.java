package ben.one.robots;

import battlecode.common.*;
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

    private float calculateAttractionFactor(RobotInfo enemyBot) {
        // TODO: implement me
        return 1.0f;
    }

    private void moveAroundEnemy(Awareness awareness) throws GameActionException {
        // RULES: stay away from tanks and lumberjacks
        // move closer to weaker units
        MapLocation pos = rc.getLocation();
        float nextX = pos.x;
        float nextY = pos.y;

        List<RobotInfo> enemy = awareness.findEnemy();
        for (RobotInfo enemyBot : enemy) {
            MapLocation enemyPos = enemyBot.getLocation();
            Direction dir = pos.directionTo(enemyPos);
            float attractionFactor = calculateAttractionFactor(enemyBot);
            MapLocation targetPos = pos.add(dir, attractionFactor);

            rc.setIndicatorLine(pos, targetPos, 0, 255, 100);

            nextX += targetPos.x;
            nextY += targetPos.y;
        }

        MapLocation target = new MapLocation(nextX, nextY);

        rc.setIndicatorLine(pos, target, 0, 100, 255);

        Direction targetDir = pos.directionTo(target);
        if (rc.canMove(targetDir)) {
            rc.move(target);
        }
    }

    private void fireAtEnemy(Awareness awareness) throws GameActionException {
        // TODO: real implementation
        RobotInfo enemyBot = awareness.findEnemy().get(0);
        MapLocation enemyPos = enemyBot.getLocation();
        if (rc.canFireSingleShot()) {
            rc.fireSingleShot(rc.getLocation().directionTo(enemyPos));
        }
    }

    private void moveAndFire(Awareness awareness) throws GameActionException {
        if (!rc.hasMoved()) {
            // Unit may have already moved to dodge bullets
            moveAroundEnemy(awareness);
        }
        fireAtEnemy(awareness);
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