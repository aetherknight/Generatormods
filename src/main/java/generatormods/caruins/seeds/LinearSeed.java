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

public class LinearSeed implements ISeed {
    private int maxWidth;

    public LinearSeed(int maxWidth) {
        this.maxWidth = maxWidth;
    }

    public byte[][] makeSeed(Random random) {
        if (maxWidth <= 1)
            return new byte[][] { { BuildingCellularAutomaton.ALIVE } }; //degenerate case
        int width = random.nextInt(random.nextInt(maxWidth - 1) + 1) + 2; //random number in (2,maxWidth) inclusive, concentrated towards low end
        byte[][] seed = new byte[width][1];
        for (int x = 0; x < width; x++)
            seed[x][0] = BuildingCellularAutomaton.ALIVE;
        return seed;
    }
}

