package ben.one.robots;

import battlecode.common.*;
import ben.one.Awareness;

abstract class Robot {
    RobotController rc;

    Robot(RobotController rc) {
        this.rc = rc;
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
        return new Direction((float)Math.random() * 2 * (float)Math.PI);
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
        float strideRadius = type.strideRadius;
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
            float missBy = distance * (float)Math.tan(angle);

            if (Math.abs(missBy) < radius + bullet.getRadius()) {
                debug_outf("Bullet %s, will hit, distance: %.2f", bullet, missBy);
                float bulletSpeed = bullet.getSpeed();
                float distanceNextTurn = Math.max(0, distance - bulletSpeed);
                float escapeAngle = (float)((Math.PI / 2.0) + ((distanceNextTurn / bulletSenseRadius) * (Math.PI / 4.0)));
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
        if (rc.canMove(target)) {
            rc.move(target);
        } else {
            randomMovement();
        }
    }

    // DEBUG

    void debug_outf(String pattern, Object... args) {
        System.out.printf("%s%n", String.format(pattern, args));
    }
}
