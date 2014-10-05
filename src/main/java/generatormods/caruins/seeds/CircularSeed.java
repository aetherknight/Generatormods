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

import generatormods.Building;
import generatormods.BuildingCellularAutomaton;

import java.util.Random;

public class CircularSeed implements ISeed {
    private final static int CIRCULAR_SEED_MIN_WIDTH = 4;

    private int maxWidth;

    public CircularSeed(int maxWidth) {
        this.maxWidth = maxWidth;
    }

    public byte[][] makeSeed(Random random) {
        int diam =
                Math.min(
                        random.nextInt(random.nextInt(random.nextInt(Math.max(1, maxWidth
                                - CIRCULAR_SEED_MIN_WIDTH)) + 1) + 1)
                                + CIRCULAR_SEED_MIN_WIDTH, Building.MAX_SPHERE_DIAM);
        byte[][] seed = new byte[diam][diam];
        for (int x = 0; x < diam; x++)
            for (int y = 0; y < diam; y++)
                seed[x][y] =
                        Building.CIRCLE_SHAPE[diam][x][y] == 1 ? BuildingCellularAutomaton.ALIVE
                                : BuildingCellularAutomaton.DEAD;
        return seed;
    }
}
