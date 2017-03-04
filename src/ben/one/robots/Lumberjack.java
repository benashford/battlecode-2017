package ben.one.robots;

import battlecode.common.*;
import ben.one.Awareness;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lumberjack extends AggressiveRobot {
    private static final Map<RobotType, Float> ATTRACTIONS = new HashMap<>();

    static {
        ATTRACTIONS.put(RobotType.ARCHON, 20f);
        ATTRACTIONS.put(RobotType.SOLDIER, 0f);
        ATTRACTIONS.put(RobotType.GARDENER, 10f);
        ATTRACTIONS.put(RobotType.LUMBERJACK, -20f);
        ATTRACTIONS.put(RobotType.SCOUT, -20f);
        ATTRACTIONS.put(RobotType.TANK, -40f);
    }

    public Lumberjack(RobotController rc) {
        super(rc, ATTRACTIONS);
    }

    @Override
    RobotState defaultState() {
        return new Roam();
    }

    private <B extends BodyInfo> boolean inStrikingRange(MapLocation me, List<B> bodies) {
        for (B body : bodies) {
            MapLocation bodyLoc = body.getLocation();
            float distance = me.distanceTo(bodyLoc) - body.getRadius();
            if (distance < RobotType.LUMBERJACK.bodyRadius * 2) {
                return true;
            }
        }
        return false;
    }

    @Override
    void attackEnemy(Awareness awareness) throws GameActionException {
        // Check that an enemy is within striking range, but no units of own side are that close
        MapLocation myLocation = rc.getLocation();
        List<RobotInfo> friends = awareness.findFriends();
        if (inStrikingRange(myLocation, friends)) {
            return;
        }
        List<TreeInfo> teamTrees = awareness.findFriendTrees();
        if (inStrikingRange(myLocation, teamTrees)) {
            return;
        }
        List<RobotInfo> enemy = awareness.findEnemy();
        List<TreeInfo> enemyTrees = awareness.findEnemyTrees();
        boolean attack = inStrikingRange(myLocation, enemy) && inStrikingRange(myLocation, enemyTrees);
        if (attack && rc.canStrike()) {
            rc.strike();
        }
    }

    private void chopTrees(Awareness awareness) throws GameActionException {
        TreeInfo tree = awareness.findNearestTreeWithRobot();
        if (tree != null) {
            if (rc.canInteractWithTree(tree.getID())) {
                rc.chop(tree.getID());
                return;
            } else if (!rc.hasMoved()) {
                Direction d = rc.getLocation().directionTo(tree.getLocation());
                if (rc.canMove(d)) {
                    rc.move(d);
                }
            }
        }
        TreeInfo nearestTree = awareness.findNearestTree();
        if (nearestTree != null) {
            if (rc.canInteractWithTree(nearestTree.getID()) && !rc.hasAttacked()) {
                rc.chop(nearestTree.getID());
                return;
            }
        }
        if (!rc.hasMoved()) {
            defaultMovement(awareness);
        }
    }

    static boolean shouldBuild(int buildCount, int roundCount, int roundLimit) {
        return false;
//        if (roundCount > (roundLimit / 2)) {
//            return false;
//        } if (roundCount > (roundLimit / 4)) {
//            return buildCount % 10 == 0;
//        } else {
//            return buildCount % 5 == 0;
//        }
    }
}
