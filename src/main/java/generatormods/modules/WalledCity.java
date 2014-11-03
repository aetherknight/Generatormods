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

import generatormods.builders.UndergroundCityBuilder;
import generatormods.builders.WalledCityBuilder;
import generatormods.config.WalledCityConfig;
import generatormods.config.templates.TemplateLoader;
import generatormods.config.templates.TemplateWall;
import generatormods.walledcity.CityDataManager;
import generatormods.walledcity.WalledCityChatHandler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.world.World;

import static generatormods.util.WorldUtil.IGNORE_WATER;
import static generatormods.util.WorldUtil.WORLD_MAX_Y;
import static generatormods.util.WorldUtil.findSurfaceJ;

/**
 * Main for the Walled City Mod. It loads configuration and templates, keeps track of city
 * locations, and it starts building walled cities and underground cities.
 */
public class WalledCity extends AbstractModule {
	public static WalledCity instance;

    public final static int MAX_FOG_HEIGHT = 27;
	public final static int CITY_TYPE_UNDERGROUND = 1;//TheEnd dimension id, since we don't generate there

    public List<TemplateWall> surfaceCityStyles = new ArrayList<TemplateWall>();
    public List<TemplateWall> undergroundCityStyles = new ArrayList<TemplateWall>();
    public WalledCityChatHandler chatHandler;
    public CityDataManager cityDataManager;
    public WalledCityConfig config;

    public WalledCity(String parentModName, File configDir, File jarFile) {
        super(parentModName, configDir, jarFile);
    }

    @Override
    public final void generate(World world, Random random, int i, int k) {
        if (surfaceCityStyles.size() > 0
                && cityDataManager.isCitySeparated(world, i, k, world.provider.dimensionId)
                && random.nextFloat() < config.getGlobalFrequency()) {
            WalledCityBuilder wcb =
                    new WalledCityBuilder(world, random, i, k, config.getTriesPerChunk(),
                            config.getGlobalFrequency(), logger, config.getChestConfigs(),
                            chatHandler, cityDataManager, surfaceCityStyles,
                            config.getRejectOnPreexistingArtifacts());
            wcb.run();
        }
        if (undergroundCityStyles.size() > 0 && cityDataManager.isCitySeparated(world, i, k, CITY_TYPE_UNDERGROUND)
                && random.nextFloat() < config.getUndergroundGlobalFrequency()) {
            UndergroundCityBuilder wgt =
                    new UndergroundCityBuilder(world, random, i, k, config.getTriesPerChunk(),
                            config.getUndergroundGlobalFrequency(), logger,
                            config.getChestConfigs(), chatHandler, cityDataManager,
                            undergroundCityStyles);
            // 44 at sea level
            int maxSpawnHeight =
                    findSurfaceJ(world, i, k, WORLD_MAX_Y, false, IGNORE_WATER)
                            - UndergroundCityBuilder.MAX_DIAM / 2 - 5;
            // 34, a pretty thin margin. Too thin for underocean cities?
            int minSpawnHeight = MAX_FOG_HEIGHT + UndergroundCityBuilder.MAX_DIAM / 2 - 8;
            if (minSpawnHeight <= maxSpawnHeight)
                wgt.setSpawnHeight(minSpawnHeight, maxSpawnHeight, false);
            wgt.run();
        }
    }

    private void loadTemplates() throws Exception {
        TemplateLoader tl = new TemplateLoader(logger, jarFile, configDir);
        tl.extractTemplatesFromJar("walledcity");
        List<TemplateWall> allCityStyles = tl.loadWallStylesAndStreets("walledcity");
        for (TemplateWall cityStyle : allCityStyles) {
            if (cityStyle.underground) {
                // underground cities lack outer walls, so its wall style should instead be a street
                // style
                cityStyle.streets.add(cityStyle);
                undergroundCityStyles.add(cityStyle);
            } else {
                // surface walled city
                surfaceCityStyles.add(cityStyle);
            }
        }
        logger.info("Template loading complete.");
    }

    public final void loadConfiguration() {
		try {
            logger.info("Loading config and templates for WalledCity");

            config = new WalledCityConfig(configDir, logger);
            config.initialize();
            allowedDimensions = config.getAllowedDimensions();

            loadTemplates();

            cityDataManager =
                    new CityDataManager(logger, config.getUndergroundMinCitySeparation(),
                            config.getMinCitySeparation());
            chatHandler = new WalledCityChatHandler(config.getCityBuiltMessage());

            logger.info(
                    "Probability of surface city generation attempt per chunk explored is {}, with {} tries per chunk.",
                    config.getGlobalFrequency(), config.getTriesPerChunk());
            logger.info(
                    "Probability of underground city generation attempt per chunk explored is {}",
                    config.getUndergroundGlobalFrequency());
		} catch (Exception e) {
            disable("Ran into an error while loading configuration", e);
		}
        if (config.getGlobalFrequency() < 0.000001
                && config.getUndergroundGlobalFrequency() < 0.000001) {
            disable("both surface city and underground frequencies are below 0.000001");
        }
	}

	@Override
	public String toString() {
        return "WalledCity";
	}

	@Override
	public void updateWorldExplored(World world) {
		super.updateWorldExplored(world);
		try {
            cityDataManager.updateWorldExplored(world);
		} catch (IOException e) {
            logger.warn(e);
		}
	}
}
