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
package generatormods.common.config;

import java.io.File;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraftforge.common.config.Configuration;

import org.apache.logging.log4j.Logger;

public abstract class AbstractConfig {
    protected File configFile;
    protected File configDir;
    protected Logger logger;

    // Defaults to the Nether and the Overworld
    private static final int[] DEFAULT_DIM_LIST = {-1, 0};

    // Config options common to all the mods
    private double globalFrequency;
    private int triesPerChunk;
    private List<Integer> allowedDimensions;
    private Map<ChestType, ChestContentsSpec> chestConfigs;

    public AbstractConfig(File configDir, String configName, Logger logger) {
        this.configDir = configDir;
        configFile = new File(configDir + "/" + configName + ".cfg");
        this.logger = logger;
    }

    protected void initCommonConfig(Configuration config, String section) {
        // TODO: turn this into an integer n where 1/n is the odds that a given chunk will attempt
        // to generate a structure.
        globalFrequency =
                config.get(
                        section,
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

        initChestConfigs();
    }

    private void initChestConfigs() {
        ChestsConfig cc = new ChestsConfig(configDir, logger);
        cc.initialize();
        chestConfigs = cc.chestConfigs;
    }

    public Logger getLogger() {
        return logger;
    }

    public double getGlobalFrequency() {
        return globalFrequency;
    }

    public int getTriesPerChunk() {
        return triesPerChunk;
    }

    public List<Integer> getAllowedDimensions() {
        return allowedDimensions;
    }

    public Map<ChestType, ChestContentsSpec> getChestConfigs() {
        return chestConfigs;
    }
}
