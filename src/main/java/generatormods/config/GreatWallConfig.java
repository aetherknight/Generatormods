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
package generatormods.config;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

import org.apache.logging.log4j.Logger;

public class GreatWallConfig extends AbstractConfig {
    private double curveBias;
    private int lengthBiasNorm;
    private int backtrackLength;

    public GreatWallConfig(File configDir, Logger logger) {
        super(configDir, "GreatWall", logger);
    }

    /**
     * Create or read a Forge-style config file.
     */
    public void initialize() {
        Configuration config = new Configuration(configFile);

        String section = "GreatWall";

        initCommonConfig(config, section);

        initGreatWallConfig(config, section);

        if (config.hasChanged())
            config.save();
    }

    private void initGreatWallConfig(Configuration config, String section) {
        curveBias =
                config.get(
                        section,
                        "Curve Bias",
                        0.5,
                        "Strength of the bias towards curvier walls. Value should be between 0.0 and\n1.0.",
                        0.0, 1.0).getDouble();
        lengthBiasNorm =
                config.get(section, "Length Bias Norm", 200,
                        "Wall length at which there is no penalty for generation").getInt();

        backtrackLength =
                config.get(section, "Backtrack Length", 9,
                        "Length of backtracking for wall planning if a dead end is hit").getInt();
    }

    public double getCurveBias() {
        return curveBias;
    }

    public int getLengthBiasNorm() {
        return lengthBiasNorm;
    }

    public int getBacktrackLength() {
        return backtrackLength;
    }
}
