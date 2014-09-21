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
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.init.Blocks;
import net.minecraft.block.Block;

import generatormods.TemplateRule;
import generatormods.Building;
import generatormods.BuildingCellularAutomaton;

public class CARuinsConfig {
    // Defaults to the Nether and the Overworld
    private static final int[] DEFAULT_DIM_LIST = {-1, 0};
    // Default CARuins template is 1/3 cobblestone, 2/3 mossy cobblestone.
    private final static TemplateRule DEFAULT_TEMPLATE = new TemplateRule(new Block[] {
            Blocks.cobblestone, Blocks.mossy_cobblestone, Blocks.mossy_cobblestone}, new int[] {0,
            0, 0}, 100);
    // Customize the default templates per-biome. We track it here by biome ID.
    private static TemplateRule[] DEFAULT_BLOCK_RULES = new TemplateRule[BiomeGenBase
            .getBiomeGenArray().length];
    static {
        DEFAULT_BLOCK_RULES[0] = DEFAULT_TEMPLATE; // Underground, unused
        DEFAULT_BLOCK_RULES[1] = DEFAULT_TEMPLATE; // Ocean
        DEFAULT_BLOCK_RULES[2] =
                new TemplateRule(new Block[] {Blocks.stone, Blocks.stonebrick, Blocks.stonebrick},
                        new int[] {0, 1, 2}, 100); // Plains
        DEFAULT_BLOCK_RULES[3] = new TemplateRule(Blocks.sandstone, 0, 100); // Desert
        DEFAULT_BLOCK_RULES[4] =
                new TemplateRule(new Block[] {Blocks.stone, Blocks.stonebrick, Blocks.stonebrick},
                        new int[] {0, 0, 2}, 100); // Hills
        DEFAULT_BLOCK_RULES[5] = DEFAULT_TEMPLATE; // Forest
        DEFAULT_BLOCK_RULES[6] = DEFAULT_TEMPLATE; // Taiga
        DEFAULT_BLOCK_RULES[7] = DEFAULT_TEMPLATE; // Swampland
        DEFAULT_BLOCK_RULES[8] = DEFAULT_TEMPLATE; // River
        DEFAULT_BLOCK_RULES[9] = new TemplateRule(Blocks.nether_brick, 0, 100);// Nether
        DEFAULT_BLOCK_RULES[10] = new TemplateRule(Blocks.end_stone, 0, 100); // Sky
        DEFAULT_BLOCK_RULES[11] =
                new TemplateRule(new Block[] {Blocks.ice, Blocks.snow, Blocks.stonebrick},
                        new int[] {0, 0, 2}, 100); // FrozenOcean
        DEFAULT_BLOCK_RULES[12] =
                new TemplateRule(new Block[] {Blocks.ice, Blocks.snow, Blocks.stonebrick},
                        new int[] {0, 0, 2}, 100); // FrozenRiver
        DEFAULT_BLOCK_RULES[13] =
                new TemplateRule(new Block[] {Blocks.snow, Blocks.stonebrick, Blocks.stonebrick},
                        new int[] {0, 2, 2}, 100); // IcePlains
        DEFAULT_BLOCK_RULES[14] =
                new TemplateRule(new Block[] {Blocks.snow, Blocks.stonebrick, Blocks.stonebrick},
                        new int[] {0, 2, 2}, 100); // IceMountains
        DEFAULT_BLOCK_RULES[15] = DEFAULT_TEMPLATE; // MushroomIsland
        DEFAULT_BLOCK_RULES[16] = DEFAULT_TEMPLATE; // Shore
        DEFAULT_BLOCK_RULES[17] = DEFAULT_TEMPLATE; // Beach
        DEFAULT_BLOCK_RULES[18] = new TemplateRule(Blocks.sandstone, 0, 100); // DesertHills
        DEFAULT_BLOCK_RULES[19] = DEFAULT_TEMPLATE; // ForestHills
        DEFAULT_BLOCK_RULES[20] = DEFAULT_TEMPLATE; // TaigaHills
        DEFAULT_BLOCK_RULES[21] =
                new TemplateRule(new Block[] {Blocks.stone, Blocks.stonebrick, Blocks.stonebrick},
                        new int[] {0, 0, 2}, 100);// ExtremeHillsEdge
        for (int i = 22; i < BiomeGenBase.getBiomeGenArray().length; i++) {
            if(DEFAULT_BLOCK_RULES[i] == null) {
                DEFAULT_BLOCK_RULES[i] = DEFAULT_TEMPLATE;
            }
        }
    }

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

    public static TemplateRule mediumLightWideFloorSpawnerRule;
    public static TemplateRule mediumLightNarrowFloorSpawnerRule;
    public static TemplateRule lowLightSpawnerRule;

    public static TemplateRule[] blockRules;

    public CARuinsConfig() {}

    /**
     * Create or read a Forge-style config file.
     */
    public static void initialize(File configDir) {
        File forgeStyleConfigFile = new File(configDir + "/CARuins.cfg");
        Configuration config = new Configuration(forgeStyleConfigFile);

        String section = "CARuins";

        initCommonConfig(config, section);
        initCARuinsConfig(config, section);
        initChestConfigs(config, section);
        initSeedWeights(config, section);
        initSpawnerRules(config, section);
        initBlockRules(config, section);

        if (config.hasChanged())
            config.save();
    }

    private static void initCommonConfig(Configuration config, String section) {
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
    }

    private static void initCARuinsConfig(Configuration config, String section) {
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

    private static void initChestConfigs(Configuration config, String baseSection) {
        // Chest contents. Grouped by chest type.
        chestConfigs = new HashMap<String, ChestContentsConfig>();
        for (int l = 0; l < Building.CHEST_TYPE_LABELS.length; l++) {
            String chestType = Building.CHEST_TYPE_LABELS[l];
            String section = baseSection + ".CHEST_" + chestType;

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
    }

    private static void initSeedWeights(Configuration config, String baseSection) {
        String section = baseSection + ".SeedWeights";

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
    }

    /**
     * Attempts to get and parse a given spawner rule.
     *
     * If it fails to parse, it reverts to the default rule.
     */
    private static TemplateRule getSpawnerRule(Configuration config, String section,
            String ruleName, TemplateRule defaultRule) {
        String rawRule = config.get(section, ruleName, defaultRule.toString()).getString();
        try {
            return new TemplateRule(rawRule, false);
        } catch (Exception e) {
            // TODO: log the error
            return defaultRule;
        }
    }

    private static void initSpawnerRules(Configuration config, String baseSection) {
        String section = baseSection + ".SpawnerRules";

        config.setCategoryComment(
                section,
                "These spawner rule variables control what spawners will be used depending on the light level and floor width.");

        mediumLightNarrowFloorSpawnerRule =
                getSpawnerRule(config, section, "MediumLightNarrowFloorSpawnerRule",
                        BuildingCellularAutomaton.DEFAULT_MEDIUM_LIGHT_NARROW_SPAWNER_RULE);
        mediumLightWideFloorSpawnerRule =
                getSpawnerRule(config, section, "MediumLightWideFloorSpawnerRule",
                        BuildingCellularAutomaton.DEFAULT_MEDIUM_LIGHT_WIDE_SPAWNER_RULE);
        lowLightSpawnerRule =
                getSpawnerRule(config, section, "LowLightSpawnerRule",
                        BuildingCellularAutomaton.DEFAULT_LOW_LIGHT_SPAWNER_RULE);
    }

    /**
     * Load a BlockRule or generate a default BlockRule for all biomes.
     */
    private static void initBlockRules(Configuration config, String baseSection) {
        String section = baseSection + ".BlockRules";
        blockRules = new TemplateRule[DEFAULT_BLOCK_RULES.length];

        config.setCategoryComment(
                section,
                "BlockRule is the template rule that controls what blocks the structure will be made out of.\nDefault is:\n   BiomeNameBlockRule:0,100,minecraft:cobblestone-0,minecraft:mossy_cobblestone-0,minecraft:mossy_cobblestone-0\n\nWhich translates into: (special condition) then,(100%=complete) ruin in either normal(1 out of 3 chance) or mossy cobblestone(2 out of 3) in said biome. Metadatas are supported, use blockname-blockmetadata syntax.");

        // We only care about biomes that exist, and at worst we use the
        // default template for a biome. If a given biome ID does not
        // correspond to a biome, then it is ignored.
        for (int i = 0; i < BiomeGenBase.getBiomeGenArray().length; i++) {
            BiomeGenBase currBiome = BiomeGenBase.getBiomeGenArray()[i];
            if (currBiome != null) {
                String rawBlockRule =
                        config.get(section, currBiome.biomeName, DEFAULT_BLOCK_RULES[i].toString(),
                                "" + currBiome.biomeID + " -- " + currBiome.biomeName).getString();
                try {
                    blockRules[i] = new TemplateRule(rawBlockRule, false);
                } catch (Exception e) {
                    // TODO: log the error
                    blockRules[i] = DEFAULT_BLOCK_RULES[i];
                }
            }
        }
    }
}
