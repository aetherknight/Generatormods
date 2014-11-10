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
package generatormods.modules;

import generatormods.builders.GreatWallBuilder;
import generatormods.config.GreatWallConfig;
import generatormods.config.templates.TemplateLoader;
import generatormods.config.templates.TemplateWall;

import java.io.File;
import java.util.List;
import java.util.Random;

import net.minecraft.world.World;

/**
 * Main class for the Great Wall Mod. It laods configuration and templates, and it starts building
 * great walls.
 */
public class GreatWall extends AbstractModule {
	public static GreatWall instance;

    public List<TemplateWall> wallStyles;
    public GreatWallConfig config;

    public GreatWall(String parentModName, File configDir, File jarFile) {
        super(parentModName, configDir, jarFile);
    }

    public final void loadConfiguration() {
		try {
            logger.info("Loading config and templates for GreatWall");

            config = new GreatWallConfig(configDir, logger);
            config.initialize();
            allowedDimensions = config.getAllowedDimensions();

            TemplateLoader tl = new TemplateLoader(logger, jarFile, configDir);
            tl.extractTemplatesFromJar("greatwall");
            wallStyles = tl.loadWallStyles("greatwall");
            logger.info("Template loading complete.");

            logger.info(
                    "Probability of wall generation attempt per chunk explored is {}, with {} tries per chunk.",
                    config.getGlobalFrequency(), config.getTriesPerChunk());
		} catch (Exception e) {
            disable("Ran into an error while loading configuration", e);
		}
        if (config.getGlobalFrequency() < 0.000001)
            disable("global frequency is less than 0.000001");
	}

	@Override
	public final void generate(World world, Random random, int i, int k) {
        if (random.nextFloat() < config.getGlobalFrequency())
            (new GreatWallBuilder(world, random, i, k, config.getTriesPerChunk(),
                    config.getGlobalFrequency(), logger, config.getChestConfigs(), wallStyles,
                    config.getCurveBias())).run();
	}

	@Override
	public String toString() {
        return "GreatWall";
	}
}
