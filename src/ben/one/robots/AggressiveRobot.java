package ben.one.robots;

import battlecode.common.*;
import ben.one.Awareness;

import java.util.List;
import java.util.Map;

abstract class AggressiveRobot extends Robot {
    // TODO: this could be a simple array
    private final Map<RobotType, Float> attractionTable;

    AggressiveRobot(RobotController rc, Map<RobotType, Float> attractionTable) {
        super(rc);
        this.attractionTable = attractionTable;
    }

    abstract void attackEnemy(Awareness awareness) throws GameActionException;

    void moveAndAttack(Awareness awareness) throws GameActionException {
        if (!rc.hasMoved()) {
            // Unit may have already moved to dodge bullets
            moveAroundEnemy(awareness);
        }
        attackEnemy(awareness);
    }

    private float calculateAttractionFactor(RobotInfo enemyBot) {
        // Attraction is a function of unit type and health
        float baseAttraction = attractionTable.get(enemyBot.getType());
        float enemyHealth = enemyBot.getHealth() / enemyBot.getType().maxHealth;
        float health = (1 - enemyHealth) * 10;
        return baseAttraction + health;
    }

    private void moveAroundEnemy(Awareness awareness) throws GameActionException {
        MapLocation pos = rc.getLocation();
        float nextX = pos.x;
        float nextY = pos.y;

        float bodyRadius = rc.getType().bodyRadius;

        List<RobotInfo> enemy = awareness.findEnemy();
        for (RobotInfo enemyBot : enemy) {
            MapLocation enemyPos = enemyBot.getLocation();
            float distance = pos.distanceTo(enemyPos);
            float closeEnough = (bodyRadius * 1.5f) + enemyBot.getRadius();

            Direction dir = pos.directionTo(enemyPos);
            float attractionFactor = calculateAttractionFactor(enemyBot) * (distance - closeEnough);

            MapLocation targetPos = pos.add(dir, attractionFactor);

            rc.setIndicatorLine(pos, targetPos, 0, 255, 100);

            nextX -= pos.x - targetPos.x;
            nextY -= pos.y - targetPos.y;
        }

        MapLocation target = new MapLocation(nextX, nextY);

        rc.setIndicatorLine(pos, target, 0, 100, 255);

        Direction targetDir = pos.directionTo(target);
        if (targetDir != null && rc.canMove(targetDir)) {
            rc.move(targetDir);
        }
    }

    class Attack extends RobotState {
        RobotState interrupt(Awareness awareness) {
            if (awareness.isDanger()) {
                return this;
            } else {
                return wrappedState;
            }
        }

        RobotState act(Awareness awareness) throws GameActionException {
            moveAndAttack(awareness);
            return this;
        }
    }

    //    class MoveTo implements RobotState {
//        private MapLocation targetDir;
//
//        MoveTo(MapLocation order) {
//            this.targetDir = order;
//        }
//
//        @Override
//        public RobotState act(Awareness awareness) throws GameActionException {
//            MapLocation myLocation = rc.getLocation();
//            Direction dir = myLocation.directionTo(targetDir);
//            debug_outf("Trying to move in direction: %s", dir);
//            if (rc.canMove(dir)) {
//                debug_dir(myLocation, targetDir);
//                rc.move(dir);
//                return this;
//            } else {
//                defaultMovement(awareness);
//                return new Roam();
//            }
//        }
//    }

    class MoveTo extends RobotState {
        private float CLOSE_ENOUGH = 0.5f;

        private MapLocation targetLoc;

        MoveTo(MapLocation order) {
            this.targetLoc = order;
        }

        RobotState interrupt(Awareness awareness) {
            if (awareness.isBullets()) {
                return new Evade(this);
            } else if (awareness.isEnemy()) {
                return new Attack();
            } else {
                return this;
            }
        }

        RobotState act(Awareness awareness) throws GameActionException {
            MapLocation myLocation = rc.getLocation();
            float distance = myLocation.distanceTo(targetLoc);
            if (distance < CLOSE_ENOUGH) {
                return new Roam();
            }
            Direction dir = myLocation.directionTo(targetLoc);
            if (!rc.hasMoved() && rc.canMove(dir)) {
                rc.move(dir);
                return this;
            } else {
                defaultMovement(awareness);
                return this;
            }
        }
    }

    class Roam extends RobotState {
        RobotState interrupt(Awareness awareness) throws GameActionException {
            if (awareness.isBullets()) {
                return new Evade(this);
            } else if (awareness.isEnemy()) {
                return new Attack();
            } else if (awareness.hasOrders()) {
                // TODO - better targeting
                List<MapLocation> locs = awareness.getOrders();
                return new MoveTo(locs.iterator().next());
            } else {
                return this;
            }
        }

        @Override
        RobotState act(Awareness awareness) throws GameActionException {
            if (!rc.hasMoved()) {
                defaultMovement(awareness);
            }
            return this;
        }
    }
}
