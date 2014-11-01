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
package generatormods.util;

public class IntUtil {
    public static int min(int[] a) {
        int min = Integer.MAX_VALUE;
        for (int i : a)
            min = Math.min(min, i);
        return min;
    }

    public static int max(int[] a) {
        int max = Integer.MIN_VALUE;
        for (int i : a)
            max = Math.max(max, i);
        return max;
    }

    /**
     * Integer modulo (%) that always returns a nonnegative value. <a
     * href="http://docs.oracle.com/javase/specs/jls/se5.0/html/expressions.html#15.17.3">Java's
     * modulo math might return a negative value when the dividend (lhs) is negative</a>. However,
     * when doing array indices, we need negative values to wrap around to positive values.
     */
    public static int nonnegativeModulo(int lhs, int rhs) {
        int res = lhs % rhs;
        if (res < 0)
            res = res + rhs;
        return res;
    }

    /**
     * Integer.signum() variation that allows for wiggle room.
     * <p>
     * Normally, Integer.signum() returns -1 when n is negative, 0 when n is 0, or 1 when n is
     * positive. However, if n is within wiggle (including wiggle) of 0, then n is treated as if it
     * is 0.
     *
     * @param n The integer whose signum is being checked.
     * @param wiggle The amount of wiggle room around which n will be treated as 0.
     *
     * @return 0, -1, or 1 depending on whether n is within wiggle of 0, or if n is negative or
     *         positiv, respectively.
     */
    public static int signum(int n, int wiggle) {
        if (n <= wiggle && -n <= wiggle)
            return 0;
        return n < 0 ? -1 : 1;
    }
}
