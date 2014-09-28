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

import java.io.File;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;

public class SharedConfig {
    protected File configFile;
    protected Logger logger;

    // Defaults to the Nether and the Overworld
    public static final int[] DEFAULT_DIM_LIST = {-1, 0};

    // Config options common to all the mods
    public float globalFrequency;
    public int triesPerChunk;
    public List<Integer> allowedDimensions;

    public Map<ChestType, ChestContentsConfig> chestConfigs;

    public SharedConfig(float globalFrequency, int triesPerChunk, List<Integer> allowedDimensions,
            Map<ChestType, ChestContentsConfig> chestConfigs, Logger logger) {
        this.globalFrequency = globalFrequency;
        this.triesPerChunk = triesPerChunk;
        this.allowedDimensions = allowedDimensions;
        this.chestConfigs = chestConfigs;
        this.logger = logger;
    }
}
