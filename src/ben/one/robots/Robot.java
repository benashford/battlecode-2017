package ben.one.robots;

import battlecode.common.*;
import ben.one.Awareness;
import ben.one.comms.ShipToShore;
import ben.one.comms.ShoreToShip;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static ben.one.Util.debug_outf;

abstract class Robot {
    /**
     * The maximum number of bullet sources to broadcast.
     */
    private static final int MAX_BROADCAST_ENEMIES = 4;

    RobotController rc;
    RobotState state;

    ShipToShore radio;
    ShoreToShip orders;

    private Random rand;

    Robot(RobotController rc) {
        this.rc = rc;
        radio = new ShipToShore(rc);
        orders = new ShoreToShip(rc);
        rand = new Random();
    }

    abstract RobotState defaultState();

    RobotState initState() {
        return defaultState();
    }

    private RobotState orDefault(RobotState state) {
        if (state == null) {
            return defaultState();
        } else {
            return state;
        }
    }

    private void doTurn(Awareness awareness) throws GameActionException {
        debug_outf("STARTING TURN! (state=%s)", state);
        state = orDefault(state.interrupt(awareness));
        debug_outf("AFTER INTERRUPT! (state=%s)", state);
        state = orDefault(state.act(awareness));
        debug_outf("AFTER ACT! (state=%s)", state);
        if (state == null) {
            state = defaultState();
        }

        // Do signalling
        boolean enemiesNearby = awareness.isEnemy();
        if (enemiesNearby) {
            broadcastEnemies(awareness.findEnemy());
        }
        debug_outf("ENDING TURN! (state=%s)", state);
    }

    public final void run() throws GameActionException {
        state = initState();
        while (true) {
            doTurn(new Awareness(rc, orders));
            Clock.yield();
        }
    }

    /**
     * Don't stand still...
     */
    static Direction randomDirection() {
        return new Direction((float) Math.random() * 2 * (float) Math.PI);
    }

    /**
     * When all out of other ideas...
     */
    void randomMovement() throws GameActionException {
        Direction d = randomDirection();
        if (!rc.hasMoved() && rc.canMove(d)) {
            rc.move(d);
        }
    }

    boolean shakeTreeMovement(Awareness awareness) throws GameActionException {
        TreeInfo tree = awareness.findNearestTreeWithBullets();
        if (tree != null) {
            if (rc.canInteractWithTree(tree.getID())) {
                rc.shake(tree.getID());
                return true;
            } else {
                if (!rc.hasMoved() && rc.canMove(tree.getLocation())) {
                    rc.move(tree.getLocation());
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Default behaviour is to shake trees
     */
    void defaultMovement(Awareness awareness) throws GameActionException {
        if (!shakeTreeMovement(awareness)) {
            randomMovement();
        }
    }

    private void broadcastEnemies(List<RobotInfo> enemies) throws GameActionException {
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

    void debug_spot(MapLocation location, int r, int g, int b) {
        rc.setIndicatorDot(location, r, g, b);
    }

    void debug_point_angle(float angle, int r, int g, int b) {
        rc.setIndicatorLine(rc.getLocation(), rc.getLocation().add(angle, 2f), r, g, b);
    }

    class Evade extends RobotState {
        Evade(RobotState state) {
            super(state);
        }

        @Override
        public RobotState interrupt(Awareness awareness) {
            if (!awareness.isBullets()) {
                return wrappedState;
            } else {
                return this;
            }
        }

        @Override
        public RobotState act(Awareness awareness) throws GameActionException {
            if (!rc.hasMoved()) {
                evadeBullets(awareness);
            }
            callWrappedState(awareness);
            return this;
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
            if (myLocation.distanceTo(target) > 0.2f && !rc.hasMoved() && rc.canMove(d)) {
                rc.move(d);
            }
        }

        public String toString() {
            return String.format("EVADE[wrappedState=%s]", wrappedState);
        }
    }
}
