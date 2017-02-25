package ben.one.robots;

import battlecode.common.*;
import ben.one.Awareness;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

abstract class Robot {
    RobotController rc;
    private Team team;
    private Team enemy;

    Robot(RobotController rc) {
        this.rc = rc;
        this.team = rc.getTeam();
        this.enemy = this.team.opponent();
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

    private boolean onTarget(MapLocation me, float radius, BulletInfo bullet) {
        MapLocation bulletLoc = bullet.getLocation();
        Direction dirToMe = me.directionTo(bulletLoc).opposite();
        Direction bulletDir = bullet.getDir();
        float angle = Math.abs(dirToMe.degreesBetween(bulletDir));

        if (angle >= 1f) {
            return false;
        }

        float distance = me.distanceTo(bulletLoc);
        float missBy = distance * (float)Math.tan(angle);

        rc.setIndicatorLine(me, me.add(dirToMe.rotateLeftDegrees(90f), missBy), 0, 255, 127);
        rc.setIndicatorLine(me, me.add(dirToMe.rotateRightDegrees(90f), missBy), 0, 255, 127);

        return missBy < radius + bullet.getRadius();
    }

    abstract float getRadius();

    void evadeBullets(Awareness awareness) {
        BulletInfo[] bullets = awareness.findBullets();
        MapLocation myLocation = rc.getLocation();
        float radius = getRadius();
        List<BulletInfo> dangerousBullets = new ArrayList<>(bullets.length);
        for (BulletInfo bullet : bullets) {
            if (onTarget(myLocation, radius, bullet)) {
                dangerousBullets.add(bullet);
            }
        }
        debug_outf("Found: %d bullets", dangerousBullets.size());
    }

    // DEBUG

    void debug_outf(String pattern, Object... args) {
        System.out.printf("%s%n", rc.getID(), String.format(pattern, args));
    }
}
