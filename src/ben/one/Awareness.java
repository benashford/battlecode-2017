package ben.one;

import battlecode.common.*;
import ben.one.comms.ShoreToShip;

import java.util.ArrayList;
import java.util.List;

public class Awareness {
    private final RobotController rc;
    private final ShoreToShip radio;

    private BulletInfo[] bullets;
    private List<TreeInfo> enemyTrees;
    private List<TreeInfo> friendTrees;
    private List<TreeInfo> neutralTrees;

    private List<RobotInfo> enemy;
    private List<RobotInfo> friend;

    private List<MapLocation> orders;

    public Awareness(RobotController rc, ShoreToShip radio) {
        this.rc = rc;
        this.radio = radio;
    }

    private void processTrees() {
        TreeInfo[] trees = rc.senseNearbyTrees();
        enemyTrees = new ArrayList<>(trees.length);
        friendTrees = new ArrayList<>(trees.length);
        neutralTrees = new ArrayList<>(trees.length);

        Team myTeam = rc.getTeam();

        for (TreeInfo tree : trees) {
            Team treeTeam = tree.getTeam();
            if (treeTeam == Team.NEUTRAL) {
                neutralTrees.add(tree);
            } else if (treeTeam == myTeam) {
                friendTrees.add(tree);
            } else {
                enemyTrees.add(tree);
            }
        }
    }

    public List<TreeInfo> findNeutralTrees() {
        if (neutralTrees == null) {
            processTrees();
        }
        return neutralTrees;
    }

    public List<TreeInfo> findFriendTrees() {
        if (friendTrees == null) {
            processTrees();
        }
        return friendTrees;
    }

    public List<TreeInfo> findEnemyTrees() {
        if (enemyTrees == null) {
            processTrees();
        }
        return enemyTrees;
    }

    public TreeInfo findNearestTreeWithBullets() {
        List<TreeInfo> trees = findNeutralTrees();
        for (TreeInfo tree : trees) {
            if (tree.getContainedBullets() > 0) {
                debug_tree(tree, 255);
                return tree;
            }
        }
        return null;
    }

    public TreeInfo findNearestTreeWithRobot() {
        List<TreeInfo> trees = findNeutralTrees();
        for (TreeInfo tree : trees) {
            if (tree.getContainedRobot() != null) {
                debug_tree(tree, 127);
                return tree;
            }
        }
        return null;
    }

    public TreeInfo findNearestTree() {
        List<TreeInfo> enemyTrees = findEnemyTrees();
        if (!enemyTrees.isEmpty()) {
            return enemyTrees.iterator().next();
        }
        List<TreeInfo> neutralTrees = findNeutralTrees();
        if (!neutralTrees.isEmpty()) {
            return neutralTrees.iterator().next();
        }
        return null;
    }

    public BulletInfo[] findBullets() {
        if (bullets == null) {
            bullets = rc.senseNearbyBullets();
        }
        return bullets;
    }

    /**
     * Like findBullets, but only includes those that are nearby or heading in my direction.
     */
    public List<BulletInfo> findDangerousBullets() {
        float myRadius = rc.getType().bodyRadius;
        MapLocation myLocation = rc.getLocation();
        BulletInfo[] allBullets = findBullets();
        List<BulletInfo> bullets = new ArrayList<>();
        for (BulletInfo bullet : allBullets) {
            MapLocation bulletLoc = bullet.getLocation();
            float distance = myLocation.distanceTo(bulletLoc);
            if (distance < myRadius * 2f) {
                bullets.add(bullet);
                continue;
            }
            Direction meToBullet = bulletLoc.directionTo(myLocation);
            float approachAngle = meToBullet.degreesBetween(bullet.getDir());
            if (Math.abs(approachAngle) < (Math.PI / 3.0)) {
                bullets.add(bullet);
                continue;
            }
        }
        return bullets;
    }

    public boolean isBullets() {
        return findBullets().length > 0;
    }

    public boolean isDangerousBullets() {
        return !findDangerousBullets().isEmpty();
    }

    private void processRobots() {
        Team myTeam = rc.getTeam();
        RobotInfo[] robots = rc.senseNearbyRobots();
        friend = new ArrayList<>(robots.length);
        enemy = new ArrayList<>(robots.length);

        for (RobotInfo robot : robots) {
            if (robot.getTeam() == myTeam) {
                friend.add(robot);
            } else {
                enemy.add(robot);
            }
        }
    }

    public List<RobotInfo> findEnemy() {
        if (enemy == null) {
            processRobots();
        }
        return enemy;
    }

    public List<RobotInfo> findFriends() {
        if (friend == null) {
            processRobots();
        }
        return friend;
    }

    public Periscope findFriendsOrderedByAngle(MapLocation loc) {
        return new Periscope(loc, findFriends());
    }

    public boolean isEnemy() {
        return !findEnemy().isEmpty();
    }

    public boolean isDanger() {
        return isDangerousBullets() || isEnemy();
    }

    private void processOrders() throws GameActionException {
        orders = radio.readOrders();
    }

    public List<MapLocation> getOrders() throws GameActionException {
        if (orders == null) {
            processOrders();
        }
        return orders;
    }

    public boolean hasOrders() throws GameActionException {
        if (orders == null) {
            processOrders();
        }
        return !orders.isEmpty();
    }

    // DEBUG

    private void debug_tree(TreeInfo tree, int intensity) {
        rc.setIndicatorDot(tree.getLocation(), intensity, intensity, intensity);
    }
}
