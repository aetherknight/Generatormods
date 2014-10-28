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

public class Shape {
    public final static int MAX_SPHERE_DIAM = 40;
    /* [diam][] */
    public final static int[][] SPHERE_SHAPE = new int[MAX_SPHERE_DIAM + 1][];
    /* [diam][y][x] */
    public final static int[][][] CIRCLE_SHAPE = new int[MAX_SPHERE_DIAM + 1][][];
    /* [diam][y][x] */
    public final static int[][][] CIRCLE_CRENEL = new int[MAX_SPHERE_DIAM + 1][][];

    static {
        for (int diam = 1; diam <= MAX_SPHERE_DIAM; diam++) {
            circleShape(diam);
        }
        // change diam 6 shape to look better
        CIRCLE_SHAPE[6] =
                new int[][] { {-1, -1, 1, 1, -1, -1}, {-1, 1, 0, 0, 1, -1}, {1, 0, 0, 0, 0, 1},
                        {1, 0, 0, 0, 0, 1}, {-1, 1, 0, 0, 1, -1}, {-1, -1, 1, 1, -1, -1}};
        CIRCLE_CRENEL[6] =
                new int[][] { {-1, -1, 1, 0, -1, -1}, {-1, 0, 0, 0, 1, -1}, {1, 0, 0, 0, 0, 0},
                        {0, 0, 0, 0, 0, 1}, {-1, 1, 0, 0, 0, -1}, {-1, -1, 0, 1, -1, -1}};
    }

    private static void circleShape(int diam) {
        float rad = diam / 2.0F;
        float[][] shape_density = new float[diam][diam];
        for (int x = 0; x < diam; x++)
            for (int y = 0; y < diam; y++)
                shape_density[y][x] =
                        ((x + 0.5F - rad) * (x + 0.5F - rad) + (y + 0.5F - rad) * (y + 0.5F - rad))
                                / (rad * rad);
        int[] xheight = new int[diam];
        for (int y = 0; y < diam; y++) {
            int x = 0;
            while (shape_density[y][x] > 1.0F) {
                x++;
            }
            xheight[y] = x;
        }
        CIRCLE_SHAPE[diam] = new int[diam][diam];
        CIRCLE_CRENEL[diam] = new int[diam][diam];
        SPHERE_SHAPE[diam] = new int[(diam + 1) / 2];
        int nextHeight, crenel_adj = 0;
        for (int x = 0; x < diam; x++)
            for (int y = 0; y < diam; y++) {
                CIRCLE_SHAPE[diam][y][x] = 0;
                CIRCLE_CRENEL[diam][y][x] = 0;
            }
        for (int y = 0; y < diam; y++) {
            if (y == 0 || y == diam - 1)
                nextHeight = diam / 2 + 1;
            else
                nextHeight =
                        xheight[y < diam / 2 ? y - 1 : y + 1]
                                + (xheight[y] == xheight[y < diam / 2 ? y - 1 : y + 1] ? 1 : 0);
            if (y > 0 && xheight[y] == xheight[y - 1])
                crenel_adj++;
            int x = 0;
            for (; x < xheight[y]; x++) {
                CIRCLE_SHAPE[diam][y][x] = -1;
                CIRCLE_SHAPE[diam][y][diam - x - 1] = -1;
                CIRCLE_CRENEL[diam][y][x] = -1;
                CIRCLE_CRENEL[diam][y][diam - x - 1] = -1;
            }
            for (; x < nextHeight; x++) {
                CIRCLE_SHAPE[diam][y][x] = 1;
                CIRCLE_SHAPE[diam][y][diam - x - 1] = 1;
                CIRCLE_CRENEL[diam][y][x] = (x + crenel_adj) % 2;
                CIRCLE_CRENEL[diam][y][diam - x - 1] = (x + crenel_adj + diam + 1) % 2;
            }
        }
        for (int y = diam / 2; y < diam; y++)
            SPHERE_SHAPE[diam][y - diam / 2] =
                    (2 * (diam / 2 - xheight[y]) + (diam % 2 == 0 ? 0 : 1));
    }
}
