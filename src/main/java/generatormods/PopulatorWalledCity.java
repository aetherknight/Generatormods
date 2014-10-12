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

import generatormods.common.ModUpdateDetectorWrapper;
import generatormods.walledcity.CityDataManager;
import generatormods.walledcity.WalledCityChatHandler;
import generatormods.walledcity.config.WalledCityConfig;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.village.Village;
import net.minecraft.village.VillageDoorInfo;
import net.minecraft.world.World;

/*
 * PopulatorWalledCity is the main class that hooks into ModLoader for the
 * Walled City Mod. It reads the globalSettings file, keeps track of city
 * locations, and runs WorldGenWalledCitys and WorldGenUndergroundCities.
 */
@Mod(modid = "WalledCityMod", name = "Walled City Generator", version = BuildingExplorationHandler.VERSION, dependencies = "after:ExtraBiomes,BiomesOPlenty", acceptableRemoteVersions = "*")
public class PopulatorWalledCity extends BuildingExplorationHandler {
	@Instance("WalledCityMod")
	public static PopulatorWalledCity instance;

	public final static int MIN_CITY_LENGTH = 40;
	final static int MAX_FOG_HEIGHT = 27;
	public final static int CITY_TYPE_UNDERGROUND = 1;//TheEnd dimension id, since we don't generate there
    private final static String STREET_TEMPLATES_FOLDER_NAME = "streets";
	//DATA VARIABLES
	public List<TemplateWall> cityStyles = null;
    public List<TemplateWall> undergroundCityStyles = new ArrayList<TemplateWall>();
    public WalledCityChatHandler chatHandler;
    public CityDataManager cityDataManager;

    public WalledCityConfig config;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        templateFolderName = "walledcity";
        ModUpdateDetectorWrapper.checkForUpdates(this, event);
    }

	//Load templates after mods have loaded so we can check whether any modded blockIDs are valid
	@EventHandler
	public void modsLoaded(FMLPostInitializationEvent event) {
        loadConfiguration();
        cityDataManager =
                new CityDataManager(logger, config.undergroundMinCitySeparation,
                        config.minCitySeparation);
        chatHandler = new WalledCityChatHandler(config.cityBuiltMessage);
		if (!errFlag) {
			GameRegistry.registerWorldGenerator(this, 0);
		}
	}

    @Override
    public final void generate(World world, Random random, int i, int k) {
        if (cityStyles.size() > 0 && cityDataManager.isCitySeparated(world, i, k, world.provider.dimensionId)
                && random.nextFloat() < config.globalFrequency) {
            (new WorldGenWalledCity(this, world, random, i, k, config.triesPerChunk,
                    config.globalFrequency)).run();
        }
        if (undergroundCityStyles.size() > 0 && cityDataManager.isCitySeparated(world, i, k, CITY_TYPE_UNDERGROUND)
                && random.nextFloat() < config.undergroundGlobalFrequency) {
            WorldGeneratorThread wgt =
                    new WorldGenUndergroundCity(this, world, random, i, k, 1,
                            config.undergroundGlobalFrequency);
            // 44 at sea level
            int maxSpawnHeight =
                    Building.findSurfaceJ(world, i, k, Building.WORLD_MAX_Y, false,
                            Building.IGNORE_WATER) - WorldGenUndergroundCity.MAX_DIAM / 2 - 5;
            // 34, a pretty thin margin. Too thin for underocean cities?
            int minSpawnHeight = MAX_FOG_HEIGHT + WorldGenUndergroundCity.MAX_DIAM / 2 - 8;
            if (minSpawnHeight <= maxSpawnHeight)
                wgt.setSpawnHeight(minSpawnHeight, maxSpawnHeight, false);
            (wgt).run();
        }
    }

    private void loadTemplates() throws Exception {
        File stylesDirectory = new File(CONFIG_DIRECTORY, templateFolderName);
        cityStyles = TemplateWall.loadWallStylesFromDir(stylesDirectory, logger);
        TemplateWall.loadStreets(cityStyles, new File(stylesDirectory, STREET_TEMPLATES_FOLDER_NAME), logger);
        // TODO: does this work? I worry that the remove() screws up indices.
        for (int m = 0; m < cityStyles.size(); m++) {
            if (cityStyles.get(m).underground) {
                TemplateWall uws = cityStyles.remove(m);
                uws.streets.add(uws); //underground cities have no outer walls, so this should be a street style
                undergroundCityStyles.add(uws);
                m--;
            }
        }
        logger.info("Template loading complete.");
    }

    private final void loadConfiguration() {
		try {
            logger.info("Loading options and templates for the Walled City Generator.");

            config = new WalledCityConfig(CONFIG_DIRECTORY, logger);
            config.initialize();
            sharedConfig = config.sharedConfig;

            loadTemplates();

            logger.info("Probability of city generation attempt per chunk explored is " + sharedConfig.globalFrequency + ", with " + sharedConfig.triesPerChunk + " tries per chunk.");
		} catch (Exception e) {
			errFlag = true;
            logger.fatal("There was a problem loading the walled city mod", e);
		}
		if (config.globalFrequency < 0.000001 && config.undergroundGlobalFrequency < 0.000001)
			errFlag = true;
	}

	@Override
	public String toString() {
		return "WalledCityMod";
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
