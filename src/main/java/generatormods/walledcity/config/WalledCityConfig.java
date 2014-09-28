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
package generatormods.walledcity.config;

import generatormods.config.AbstractConfig;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

import org.apache.logging.log4j.Logger;

public class WalledCityConfig extends AbstractConfig {
    public float undergroundGlobalFrequency = 0.015F;
    public int minCitySeparation = 500;
    public int undergroundMinCitySeparation = 500;
    public boolean cityBuiltMessage = false;
    public int backtrackLength = 9;
    public boolean rejectOnPreexistingArtifacts = true;

    public WalledCityConfig(File configDir, Logger logger) {
        super(configDir, "WalledCity", logger);
    }

    /**
     * Create or read a Forge-style config file.
     */
    public void initialize() {
        Configuration config = new Configuration(configFile);

        String section = "CARuins";

        initCommonConfig(config, section);

        initWalledCityConfig(config, section);

        if (config.hasChanged())
            config.save();
    }

    private void initWalledCityConfig(Configuration config, String section) {
        undergroundGlobalFrequency =
                (float) config
                        .get(section,
                                "Underground City Global Frequency",
                                0.015,
                                "Controls how likely belowground cities are to appear. Should be between 0.0 and\n1.0. Lower to make less common.",
                                0.0, 1.0).getDouble();
        undergroundMinCitySeparation =
                config.get(section, "Underground City Minimum Separation", 500,
                        "The minimum allowable separation, in blocks, between underground city spawns.")
                        .getInt();

        minCitySeparation =
                config.get(section, "City Minimum Separation", 500,
                        "The minimum allowable separation, in blocks, between surface city spawns.")
                        .getInt();

        cityBuiltMessage =
                config.get(section, "City Built Message", false,
                        "Whether players receive message when a city is building. Set to true to receive\nmessage.")
                        .getBoolean();

        backtrackLength =
                config.get(section, "Backtrack Length", 9,
                        "Length of backtracking for wall planning if a dead end is hit").getInt();

        rejectOnPreexistingArtifacts =
                config.get(
                        section,
                        "Reject On Preexisting Artifacts",
                        true,
                        "Determines whether the planner rejects city sites that contain preexisting\nman-made blocks. Set to true to do this check.")
                        .getBoolean();
    }
}
