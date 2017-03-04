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

    // TODO: get rid of this by having a Lumberjack specific attack and a ShootingRobot specific attack state
    abstract void attackEnemy(Awareness awareness) throws GameActionException;

    private float calculateAttractionFactor(RobotInfo enemyBot) {
        // Attraction is a function of unit type and health
        float baseAttraction = attractionTable.get(enemyBot.getType());
        float enemyHealth = enemyBot.getHealth() / enemyBot.getType().maxHealth;
        float health = (1 - enemyHealth) * 10;
        return baseAttraction + health;
    }

    private void moveAroundEnemy(Awareness awareness) throws GameActionException {
        if (rc.hasMoved()) {
            return;
        }

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

    class Shoot extends RobotState {
        Shoot() {
            super();
        }

        Shoot(RobotState state) {
            super(state);
        }

        RobotState interrupt(Awareness awareness) {
            if (awareness.isEnemy()) {
                return this;
            } else {
                return wrappedState;
            }
        }

        RobotState act(Awareness awareness) throws GameActionException {
            attackEnemy(awareness);
            callWrappedState(awareness);
            return this;
        }

        public String toString() {
            return String.format("SHOOT[wrappedState=%s]", wrappedState);
        }
    }

    class Attack extends RobotState {
        Attack(RobotState state) {
            super(state);
        }

        RobotState interrupt(Awareness awareness) {
            if (awareness.isDanger()) {
                return this;
            } else {
                return wrappedState;
            }
        }

        RobotState act(Awareness awareness) throws GameActionException {
            moveAroundEnemy(awareness);
            callWrappedState(awareness);
            return this;
        }
    }

    class MoveTo extends RobotState {
        private float CLOSE_ENOUGH = 0.5f;
        private int MAXIMUM_STUCK_MOVES = 20;

        private MapLocation targetLoc;

        private int stuckMoves = 0;

        MoveTo(MapLocation order) {
            this.targetLoc = order;
        }

        RobotState interrupt(Awareness awareness) {
            if (awareness.isBullets()) {
                return new Evade(new Shoot(this));
            } else if (awareness.isEnemy()) {
                return new Attack(new Shoot());
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
                stuckMoves = 0;
                rc.move(dir);
                return this;
            } else {
                defaultMovement(awareness);
                if (++stuckMoves >= MAXIMUM_STUCK_MOVES) {
                    return null;
                } else {
                    return this;
                }
            }
        }

        public String toString() {
            return String.format("MOVETO[target=%s,stuckMoves=%d]", targetLoc, stuckMoves);
        }
    }

    class Roam extends RobotState {
        RobotState interrupt(Awareness awareness) throws GameActionException {
            if (awareness.isBullets()) {
                return new Evade(new Shoot());
            } else if (awareness.isEnemy()) {
                return new Attack(new Shoot());
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
