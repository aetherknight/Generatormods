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

import generatormods.config.ChestContentsConfig;
import generatormods.config.ChestType;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.IWorldGenerator;
import cpw.mods.fml.common.Loader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.minecraft.world.World;
import net.minecraft.world.WorldProviderEnd;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.storage.ISaveHandler;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

/*
 * BuildingExplorationHandler is an abstract superclass for PopulatorWalledCity
 * and PopulatorGreatWall. It loads settings files and runs
 * WorldGeneratorThreads.
 */
public abstract class BuildingExplorationHandler implements IWorldGenerator {
    protected final static String VERSION = "0.1.6";
	protected final static int MAX_TRIES_PER_CHUNK = 100;
	public final static File CONFIG_DIRECTORY = new File(Loader.instance().getConfigDir(), "generatormods");
	protected final static File LOG = new File(new File(getMinecraftBaseDir(), "logs"), "generatormods_log.txt");
	protected String settingsFileName, templateFolderName;
	public Logger logger;
	public PrintWriter lw = null;
	public float GlobalFrequency = 0.025F;
	public int TriesPerChunk = 1;
	protected Map<ChestType, ChestContentsConfig> chestItems = new HashMap<ChestType, ChestContentsConfig>();
	protected boolean errFlag = false, dataFilesLoaded = false;
	protected boolean logActivated = false;
	protected List<Integer> AllowedDimensions = new ArrayList<Integer>();
	private List<World> currentWorld = new ArrayList<World>();
	public static String[] BIOME_NAMES = new String[BiomeGenBase.getBiomeGenArray().length + 1];
	static {
		BIOME_NAMES[0] = "Underground";
	}

	//**************************** FORGE WORLD GENERATING HOOK ****************************************************************************//
	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
		if (world.getWorldInfo().isMapFeaturesEnabled() && !(world.provider instanceof WorldProviderEnd)) {
			//if structures are enabled can generate in any world except in The End, if id is in AllowedDimensions list
			if (AllowedDimensions.contains(world.provider.dimensionId)) {
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

	public void logOrPrint(String str, String lvl) {
		if (this.logActivated)
			logger.log(Level.toLevel(lvl), str);
	}

	public void updateWorldExplored(World world) {
		if (isNewWorld(world)) {
			logOrPrint("Starting to survey " + world.provider.getDimensionName() + " for generation...", "INFO");
		}
	}

	protected void finalizeLoading(boolean hasTemplate, String structure) {
		if (hasTemplate) {
			lw.println("\nTemplate loading complete.");
		}
		lw.println("Probability of " + structure + " generation attempt per chunk explored is " + GlobalFrequency + ", with " + TriesPerChunk + " tries per chunk.");
	}

	protected void initializeLogging(String message) throws IOException {
		if (LOG.length() > 8350)
			LOG.delete();
		lw = new PrintWriter(new BufferedWriter(new FileWriter(LOG, LOG.canWrite())));
		logOrPrint(message, "INFO");
		if (BIOME_NAMES[1] == null || BIOME_NAMES[1].equals("")) {
			for (int i = 0; i < BIOME_NAMES.length - 1; i++) {
				if (BiomeGenBase.getBiomeGenArray()[i] != null)
					BIOME_NAMES[i + 1] = BiomeGenBase.getBiomeGenArray()[i].biomeName;
			}
		}
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

	private static File getMinecraftBaseDir() {
		if (FMLCommonHandler.instance().getSide().isClient()) {
			return FMLClientHandler.instance().getClient().mcDataDir;
		}
		return FMLCommonHandler.instance().getMinecraftServerInstance().getFile("");
	}
}
