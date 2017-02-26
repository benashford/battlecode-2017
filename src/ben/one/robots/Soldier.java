package ben.one.robots;

import battlecode.common.*;
import ben.one.Awareness;
import ben.one.Robot360;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Soldier extends Robot {
    private static final Map<RobotType, Float> ATTRACTIONS = new HashMap<>();

    static {
        ATTRACTIONS.put(RobotType.ARCHON, 100f);
        ATTRACTIONS.put(RobotType.SOLDIER, 0f);
        ATTRACTIONS.put(RobotType.GARDENER, 50f);
        ATTRACTIONS.put(RobotType.LUMBERJACK, -10f);
        ATTRACTIONS.put(RobotType.SCOUT, 100f);
        ATTRACTIONS.put(RobotType.TANK, -50f);
    }

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
        }
        if (!awareness.isDanger()) {
            state = state.act(awareness);
        }
    }

    private void resetState() {
        state = new Roam();
    }

    private float calculateAttractionFactor(RobotInfo enemyBot) {
        // Attraction is a function of unit type and health
        float baseAttraction = ATTRACTIONS.get(enemyBot.getType());
        float enemyHealth = enemyBot.getHealth() / enemyBot.getType().maxHealth;
        float health = (1 - enemyHealth) * 10;
        return baseAttraction + health;
    }

    private void moveAroundEnemy(Awareness awareness) throws GameActionException {
        // RULES: stay away from tanks and lumberjacks
        // move closer to weaker units
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

    private void fireAtEnemy(Awareness awareness) throws GameActionException {
        MapLocation currentLocation = rc.getLocation();
        List<RobotInfo> enemy = awareness.findEnemy();
        Robot360 team = awareness.findFriendsOrderedByAngle(currentLocation);
        enemyIter: for (RobotInfo enemyBot : enemy) {
            MapLocation enemyLocation = enemyBot.getLocation();
            Direction enemyDirection = currentLocation.directionTo(enemyLocation);
            List<RobotInfo> friendsInFiringLine = team.inDirection(enemyDirection);
            for (RobotInfo friend : friendsInFiringLine) {
                MapLocation friendLocation = friend.getLocation();
                float friendDistance = currentLocation.distanceTo(friendLocation);
                Direction friendDirection = currentLocation.directionTo(friendLocation);
                float angle = enemyDirection.degreesBetween(friendDirection);
                float missBy = friendDistance * (float)Math.sin(angle);
                if (Math.abs(missBy) < friend.getRadius()) {
                    debug_outf("Not firing at %s, due to location of %s, missBy: %.2f", enemyBot, friend, missBy);
                    debug_spot(friendLocation, 127, 0, 0);
                    continue enemyIter;
                } else {
                    debug_outf("Miss range to: %s is %.2f", friend, missBy);
                    debug_spot(friendLocation, 0, 0, 0);
                }
            }
            debug_outf("OK to fire at %s", enemyBot);
            if (friendsInFiringLine.isEmpty()) {
                if (rc.canFirePentadShot()) {
                    rc.firePentadShot(enemyDirection);
                    break;
                }
            }
            if (!rc.hasAttacked() && rc.canFireSingleShot()) {
                debug_shot(currentLocation, enemyLocation);
                rc.fireSingleShot(enemyDirection);
                debug_outf("Fired in: %s", enemyDirection);
                break;
            }
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
            if (!rc.hasMoved()) {
                defaultMovement(awareness);
            }
            return this;
        }
    }

    // DEBUGGING

    private void debug_spot(MapLocation location, int r, int g, int b) {
        rc.setIndicatorDot(location, r, g, b);
    }

    private void debug_shot(MapLocation location, MapLocation otherLocation) {
        rc.setIndicatorLine(location, otherLocation, 255, 153, 0);
    }
}

interface SoldierState extends RobotState<SoldierState> {

}