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
import generatormods.common.TemplateWall;
import generatormods.gen.WorldGenGreatWall;
import generatormods.greatwall.config.GreatWallConfig;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

import net.minecraft.world.World;

/*
 * PopulatorGreatWall is the main class that hooks into ModLoader for the Great
 * Wall Mod. It reads the globalSettings file and runs WorldGenWalledCities.
 */
@Mod(modid = "GreatWallMod", name = "Great Wall Mod", version = BuildingExplorationHandler.VERSION, dependencies = "after:ExtraBiomes,BiomesOPlenty", acceptableRemoteVersions = "*")
public class PopulatorGreatWall extends BuildingExplorationHandler {
	@Instance("GreatWallMod")
	public static PopulatorGreatWall instance;
	//DATA VARIABLES
	public ArrayList<TemplateWall> wallStyles = null;
	public int[] placedCoords = null;
	public World placedWorld = null;

    public GreatWallConfig config;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		logger = event.getModLog();
		templateFolderName = "greatwall";
        ModUpdateDetectorWrapper.checkForUpdates(this, event);
	}

    @EventHandler
    public void modsLoaded(FMLPostInitializationEvent event) {
        loadConfiguration();
        if (!errFlag) {
            GameRegistry.registerWorldGenerator(this, 1);
        }
    }

    public final void loadConfiguration() {
		try {
            logger.info("Loading options and templates for the Great Wall Mod.");

            config = new GreatWallConfig(CONFIG_DIRECTORY, logger);
            config.initialize();
            sharedConfig = config.sharedConfig;

			File stylesDirectory = new File(CONFIG_DIRECTORY, templateFolderName);
            wallStyles = TemplateWall.loadWallStylesFromDir(stylesDirectory, logger);
            logger.info("Template loading complete.");

            logger.info("Probability of wall generation attempt per chunk explored is " + config.globalFrequency + ", with " + config.triesPerChunk + " tries per chunk.");
		} catch (Exception e) {
			errFlag = true;
            logger.fatal("There was a problem loading the great wall mod", e);
		}
		if (config.globalFrequency < 0.000001)
			errFlag = true;
	}

	@Override
	public final void generate(World world, Random random, int i, int k) {
		if (random.nextFloat() < config.globalFrequency)
			(new WorldGenGreatWall(this, world, random, i, k, config.triesPerChunk, config.globalFrequency)).run();
	}

	@Override
	public String toString() {
		return "GreatWallMod";
	}
}
