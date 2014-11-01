package generatormods.test;

import generatormods.common.Dir;

import org.junit.Test;

import static generatormods.common.Handedness.L_HAND;
import static generatormods.common.Handedness.R_HAND;
import static org.junit.Assert.assertEquals;

public class DirTests {
    @Test
    public void testRotate() {
        // Integer, default mirroring of R_HAND
        assertEquals(Dir.NORTH.rotate(-1), Dir.WEST);
        assertEquals(Dir.NORTH.rotate(0), Dir.NORTH);
        assertEquals(Dir.NORTH.rotate(1), Dir.EAST);
        assertEquals(Dir.NORTH.rotate(2), Dir.SOUTH);
        assertEquals(Dir.NORTH.rotate(3), Dir.WEST);
        assertEquals(Dir.NORTH.rotate(4), Dir.NORTH);

        // Handedness, single
        assertEquals(Dir.NORTH.rotate(R_HAND), Dir.EAST);
        assertEquals(Dir.NORTH.rotate(L_HAND), Dir.WEST);

        // Handedness, integer
        assertEquals(Dir.NORTH.rotate(R_HAND, 0), Dir.NORTH);
        assertEquals(Dir.NORTH.rotate(R_HAND, 1), Dir.EAST);
        assertEquals(Dir.NORTH.rotate(R_HAND, 2), Dir.SOUTH);
        assertEquals(Dir.NORTH.rotate(R_HAND, 3), Dir.WEST);

        assertEquals(Dir.NORTH.rotate(L_HAND, 0), Dir.NORTH);
        assertEquals(Dir.NORTH.rotate(L_HAND, 1), Dir.WEST);
        assertEquals(Dir.NORTH.rotate(L_HAND, 2), Dir.SOUTH);
        assertEquals(Dir.NORTH.rotate(L_HAND, 3), Dir.EAST);

        // No handedness
        assertEquals(Dir.NORTH.rotate(null, 0), Dir.NORTH);
        assertEquals(Dir.NORTH.rotate(null), Dir.NORTH);
    }

    @Test
    public void testOpposite() {
        assertEquals(Dir.NORTH.opposite(), Dir.SOUTH);
        assertEquals(Dir.EAST.opposite(), Dir.WEST);
        assertEquals(Dir.SOUTH.opposite(), Dir.NORTH);
        assertEquals(Dir.WEST.opposite(), Dir.EAST);
    }

    @Test
    public void testReorient() {
        // clockwise/non-mirrored
        assertEquals(Dir.NORTH.reorient(R_HAND, Dir.NORTH), Dir.NORTH);
        assertEquals(Dir.NORTH.reorient(R_HAND, Dir.EAST), Dir.EAST);
        assertEquals(Dir.NORTH.reorient(R_HAND, Dir.SOUTH), Dir.SOUTH);
        assertEquals(Dir.NORTH.reorient(R_HAND, Dir.WEST), Dir.WEST);

        assertEquals(Dir.EAST.reorient(R_HAND, Dir.NORTH), Dir.EAST);
        assertEquals(Dir.EAST.reorient(R_HAND, Dir.EAST), Dir.SOUTH);
        assertEquals(Dir.EAST.reorient(R_HAND, Dir.SOUTH), Dir.WEST);
        assertEquals(Dir.EAST.reorient(R_HAND, Dir.WEST), Dir.NORTH);

        assertEquals(Dir.SOUTH.reorient(R_HAND, Dir.NORTH), Dir.SOUTH);
        assertEquals(Dir.SOUTH.reorient(R_HAND, Dir.EAST), Dir.WEST);
        assertEquals(Dir.SOUTH.reorient(R_HAND, Dir.SOUTH), Dir.NORTH);
        assertEquals(Dir.SOUTH.reorient(R_HAND, Dir.WEST), Dir.EAST);

        assertEquals(Dir.WEST.reorient(R_HAND, Dir.NORTH), Dir.WEST);
        assertEquals(Dir.WEST.reorient(R_HAND, Dir.EAST), Dir.NORTH);
        assertEquals(Dir.WEST.reorient(R_HAND, Dir.SOUTH), Dir.EAST);
        assertEquals(Dir.WEST.reorient(R_HAND, Dir.WEST), Dir.SOUTH);

        //mirrored
        assertEquals(Dir.NORTH.reorient(L_HAND, Dir.NORTH), Dir.NORTH);
        assertEquals(Dir.NORTH.reorient(L_HAND, Dir.EAST), Dir.WEST);
        assertEquals(Dir.NORTH.reorient(L_HAND, Dir.SOUTH), Dir.SOUTH);
        assertEquals(Dir.NORTH.reorient(L_HAND, Dir.WEST), Dir.EAST);

        assertEquals(Dir.EAST.reorient(L_HAND, Dir.NORTH), Dir.EAST);
        assertEquals(Dir.EAST.reorient(L_HAND, Dir.EAST), Dir.NORTH);
        assertEquals(Dir.EAST.reorient(L_HAND, Dir.SOUTH), Dir.WEST);
        assertEquals(Dir.EAST.reorient(L_HAND, Dir.WEST), Dir.SOUTH);

        assertEquals(Dir.SOUTH.reorient(L_HAND, Dir.NORTH), Dir.SOUTH);
        assertEquals(Dir.SOUTH.reorient(L_HAND, Dir.EAST), Dir.EAST);
        assertEquals(Dir.SOUTH.reorient(L_HAND, Dir.SOUTH), Dir.NORTH);
        assertEquals(Dir.SOUTH.reorient(L_HAND, Dir.WEST), Dir.WEST);

        assertEquals(Dir.WEST.reorient(L_HAND, Dir.NORTH), Dir.WEST);
        assertEquals(Dir.WEST.reorient(L_HAND, Dir.EAST), Dir.SOUTH);
        assertEquals(Dir.WEST.reorient(L_HAND, Dir.SOUTH), Dir.EAST);
        assertEquals(Dir.WEST.reorient(L_HAND, Dir.WEST), Dir.NORTH);
    }
}
