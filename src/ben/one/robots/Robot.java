package ben.one.robots;

import battlecode.common.*;
import ben.one.Awareness;
import ben.one.comms.ShipToShore;
import ben.one.comms.ShoreToShip;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static ben.one.Util.debug_outf;

abstract class Robot {
    /**
     * The maximum number of bullet sources to broadcast.
     */
    private static final int MAX_BROADCAST_BULLETS = 2;
    private static final int MAX_BROADCAST_ENEMIES = 4;

    RobotController rc;
    RobotState state;

    ShipToShore radio;
    ShoreToShip orders;

    Robot(RobotController rc) {
        this.rc = rc;
        radio = new ShipToShore(rc);
        orders = new ShoreToShip(rc);
    }

    abstract void doTurn(Awareness awareness) throws GameActionException;

    public final void run() throws GameActionException {
        while (true) {
            Awareness awareness = new Awareness(rc);
            doTurn(awareness);
            Clock.yield();
        }
    }

    /**
     * Don't stand still...
     */
    static Direction randomDirection() {
        return new Direction((float) Math.random() * 2 * (float) Math.PI);
    }

    void randomMovement() throws GameActionException {
        Direction d = randomDirection();
        if (rc.canMove(d)) {
            rc.move(d);
        }
    }

    /**
     * Default behaviour is to shake trees
     */
    void defaultMovement(Awareness awareness) throws GameActionException {
        TreeInfo tree = awareness.findNearestTreeWithBullets();
        if (tree != null) {
            if (rc.canInteractWithTree(tree.getID())) {
                rc.shake(tree.getID());
                return;
            } else {
                if (rc.canMove(tree.getLocation())) {
                    rc.move(tree.getLocation());
                    return;
                }
            }
        }
        randomMovement();
    }

    void evadeBullets(Awareness awareness) throws GameActionException {
        BulletInfo[] bullets = awareness.findBullets();
        MapLocation myLocation = rc.getLocation();
        RobotType type = rc.getType();
        float radius = type.bodyRadius;
        float bulletSenseRadius = type.bulletSightRadius;
        float nextX = myLocation.x;
        float nextY = myLocation.y;
        for (BulletInfo bullet : bullets) {
            MapLocation bulletLoc = bullet.getLocation();
            Direction dirToMe = bulletLoc.directionTo(myLocation);
            Direction bulletDir = bullet.getDir();
            float angle = dirToMe.degreesBetween(bulletDir);
            if (Math.abs(angle) >= 1f) {
                continue;
            }
            float distance = myLocation.distanceTo(bulletLoc);
            float missBy = distance * (float) Math.tan(angle);

            if (Math.abs(missBy) < radius) {
                debug_outf("Bullet %s, will hit, distance: %.2f", bullet, missBy);
                float bulletSpeed = bullet.getSpeed();
                float distanceNextTurn = Math.max(0, distance - bulletSpeed);
                float escapeAngle = (float) ((Math.PI / 2.0) + ((distanceNextTurn / bulletSenseRadius) * (Math.PI / 4.0)));
                float angleModifier = 1f;
                if (angle < 0) {
                    angleModifier = -1f;
                }
                MapLocation target = myLocation.add(bulletDir.rotateLeftRads(escapeAngle * angleModifier), radius * bullet.getDamage());
                float diffX = nextX - target.x;
                float diffY = nextY - target.y;
                nextX += diffX;
                nextY += diffY;
                rc.setIndicatorLine(myLocation, myLocation.add(dirToMe.rotateLeftDegrees(90f * angleModifier), radius), 0, 255, 127);
            }
        }
        MapLocation target = new MapLocation(nextX, nextY);
        rc.setIndicatorLine(myLocation, target, 70, 100, 255);
        Direction d = myLocation.directionTo(target);
        if (myLocation.distanceTo(target) > 0.2f && rc.canMove(d)) {
            rc.move(d);
        }
        broadcastBullets(bullets);
    }

    void broadcastBullets(BulletInfo[] bullets) throws GameActionException {
        int numBullets = bullets.length;
        if (numBullets <= MAX_BROADCAST_BULLETS) {
            for (BulletInfo bullet : bullets) {
                MapLocation source = bullet.getLocation();
                radio.broadcastBullet(source);
            }
        } else {
            List<BulletInfo> shuffleableBullets = new ArrayList<>(Arrays.asList(bullets));
            Collections.shuffle(shuffleableBullets);
            for (int i = 0; i < MAX_BROADCAST_BULLETS; i++) {
                BulletInfo bullet = shuffleableBullets.get(i);
                radio.broadcastBullet(bullet.getLocation());
            }
        }
    }

    void broadcastEnemies(List<RobotInfo> enemies) throws GameActionException {
        int numEnemies = enemies.size();
        if (numEnemies <= MAX_BROADCAST_ENEMIES) {
            for (RobotInfo enemy : enemies) {
                MapLocation source = enemy.getLocation();
                radio.broadcastEnemy(source);
            }
        } else {
            List<RobotInfo> shuffleableEnemies = new ArrayList<>(enemies);
            Collections.shuffle(shuffleableEnemies);
            for (int i = 0; i < MAX_BROADCAST_ENEMIES; i++) {
                RobotInfo enemy = shuffleableEnemies.get(i);
                radio.broadcastEnemy(enemy.getLocation());
            }
        }
    }

    MapLocation listenForOrders() throws GameActionException {
        List<MapLocation> pendingOrders = orders.readOrders();
        if (pendingOrders.isEmpty()) {
            return null;
        } else {
            // TODO: real implementation
            return pendingOrders.get(0);
        }
    }
}
