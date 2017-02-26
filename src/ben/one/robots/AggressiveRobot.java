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
        broadcastEnemies(awareness.findEnemy());
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
}
