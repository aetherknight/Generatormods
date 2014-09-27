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

import generatormods.BuildingCellularAutomaton;
import generatormods.TemplateRule;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.config.Configuration;

import org.apache.logging.log4j.Logger;

public class CARuinsConfig {
    // Defaults to the Nether and the Overworld
    private static final int[] DEFAULT_DIM_LIST = {-1, 0};
    // Default CARuins template is 1/3 cobblestone, 2/3 mossy cobblestone.
    private final static TemplateRule DEFAULT_TEMPLATE = new TemplateRule(new Block[] {
            Blocks.cobblestone, Blocks.mossy_cobblestone, Blocks.mossy_cobblestone}, new int[] {0,
            0, 0}, 100);
    // Customize the default templates per-biome. We track it here by biome ID.
    private static TemplateRule[] DEFAULT_BLOCK_RULES = new TemplateRule[BiomeGenBase
            .getBiomeGenArray().length + 1];
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
        for (int i = 22; i < BiomeGenBase.getBiomeGenArray().length + 1; i++) {
            if (DEFAULT_BLOCK_RULES[i] == null) {
                DEFAULT_BLOCK_RULES[i] = DEFAULT_TEMPLATE;
            }
        }
    }

    public float globalFrequency;
    public int triesPerChunk;
    public List<Integer> allowedDimensions;
    public boolean logActivated;

    public int minHeight;
    public int maxHeight;
    public int minHeightBeforeOscillation;
    public boolean smoothWithStairs;
    public boolean makeFloors;
    public int containerWidth;
    public int containerLength;

    public Map<ChestType, ChestContentsConfig> chestConfigs;

    public float symmetricSeedDensity;
    public int symmetricSeedWeight;
    public int linearSeedWeight;
    public int circularSeedWeight;
    public int cruciformSeedWeight;

    public List<SeedType.Weighted> weightedSeeds;

    public TemplateRule mediumLightWideFloorSpawnerRule;
    public TemplateRule mediumLightNarrowFloorSpawnerRule;
    public TemplateRule lowLightSpawnerRule;

    /**
     * Contains the template rule for a CA Ruin that is generated in a given biome. The indices of
     * this list are 1 greater than the corresponding biomeID. Index 0 is reserved the the generator
     * mods' special "Underground" pseudo-biome.
     */
    public TemplateRule[] blockRules;

    public List<WeightedCARule> caRules;

    private File configFile;
    private Logger logger;

    public CARuinsConfig(File configDir, Logger logger) {
        configFile = new File(configDir + "/CARuins.cfg");
        this.logger = logger;
    }

    /**
     * Create or read a Forge-style config file.
     */
    public void initialize() {
        Configuration config = new Configuration(configFile);

        String section = "CARuins";

        initCommonConfig(config, section);
        initCARuinsConfig(config, section);
        initChestConfigs(config, section);
        initSeedWeights(config, section);
        initSpawnerRules(config, section);
        initBlockRules(config, section);
        initCARules(config, section);

        if (config.hasChanged())
            config.save();
    }

    private void initCommonConfig(Configuration config, String section) {
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

        logActivated =
                config.get(
                        section,
                        "Log Activated",
                        true,
                        "Controls information stored into forge logs. Set to true if you want to report\nan issue with complete forge logs.")
                        .getBoolean();
    }

    private void initCARuinsConfig(Configuration config, String section) {
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
                        "Any structures that form oscillators before MaxOscillatorCullStep will be\nculled.",
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
                            "The number of selections that will be made for this chest type.")
                            .getInt();
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

    private void initSeedWeights(Configuration config, String baseSection) {
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
                        "Seed type weights are the relative likelihood weights that different seeds will\nbe used. Weights are nonnegative integers.",
                        0, 4096).getInt();
        linearSeedWeight =
                config.get(
                        section,
                        "Linear Seed Weight",
                        2,
                        "Seed type weights are the relative likelihood weights that different seeds will\nbe used. Weights are nonnegative integers.",
                        0, 4096).getInt();
        circularSeedWeight =
                config.get(
                        section,
                        "Circular Seed Weight",
                        2,
                        "Seed type weights are the relative likelihood weights that different seeds will\nbe used. Weights are nonnegative integers.",
                        0, 4096).getInt();
        cruciformSeedWeight =
                config.get(
                        section,
                        "Cruciform Seed Weight",
                        1,
                        "Seed type weights are the relative likelihood weights that different seeds will\nbe used. Weights are nonnegative integers.",
                        0, 4096).getInt();

        weightedSeeds = new ArrayList<SeedType.Weighted>();
        weightedSeeds.add(new SeedType.Weighted(SeedType.SYMMETRIC_SEED, symmetricSeedWeight));
        weightedSeeds.add(new SeedType.Weighted(SeedType.LINEAR_SEED, linearSeedWeight));
        weightedSeeds.add(new SeedType.Weighted(SeedType.CIRCULAR_SEED, circularSeedWeight));
        weightedSeeds.add(new SeedType.Weighted(SeedType.CRUCIFORM_SEED, cruciformSeedWeight));
    }

    /**
     * Attempts to get and parse a given spawner rule.
     *
     * If it fails to parse, it reverts to the default rule.
     */
    private TemplateRule getSpawnerRule(Configuration config, String section, String ruleName,
            TemplateRule defaultRule) {
        String rawRule = config.get(section, ruleName, defaultRule.toString()).getString();
        try {
            return new TemplateRule(rawRule, false);
        } catch (Exception e) {
            logger.error("Error parsing the CARuins spawner rule for " + ruleName + ": \""
                    + rawRule + "\". Using defaults instead.", e);
            return defaultRule;
        }
    }

    private void initSpawnerRules(Configuration config, String baseSection) {
        String section = baseSection + ".SpawnerRules";

        config.setCategoryComment(
                section,
                "These spawner rule variables control what spawners will be used depending on\nthe light level and floor width.");

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
    private void initBlockRules(Configuration config, String baseSection) {
        String section = baseSection + ".BlockRules";
        blockRules = new TemplateRule[DEFAULT_BLOCK_RULES.length];

        config.setCategoryComment(
                section,
                "BlockRule is the template rule that controls what blocks the structure will be\nmade out of. Default is:\n\n    S:BiomeName=0,100,minecraft:cobblestone-0,minecraft:mossy_cobblestone-0,minecraft:mossy_cobblestone-0\n\nWhich translates into: (special condition) then,(100%=complete) ruin in either\nnormal(1 out of 3 chance) or mossy cobblestone(2 out of 3) in said biome.\nMetadatas are supported, use blockname-blockmetadata syntax.");

        // We only care about biomes that exist, and at worst we use the
        // default template for a biome. If a given biome ID does not
        // correspond to a biome, then it is ignored.
        //
        // The index into blockRules and into DEFAULT_BLOCK_RULES should be 1
        // greater than the biomeID.
        for (int i = 0; i < BiomeGenBase.getBiomeGenArray().length; i++) {
            BiomeGenBase currBiome = BiomeGenBase.getBiomeGenArray()[i];
            if (currBiome != null) {
                String rawBlockRule =
                        config.get(section, currBiome.biomeName,
                                DEFAULT_BLOCK_RULES[i + 1].toString(),
                                "" + currBiome.biomeID + " -- " + currBiome.biomeName).getString();
                try {
                    blockRules[i + 1] = new TemplateRule(rawBlockRule, false);
                } catch (Exception e) {
                    logger.error("Error parsing the CARuins block rule for " + currBiome.biomeName
                            + ": \"" + rawBlockRule + "\". Using defaults instead.", e);
                    blockRules[i + 1] = DEFAULT_BLOCK_RULES[i + 1];
                }
            }
        }
    }

    private void initCARules(Configuration config, String section) {
        String[] defaultCARules = new String[WeightedCARule.DEFAULT_CA_RULES.size()];
        for (int i = 0; i < defaultCARules.length; i++) {
            defaultCARules[i] = WeightedCARule.DEFAULT_CA_RULES.get(i).toString();
        }

        String[] caRuleStrings =
                config.get(
                        section,
                        "CARules",
                        defaultCARules,
                        "Cellular Automata rules and weights. Each rule is a comma-separated list in the\nfollowing format:\n\n    B3/S23, 5, Life - good for weird temples\n\nThe first value is the Cellular Automata rule, with Birth and survival rule\nnumbers. The second value is the random weight of the rule. The third value is\nan optional comment, which main contain commas.")
                        .getStringList();
        caRules = new ArrayList<WeightedCARule>();
        for (String caRuleString : caRuleStrings) {
            try {
                caRules.add(WeightedCARule.fromString(caRuleString));
            } catch (ParseError e) {
                logger.error("Error parsing CA rule: \"" + caRuleString + "\". Ignoring.", e);
            }
        }
    }
}
