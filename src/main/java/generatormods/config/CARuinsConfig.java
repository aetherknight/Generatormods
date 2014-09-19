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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraftforge.common.config.Configuration;

import generatormods.*;

public class CARuinsConfig {
    private static final int[] DEFAULT_DIM_LIST = {-1, 0};

    public static float globalFrequency;
    public static int triesPerChunk;
    public static int[] allowedDimensions;
    public static boolean logActivated;

    public static int minHeight;
    public static int maxHeight;
    public static int minHeightBeforeOscillation;
    public static boolean smoothWithStairs;
    public static boolean makeFloors;
    public static int containerWidth;
    public static int containerLength;

    public static Map<String, ChestContentsConfig> chestConfigs;

    public static float symmetricSeedDensity;
    public static int symmetricSeedWeight;
    public static int linearSeedWeight;
    public static int circularSeedWeight;
    public static int cruciformSeedWeight;

    public CARuinsConfig() {}

    /**
     * Create or read a Forge-style config file.
     */
    public static void initialize(File configDir) {
        File forgeStyleConfigFile = new File(configDir + "/CARuins.cfg");
        Configuration config = new Configuration(forgeStyleConfigFile);

        String section = "CARuins";

        // TODO: turn this into an integer n where 1/n is the odds that a given chunk will attempt
        // to generate a structure.
        globalFrequency =
                (float) config
                        .get(section,
                                "Global Frequency",
                                0.025,
                                "Controls how likely structures are to appear --- it is the probability for trying to make a structure in a given chunk. Should be between 0.0 and 1.0. Smaller values make structures less common.",
                                0.0, 1.0).getDouble();
        triesPerChunk =
                config.get(
                        section,
                        "Tries Per Chunk",
                        1,
                        "Allows multiple attempts to build a structure per chunk. If a chunk is selected for a structure, but that structure is rejected for some reason, then a value greater than 1 will attempt to create another structure. Only set it to larger than 1 if you want very dense generation!",
                        0, 100).getInt();
        allowedDimensions =
                config.get(
                        section,
                        "Allowed Dimensions",
                        DEFAULT_DIM_LIST,
                        "Whitelist of dimension IDs where structures may be gnerated. Default is Nether (-1) and Overworld (0).")
                        .getIntList();
        logActivated =
                config.get(
                        section,
                        "Log Activated",
                        true,
                        "Controls information stored into forge logs. Set to true if you want to report an issue with complete forge logs.")
                        .getBoolean();

        minHeight =
                config.get(section, "Min Height", 20,
                        "The minimum allowed height of the structures", 0, 255).getInt();
        maxHeight =
                config.get(section, "Max Height", 20,
                        "The maximum allowed height of the structures", 0, 255).getInt();
        minHeightBeforeOscillation =
                config.get(
                        section,
                        "Min Height Before Oscillation",
                        12,
                        "Any structures that form oscillators before MaxOscillatorCullStep will be culled.",
                        0, 255).getInt();
        smoothWithStairs =
                config.get(section, "Smooth With Stairs", true,
                        "If set to true, will smooth out ruins by placing extra stair blocks.")
                        .getBoolean();
        makeFloors = config.get(section, "Make Floors", true).getBoolean();
        containerWidth =
                config.get(section, "Container Width", 40, "The width of of bounding rectangle.",
                        0, 4096).getInt();
        containerLength =
                config.get(section, "Container Length", 40,
                        "The length of the bounding rectangle.", 0, 4096).getInt();

        // Chest contents. Grouped by chest type.
        chestConfigs = new HashMap<String, ChestContentsConfig>();
        for (int l = 0; l < Building.CHEST_TYPE_LABELS.length; l++) {
            String chestType = Building.CHEST_TYPE_LABELS[l];
            section = "CARuins.CHEST_" + chestType;

            int chestTries =
                    config.get(section, "Tries", Building.DEFAULT_CHEST_TRIES[l],
                            "The number of selections that will be made for this chest type.")
                            .getInt();
            String[] rawChestItemArray =
                    config.get(
                            section,
                            "Chest Contents",
                            defaultChestItems(l),
                            "Format for each item is: <item name>-<metadata>,<selection weight>,<min stack size>,<max stack size>. E.g. minecraft:arrow-0,2,1,12 means a stack of between 1 and 12 arrows, with a selection weight of 2.")
                            .getStringList();

            // Create a list of ChestItemSpecs from the config
            List<String> rawChestItemList = Arrays.asList(rawChestItemArray);
            List<ChestItemSpec> chestItemList = new ArrayList<ChestItemSpec>();
            for (String chestItemSpecString : rawChestItemList) {
                chestItemList.add(new ChestItemSpec(chestItemSpecString));
            }
            ChestContentsConfig chestConfig =
                    new ChestContentsConfig(chestType, chestTries, chestItemList);
            chestConfigs.put(chestType, chestConfig);
        }

        section = "CARuins.SeedWeights";
        symmetricSeedDensity =
                (float) config.get(section, "Symmetric Seed Density", 0.5,
                        "The density (out of 1.0) of live blocks in the symmetric seed.", 0.0, 1.0)
                        .getDouble();
        symmetricSeedWeight =
                config.get(
                        section,
                        "Symmetric Seed Weight",
                        8,
                        "Seed type weights are the relative likelihood weights that different seeds will be used. Weights are nonnegative integers.",
                        0, 4096).getInt();
        linearSeedWeight =
                config.get(
                        section,
                        "Linear Seed Weight",
                        2,
                        "Seed type weights are the relative likelihood weights that different seeds will be used. Weights are nonnegative integers.",
                        0, 4096).getInt();
        circularSeedWeight =
                config.get(
                        section,
                        "Circular Seed Weight",
                        2,
                        "Seed type weights are the relative likelihood weights that different seeds will be used. Weights are nonnegative integers.",
                        0, 4096).getInt();
        cruciformSeedWeight =
                config.get(
                        section,
                        "Cruciform Seed Weight",
                        1,
                        "Seed type weights are the relative likelihood weights that different seeds will be used. Weights are nonnegative integers.",
                        0, 4096).getInt();

        if (config.hasChanged())
            config.save();
    }

    private static String[] defaultChestItems(int chestTypeIndex) {
        // Build an array of the chest item strings
        String[] chestItems = new String[Building.DEFAULT_CHEST_ITEMS[chestTypeIndex].length];
        for (int m = 0; m < Building.DEFAULT_CHEST_ITEMS[chestTypeIndex].length; m++) {
            Object[] chestItem = Building.DEFAULT_CHEST_ITEMS[chestTypeIndex][m];
            chestItems[m] =
                    (new ChestItemSpec(chestItem[1], (Integer) chestItem[2],
                            (Integer) chestItem[3], (Integer) chestItem[4], (Integer) chestItem[5]))
                            .toSpecString();
        }
        return chestItems;
    }
}
