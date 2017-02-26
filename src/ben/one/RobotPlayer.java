package ben.one;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import ben.one.robots.*;

public strictfp class RobotPlayer {
    public static void run(RobotController rc) throws GameActionException {
        switch (rc.getType()) {
            case ARCHON:
                new Archon(rc).run();
                break;
            case GARDENER:
                new Gardener(rc).run();
                break;
            case SOLDIER:
                new Soldier(rc).run();
                break;
            case LUMBERJACK:
                new Lumberjack(rc).run();
                break;
            case TANK:
                new Tank(rc).run();
                break;
            default:
                System.out.printf("Unimplemented Robot %s%n", rc.getType());
        }
    }
}
