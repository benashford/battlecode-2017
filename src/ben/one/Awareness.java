package ben.one;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.List;

public class Awareness {
    private final RobotController rc;

    private BulletInfo[] bullets;
    private TreeInfo[] trees;
    private List<RobotInfo> enemy;
    private List<RobotInfo> friend;

    public Awareness(RobotController rc) {
        this.rc = rc;
    }

    public TreeInfo[] findTrees() {
        if (trees == null) {
            trees = rc.senseNearbyTrees();
        }
        return trees;
    }

    public TreeInfo findNearestTreeWithBullets() {
        TreeInfo[] trees = findTrees();
        for (TreeInfo tree : trees) {
            if (tree.getContainedBullets() > 0) {
                debug_tree(tree);
                return tree;
            }
        }
        return null;
    }

    public List<TreeInfo> findTeamTrees() {
        Team myTeam = rc.getTeam();
        TreeInfo[] trees = findTrees();
        List<TreeInfo> teamTrees = new ArrayList<>(trees.length);
        for (TreeInfo tree : trees) {
            if (tree.getTeam() == myTeam) {
                teamTrees.add(tree);
            }
        }
        return teamTrees;
    }

    public BulletInfo[] findBullets() {
        if (bullets == null) {
            bullets = rc.senseNearbyBullets();
        }
        return bullets;
    }

    public boolean isBullets() {
        return findBullets().length > 0;
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

    public boolean isEnemy() {
        return !findEnemy().isEmpty();
    }

    public boolean isDanger() {
        return isBullets() || isEnemy();
    }

    // DEBUG

    private void debug_tree(TreeInfo tree) {
        rc.setIndicatorDot(tree.getLocation(), 255, 127, 127);
    }
}
