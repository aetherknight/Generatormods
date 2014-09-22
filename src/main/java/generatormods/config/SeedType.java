/* Source code for the The Great Wall Mod and Walled City Generator Mods for the game Minecraft
 * Copyright (C) 2011 by formivore
 * Copyright (C) 2013-2014 by GotoLink
 * Copyright (C) 2014 by William (B.J.) Snow Orvis (aetherknight)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package generatormods.config;

import net.minecraft.util.WeightedRandom;

public enum SeedType {
    SYMMETRIC_SEED, LINEAR_SEED, CIRCULAR_SEED, CRUCIFORM_SEED;

    public static class Weighted extends WeightedRandom.Item {
        private SeedType seedType;

        public Weighted(SeedType seedType, int weight) {
            super(weight);
            this.seedType = seedType;
        }

        public SeedType getSeedType() {
            return seedType;
        }
    }
}
