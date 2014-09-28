/* Source code for the The Great Wall Mod and Walled City Generator Mods for the game Minecraft
 * Copyright (C) 2011 by formivore
 * Copyright (C) 2013-2014 by GotoLink
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
package generatormods;

import generatormods.config.SharedConfig;

import cpw.mods.fml.common.IWorldGenerator;
import cpw.mods.fml.common.Loader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.world.World;
import net.minecraft.world.WorldProviderEnd;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.storage.ISaveHandler;

import org.apache.logging.log4j.Logger;

/*
 * BuildingExplorationHandler is an abstract superclass for PopulatorWalledCity
 * and PopulatorGreatWall. It loads settings files and runs
 * WorldGeneratorThreads.
 */
public abstract class BuildingExplorationHandler implements IWorldGenerator {
    protected final static String VERSION = "0.1.6";
	public final static File CONFIG_DIRECTORY = new File(Loader.instance().getConfigDir(), "generatormods");

	protected String templateFolderName;
	public Logger logger;
	protected boolean errFlag = false, dataFilesLoaded = false;
	private List<World> currentWorld = new ArrayList<World>();

    public SharedConfig sharedConfig;

	//**************************** FORGE WORLD GENERATING HOOK ****************************************************************************//
	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
		if (world.getWorldInfo().isMapFeaturesEnabled() && !(world.provider instanceof WorldProviderEnd)) {
			//if structures are enabled can generate in any world except in The End, if id is in AllowedDimensions list
			if (sharedConfig.allowedDimensions.contains(world.provider.dimensionId)) {
				generateSurface(world, random, chunkX, chunkZ);
			}
		}
	}

	abstract public void generate(World world, Random random, int i, int k);

	//****************************  FUNCTION - GenerateSurface  *************************************************************************************//
	public void generateSurface(World world, Random random, int i, int k) {
		if (errFlag)
			return;
		updateWorldExplored(world);
		generate(world, random, i * 16, k * 16);
	}

	abstract public void loadDataFiles();

	public void updateWorldExplored(World world) {
		if (isNewWorld(world)) {
            logger.info("Starting to survey " + world.provider.getDimensionName() + " for generation...");
		}
	}

	protected void finalizeLoading(boolean hasTemplate, String structure) {
		if (hasTemplate) {
            logger.info("Template loading complete.");
		}
		logger.info("Probability of " + structure + " generation attempt per chunk explored is " + sharedConfig.globalFrequency + ", with " + sharedConfig.triesPerChunk + " tries per chunk.");
	}

	protected boolean isNewWorld(World world) {
		if (currentWorld.isEmpty()) {
			currentWorld.add(world);
			return true;
		} else if (currentWorld.contains(world)) {
			return false;
		} else {
			File newdir = getWorldSaveDir(world);
			for (World w : currentWorld) {
				//check the filename in case we changed of dimension
				File olddir = getWorldSaveDir(w);
				if (newdir != null && olddir != null && olddir.compareTo(newdir) != 0) {
					// new world has definitely been created.
					currentWorld.add(world);
					return true;
				}
			}
			return false;
		}
	}

	protected static File getWorldSaveDir(World world) {
		ISaveHandler worldSaver = world.getSaveHandler();
		if (worldSaver.getChunkLoader(world.provider) instanceof AnvilChunkLoader) {
			return ((AnvilChunkLoader) worldSaver.getChunkLoader(world.provider)).chunkSaveLocation;
		}
		return null;
	}
}
