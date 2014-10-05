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
package generatormods.caruins.seeds;

import generatormods.BuildingCellularAutomaton;
import generatormods.common.Shape;

import java.util.Random;

public class SymmetricSeed implements ISeed {
    private final static int SYMMETRIC_SEED_MIN_WIDTH = 4;

    private int maxWidth;
    private float seedDensity;

    public SymmetricSeed(int maxWidth, float seedDensity) {
        this.maxWidth = maxWidth;
        this.seedDensity = seedDensity;
    }

    public byte[][] makeSeed(Random random) {
        maxWidth =
                random.nextInt(random.nextInt(Math.max(1, maxWidth - SYMMETRIC_SEED_MIN_WIDTH)) + 1) + 1;
        int width = random.nextInt(random.nextInt(maxWidth) + 1) + SYMMETRIC_SEED_MIN_WIDTH, length =
                random.nextInt(random.nextInt(maxWidth) + 1) + SYMMETRIC_SEED_MIN_WIDTH;
        byte[][] seed = new byte[width][length];
        int diam = Math.min(Math.max(width, length), Shape.MAX_SPHERE_DIAM);
        for (int x = 0; x < (width + 1) / 2; x++) {
            for (int y = 0; y < (length + 1) / 2; y++) {
                // use a circular mask to avoid ugly corners
                seed[x][y] =
                        (Shape.CIRCLE_SHAPE[diam][x][y] >= 0 && random.nextFloat() < seedDensity) ? BuildingCellularAutomaton.ALIVE
                                : BuildingCellularAutomaton.DEAD;
                seed[width - x - 1][y] = seed[x][y];
                seed[x][length - y - 1] = seed[x][y];
                seed[width - x - 1][length - y - 1] = seed[x][y];
            }
        }
        return seed;
    }
}
