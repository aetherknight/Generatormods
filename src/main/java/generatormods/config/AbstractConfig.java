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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraftforge.common.config.Configuration;

import org.apache.logging.log4j.Logger;

public abstract class AbstractConfig {
    protected File configFile;
    protected Logger logger;

    // Defaults to the Nether and the Overworld
    private static final int[] DEFAULT_DIM_LIST = {-1, 0};

    // Config options common to all the mods
    public float globalFrequency;
    public int triesPerChunk;
    public List<Integer> allowedDimensions;

    public Map<ChestType, ChestContentsConfig> chestConfigs;

    // sharedConfig provides a common way to provide type-safe access to the
    // global configs across all 3 mods without having to pass the mod
    // instances around everywhere as the bearer of configuration.
    public SharedConfig sharedConfig;

    public AbstractConfig(File configDir, String configName, Logger logger) {
        configFile = new File(configDir + "/" + configName + ".cfg");
        this.logger = logger;
    }

    protected void initCommonConfig(Configuration config, String section) {
        // TODO: turn this into an integer n where 1/n is the odds that a given chunk will attempt
        // to generate a structure.
        globalFrequency =
                (float) config
                        .get(section,
                                "Global Frequency",
                                0.025,
                                "Controls how likely structures are to appear --- it is the probability for\ntrying to make a structure in a given chunk. Should be between 0.0 and 1.0.\nSmaller values make structures less common.",
                                0.0, 1.0).getDouble();
        triesPerChunk =
                config.get(
                        section,
                        "Tries Per Chunk",
                        1,
                        "Allows multiple attempts to build a structure per chunk. If a chunk is selected\nfor a structure, but that structure is rejected for some reason, then a value\ngreater than 1 will attempt to create another structure. Only set it to larger\nthan 1 if you want very dense generation!",
                        0, 100).getInt();
        int[] rawAllowedDimensions =
                config.get(
                        section,
                        "Allowed Dimensions",
                        DEFAULT_DIM_LIST,
                        "Whitelist of dimension IDs where structures may be gnerated. Default is Nether\n(-1) and Overworld (0).")
                        .getIntList();
        allowedDimensions = new ArrayList<Integer>();
        for (int dimensionInt : rawAllowedDimensions) {
            allowedDimensions.add(dimensionInt);
        }

        initChestConfigs(config, section);

        sharedConfig =
                new SharedConfig(globalFrequency, triesPerChunk, allowedDimensions, chestConfigs,
                        logger);
    }

    private void initChestConfigs(Configuration config, String baseSection) {
        // Chest contents. Grouped by chest type.
        chestConfigs = new HashMap<ChestType, ChestContentsConfig>();
        for (ChestType chestType : ChestType.values()) {
            String section = baseSection + ".ChestContents." + chestType;

            String[] defaultChestItems = new String[chestType.getDefaultChestItems().size()];
            for (int i = 0; i < defaultChestItems.length; i++) {
                defaultChestItems[i] = chestType.getDefaultChestItems().get(i).toSpecString();
            }

            int chestTries =
                    config.get(section, "Tries", chestType.getDefaultChestTries(),
                            "The number of selections that will be made for this chest type.", 0,
                            100).getInt();
            String[] rawChestItemArray =
                    config.get(
                            section,
                            "Chest Contents",
                            defaultChestItems,
                            "Format for each item is:\n\n    <item name>-<metadata>,<selection weight>,<min stack size>,<max stack size>\n\nE.g.:\n\n    minecraft:arrow-0,2,1,12\n\nMeans a stack of between 1 and 12 arrows, with a selection weight of 2.")
                            .getStringList();

            // Create a list of ChestItemSpecs from the config
            ChestContentsConfig chestConfig =
                    new ChestContentsConfig(chestType, chestTries, rawChestItemArray);
            chestConfigs.put(chestType, chestConfig);
        }
    }
}
