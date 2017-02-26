package ben.one;

import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Represents a 360 degree representation of robots
 *
 * TODO: generalise this for easier map searching
 */
public class Robot360 {
    private static final float SAME_DIRECTION_LIMIT = (float)Math.PI / 3f;

    private Robot[] robots;

    public Robot360(MapLocation loc, List<RobotInfo> robotInfo) {
        robots = new Robot[robotInfo.size()];
        int numRobots = 0;
        for (RobotInfo r : robotInfo) {
            robots[numRobots++] = new Robot(r, loc.directionTo(r.getLocation()));
        }
        Arrays.sort(robots, (r1, r2) -> {
            float diff = r1.direction.radians - r2.direction.radians;
            if (diff < 0) {
                return -1;
            } else {
                return 1;
            }
        });
    }

    private float getRadians(int idx) {
        return robots[idx].direction.radians;
    }

    private int binarySearch(float rads, int min, int max) {
        int diff = max - min;
        if (diff == 0) {
            throw new IllegalStateException("Unreachable");
        }
        if (diff == 1) {
            if (rads < getRadians(min)) {
                return min;
            } else {
                return max;
            }
        } else {
            int idx = min + (diff / 2);
            if (rads < getRadians(idx)) {
                return binarySearch(rads, min, idx);
            } else {
                return binarySearch(rads, idx, max);
            }
        }
    }

    private int binarySearch(float rads) {
        return binarySearch(rads, 0, robots.length);
    }

    public List<RobotInfo> inDirection(Direction targetDirection) {
        int rlen = robots.length;
        if (rlen == 0) {
            return Collections.emptyList();
        }

        Direction leftFlank = targetDirection.rotateLeftRads(SAME_DIRECTION_LIMIT);
        Direction rightFlank = targetDirection.rotateRightRads(SAME_DIRECTION_LIMIT);

        float rightRadians = rightFlank.radians;
        float targetRadians = leftFlank.radiansBetween(rightFlank);

        debug_outf("Searching between: %s and %s", leftFlank, rightFlank);

        int rightIdx = binarySearch(rightRadians);
        List<RobotInfo> inFiringLine = new ArrayList<>(rlen);

        for (int i = rightIdx; i - rightIdx < rlen;i++) {
            Robot nr = robots[i % rlen];
            Direction rd = nr.direction;
            float angle = rd.radiansBetween(rightFlank);
            if (angle > targetRadians && angle <= 0) {
                inFiringLine.add(nr.robotInfo);
            } else {
                break;
            }
        }

        debug_outf("Found %d in the firing line", inFiringLine.size());

        return inFiringLine;
    }

    // DEBUG

    // TODO: de-duplicate, there's a few of these
    private void debug_outf(String pattern, Object... args) {
        System.out.printf("%n***%n%s%n", String.format(pattern, args));
    }
}

class Robot {
    RobotInfo robotInfo;
    Direction direction;

    Robot(RobotInfo ri, Direction d) {
        this.robotInfo = ri;
        this.direction = d;
    }
}
