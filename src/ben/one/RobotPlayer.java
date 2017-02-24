package ben.one;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import ben.one.robots.Archon;
import ben.one.robots.Gardener;
import ben.one.robots.Soldier;

public strictfp class RobotPlayer {
    public static void run(RobotController rc) throws GameActionException {
        switch (rc.getType()) {
            case ARCHON:
                new Archon(rc).run();
            case GARDENER:
                new Gardener(rc).run();
            case SOLDIER:
                new Soldier(rc).run();
            default:
                System.out.printf("Unimplemented Robot %s%n", rc.getType());
        }
    }
}
