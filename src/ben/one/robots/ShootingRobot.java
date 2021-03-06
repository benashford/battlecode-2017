package ben.one.robots;

import battlecode.common.*;
import ben.one.Awareness;
import ben.one.Periscope;

import java.util.List;
import java.util.Map;

import static ben.one.Util.debug_outf;

abstract class ShootingRobot extends AggressiveRobot {
    ShootingRobot(RobotController rc, Map<RobotType, Float> attractionTable) {
        super(rc, attractionTable);
    }

    @Override
    void attackEnemy(Awareness awareness) throws GameActionException {
        MapLocation currentLocation = rc.getLocation();
        List<RobotInfo> enemy = awareness.findEnemy();
        Periscope team = awareness.findFriendsOrderedByAngle(currentLocation);
        Periscope opposition = new Periscope(currentLocation, enemy);
        enemyIter: for (RobotInfo enemyBot : enemy) {
            MapLocation enemyLocation = enemyBot.getLocation();
            Direction enemyDirection = currentLocation.directionTo(enemyLocation);
            List<RobotInfo> friendsInFiringLine = team.inDirection(enemyDirection);
            for (RobotInfo friend : friendsInFiringLine) {
                MapLocation friendLocation = friend.getLocation();
                float friendDistance = currentLocation.distanceTo(friendLocation);
                Direction friendDirection = currentLocation.directionTo(friendLocation);
                float angle = enemyDirection.radiansBetween(friendDirection);
                float missBy = friendDistance * (float)Math.sin(angle);
                if (Math.abs(missBy) < friend.getRadius()) {
                    debug_outf("Not firing at %s, due to location of %s, missBy: %.2f, distance: %.2f, angle: %.2f", enemyBot, friend, missBy, friendDistance, angle);
                    debug_spot(friendLocation, 127, 0, 0);
                    continue enemyIter;
                } else {
                    debug_outf("Miss range to: %s is %.2f", friend, missBy);
                    debug_spot(friendLocation, 0, 0, 0);
                }
            }
            debug_outf("OK to fire at %s", enemyBot);
            if (friendsInFiringLine.isEmpty() && rc.getTeamBullets() > 100f) {
                List<RobotInfo> enemiesInFiringLine = opposition.inDirection(enemyDirection);
                int numEnemiesInFiringLine = enemiesInFiringLine.size();
                if (numEnemiesInFiringLine > 2) {
                    if (rc.canFirePentadShot()) {
                        rc.firePentadShot(enemyDirection);
                        break;
                    }
                } else if (numEnemiesInFiringLine > 1) {
                    if (rc.canFireTriadShot()) {
                        rc.fireTriadShot(enemyDirection);
                        break;
                    }
                }
            }
            if (!rc.hasAttacked() && rc.canFireSingleShot()) {
                rc.fireSingleShot(enemyDirection);
                debug_outf("Fired in: %s", enemyDirection);
                break;
            }
        }
    }
}
