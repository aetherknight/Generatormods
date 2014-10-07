/* Source code for the Generator Mods (CARuins, Great Walls, Walled Cities) for the game Minecraft
 * Copyright (C) 2011-2014 by Noah Whitman (formivore) <wakatakeru@gmail.com>
 * Copyright (C) 2013-2014 by Olivier Sylvain (GotoLink) <gotolinkminecraft@gmail.com>
 * Copyright (C) 2014 William (B.J.) Snow Orvis (aetherknight) <aetherknight@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package generatormods.common;

import java.util.Random;
    
/**
 * <p>Direction, as Generatormods currently uses them.</p>
 *
 * <p>At present, Generatormods' directions start with NORTH(0), then go clockwise
 * until WEST(3). This is different from Minecraft's Direction class, which
 * starts with South(0) and then goes clockwise unitl East(3). Also note that
 * Minecraft's notion of Facings also has a completely different ordering for
 * its directions. The following table provides some help understanding how
 * various directions are mapped to index values:</p>
 *
 * <table>
 * <tr><th>Direction</th><th>Minecraft Direction</th><th>Minecraft Facing/ForgeDirection</th><th>Vine Metadata</th><th>Generatormods Dir ordinal</th></tr>
 * <tr><td>South</td>    <td>0</td>                  <td>3</td>                              <td>0001 (1)</td>     <td>2</td>                        </tr>
 * <tr><td>West</td>     <td>1</td>                  <td>4</td>                              <td>0010 (2)</td>     <td>3</td>                        </tr>
 * <tr><td>North</td>    <td>2</td>                  <td>2</td>                              <td>0100 (4)</td>     <td>0</td>                        </tr>
 * <tr><td>East</td>     <td>3</td>                  <td>5</td>                              <td>1000 (8)</td>     <td>1</td>                        </tr>
 * <tr><td>Down</td>     <td>-</td>                  <td>0</td>                              <td>-</td>            <td>-</td>                        </tr>
 * <tr><td>Up</td>       <td>-</td>                  <td>1</td>                              <td>-</td>            <td>-</td>                        </tr>
 * </table>
 */
public enum Dir {
	NORTH(0, -1), EAST(1,0), SOUTH(0,1), WEST(-1,0);

    private static final Dir[] directions = new Dir[] { NORTH, EAST, SOUTH, WEST };
    private static final Dir[] opposite = new Dir[] { SOUTH, WEST, NORTH, EAST };

    @Deprecated
    public final int minecraftDirection;

    /**
     * The X-axis direction for this Dir. -1 means west, and +1 means east,
     * just like in Minecraft.
     */
    public final int i;

    /**
     * The Z-axis direction for this Dir. -1 means north, +1 means south.
     */
    public final int k;

    /**
     * The "local" X-axis direction for this Dir. -1 means west, and +1 means
     * east, just like in Minecraft.
     */
    public final int x;

    /**
     * The "local" Z-direction for this Dir. Oddly, this is inverted from the
     * value of k. 1 means north, -1 means south. Note that Generatormods
     * currently swaps the "y" and "z" axis labels from Minecraft.
     *
     * TODO: Make Generatormods' axis names consistant with Minecraft.
     */
    public final int y;

    Dir(int x, int z) {
        // currently, we start with north instead of south.
        this.minecraftDirection = (ordinal() + 2) % 4;
        this.i = x;
        this.k = z;

        this.x = x;
        this.y = -z;
    }

    /**
     * Reorient dir in relation to this direction. Basically, it treats "this"
     * as the new "north" for dir, which is then reoriented. If clockwise is
     * true, then it reorients normally, but if it is false, then it is as if
     * dir is mirror first.
     *
     * For example:
     *
     *     EAST.reorient(true, WEST)
     *
     * will return NORTH, while:
     *
     *     EAST.reorient(false, WEST)
     *
     * will return SOUTH.
     */
    public Dir reorient(boolean clockwise, Dir dir) {
        return this.rotate(clockwise, dir.ordinal());
    }

    /**
     * Rotate numTurns clockwise from this.
     */
    public Dir rotate(int numTurns) {
        return rotate(true, numTurns);
    }

    /**
     * Rotates the current Dir numTurns*90 degrees. If clockwise is true, it
     * rotates clockwise, if it is false it rotates counterclockwise.
     */
    public Dir rotate(boolean clockwise, int numTurns) {
        return directions[(this.ordinal() + (clockwise ? 1 : -1) * numTurns) % 4];
    }

    public static Dir randomDir(Random random) {
        return Dir.directions[random.nextInt(4)];
    }

    @Deprecated
    public Dir rotateClockwise() {
        return rotate(true, 1);
    }

    @Deprecated
    public Dir rotateCounterClockwise() {
        return rotate(false, 1);
    }

    public Dir opposite() {
        return opposite[this.ordinal()];
    }
}
