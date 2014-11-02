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
package generatormods;

import generatormods.config.CARuinsConfig;
import generatormods.gen.WorldGenCARuins;

import org.apache.logging.log4j.LogManager;

import java.util.Random;

import net.minecraft.world.World;

/**
 * Main class that hooks into Forge for CARuins. It loads configuration and
 * sets up the world generation it adds.
 */
public class PopulatorCARuins extends BuildingExplorationHandler {
	public static PopulatorCARuins instance;

    public CARuinsConfig config;

    public PopulatorCARuins(String parentModName) {
        this.logger = LogManager.getLogger(parentModName + "." + this.toString());
    }

    public final void loadConfiguration() {
		try {
            logger.info("Loading config for CARuins");

            config = new CARuinsConfig(CONFIG_DIRECTORY, logger);
            config.initialize();
            allowedDimensions = config.getAllowedDimensions();

            logger.info(
                    "Probability of CARuin generation attempt per chunk explored is {}, with {} tries per chunk.",
                    config.getGlobalFrequency(), config.getTriesPerChunk());
		} catch (Exception e) {
            disable("Ran into an error while loading configuration", e);
		}
        if (config.getGlobalFrequency() < 0.000001)
            disable("global frequency is less than 0.000001");
        if (config.caRules == null || config.caRules.size() == 0)
            disable("no CA Rules loaded");
	}

	@Override
	public final void generate(World world, Random random, int i, int k) {
        if (random.nextFloat() < config.getGlobalFrequency())
            (new WorldGenCARuins(world, random, i, k, logger, config)).run();
	}

	@Override
	public String toString() {
        return "CARuins";
	}
}
