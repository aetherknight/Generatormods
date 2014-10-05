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
package generatormods.caruins.config;

import generatormods.BuildingCellularAutomaton;
import generatormods.TemplateRule;
import generatormods.config.AbstractConfig;
import generatormods.config.ParseError;

import java.io.File;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.config.Configuration;

import org.apache.logging.log4j.Logger;

public class CARuinsConfig extends AbstractConfig{
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

    public int minHeight;
    public int maxHeight;
    public int minHeightBeforeOscillation;
    public boolean smoothWithStairs;
    public boolean makeFloors;
    public int containerWidth;
    public int containerLength;

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

    public CARuinsConfig(File configDir, Logger logger) {
        super(configDir, "CARuins", logger);
    }

    /**
     * Create or read a Forge-style config file.
     */
    public void initialize() {
        Configuration config = new Configuration(configFile);

        String section = "CARuins";

        initCommonConfig(config, section);

        initCARuinsConfig(config, section);
        initSeedWeights(config, section);
        initSpawnerRules(config, section);
        initBlockRules(config, section);
        initCARules(config, section);

        if (config.hasChanged())
            config.save();
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
