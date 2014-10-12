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

import java.util.HashMap;
import java.util.Map;

import net.minecraftforge.common.config.Configuration;

import org.apache.logging.log4j.Logger;

/**
 * Handles the shared Chests.cfg configuration file.
 */
public class ChestsConfig {
    protected File configFile;
    protected Logger logger;

    public Map<ChestType, ChestContentsSpec> chestConfigs;

    public ChestsConfig(File configDir, Logger logger) {
        configFile = new File(configDir + "/Chests.cfg");
        this.logger = logger;
    }

    public void initialize() {
        Configuration config = new Configuration(configFile);

        // Chest contents. Grouped by chest type.
        chestConfigs = new HashMap<ChestType, ChestContentsSpec>();
        for (ChestType chestType : ChestType.values()) {
            String section = chestType.toString();

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
            ChestContentsSpec chestConfig =
                    new ChestContentsSpec(chestType, chestTries, rawChestItemArray);
            chestConfigs.put(chestType, chestConfig);
        }

        if (config.hasChanged())
            config.save();
    }
}
