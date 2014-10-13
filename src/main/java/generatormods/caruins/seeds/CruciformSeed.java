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

import generatormods.buildings.BuildingCellularAutomaton;

import java.util.Random;

public class CruciformSeed implements ISeed {
    private int maxWidth;

    public CruciformSeed(int maxWidth) {
        this.maxWidth = maxWidth;
    }

    public byte[][] makeSeed(Random random) {
        if (maxWidth <= 1)
            return new byte[][] {{BuildingCellularAutomaton.ALIVE}}; // degenerate case
        // width and length are always odd
        int width = 2 * (random.nextInt(random.nextInt(maxWidth / 2) + 1) + 1) + 1;
        int length = 2 * (random.nextInt(random.nextInt(maxWidth / 2) + 1) + 1) + 1;
        byte[][] seed = new byte[width][length];
        for (int x = 0; x < width; x++)
            for (int y = 0; y < length; y++)
                seed[x][y] =
                        (x == width / 2 || y == length / 2) ? BuildingCellularAutomaton.ALIVE
                                : BuildingCellularAutomaton.DEAD;
        return seed;
    }
}
