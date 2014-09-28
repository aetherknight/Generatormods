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
package generatormods.greatwall.config;

import generatormods.config.AbstractConfig;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

import org.apache.logging.log4j.Logger;

public class GreatWallConfig extends AbstractConfig {
    public float curveBias = 0.5F;
    public int lengthBiasNorm = 200;
    public int backtrackLength = 9;

    public GreatWallConfig(File configDir, Logger logger) {
        super(configDir, "GreatWall", logger);
    }

    /**
     * Create or read a Forge-style config file.
     */
    public void initialize() {
        Configuration config = new Configuration(configFile);

        String section = "CARuins";

        initCommonConfig(config, section);
        initChestConfigs(config, section);
        initGreatWallConfig(config, section);

        if (config.hasChanged())
            config.save();
    }

    private void initGreatWallConfig(Configuration config, String section) {
        curveBias =
                (float) config
                        .get(section,
                                "Curve Bias",
                                0.5,
                                "Strength of the bias towards curvier walls. Value should be between 0.0 and\n1.0.",
                                0.0, 1.0).getDouble();
        lengthBiasNorm =
                config.get(section, "Length Bias Norm", 200,
                        "Wall length at which there is no penalty for generation").getInt();

        backtrackLength =
                config.get(section, "Backtrack Length", 0,
                        "Length of backtracking for wall planning if a dead end is hit").getInt();
    }
}
