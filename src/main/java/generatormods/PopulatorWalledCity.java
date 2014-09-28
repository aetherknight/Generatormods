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
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.village.Village;
import net.minecraft.village.VillageDoorInfo;
import net.minecraft.world.World;

import org.apache.logging.log4j.Logger;

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
	private final static String CITY_FILE_SAVE = "WalledCities.txt", STREET_TEMPLATES_FOLDER_NAME = "streets";
	//DATA VARIABLES
	public List<TemplateWall> cityStyles = null;
    public List<TemplateWall> undergroundCityStyles = new ArrayList<TemplateWall>();
	public Map<World, List<int[]>> cityLocations;
	public Map<Integer, List<VillageDoorInfo>> cityDoors;
	public LinkedList<int[]> citiesBuiltMessages = new LinkedList<int[]>();
	private Map<World, File> cityFiles;

    public WalledCityConfig config;

	//****************************  FUNCTION - addCityToVillages*************************************************************************************//
	public void addCityToVillages(World world, int id) {
		if (world != null && world.provider.dimensionId != CITY_TYPE_UNDERGROUND) {
			if (world.villageCollectionObj != null) {
				Village city = new Village(world);
				if (cityDoors.containsKey(id)) {
					for (VillageDoorInfo door : cityDoors.get(id))
						if (door != null)
							city.addVillageDoorInfo(door);
					world.villageCollectionObj.getVillageList().add(city);
					cityDoors.remove(id);
				}
			}
		}
	}

	//****************************  FUNCTION - chatCityBuilt *************************************************************************************//
	public void chatBuildingCity(String chatString, String logString) {
		if (logString != null)
            logger.debug(logString);
		if (!config.cityBuiltMessage)
			return;
		List<?> playerList = MinecraftServer.getServer().getConfigurationManager().playerEntityList;
		if (playerList != null) {
			for (int index = 0; index < playerList.size(); ++index) {
				EntityPlayerMP player = (EntityPlayerMP) playerList.get(index);
				player.addChatComponentMessage(new ChatComponentText(chatString));
			}
		}
	}

	public void chatCityBuilt(int[] args) {
		if (!config.cityBuiltMessage)
			return;
		List<?> playerList = MinecraftServer.getServer().getConfigurationManager().playerEntityList;
		if (playerList == null) {
			citiesBuiltMessages.add(args);
		} else {
			for (int index = 0; index < playerList.size(); ++index) {
				EntityPlayerMP player = (EntityPlayerMP) playerList.get(index);
				String dirStr;
				int dI = args[0] - (int) player.posX;
				int dK = args[2] - (int) player.posZ;
				if (dI * dI + dK * dK < args[4] * args[4]) {
					dirStr = "nearby";
				}
				dirStr = "to the ";
				if (Math.abs(dI) > 2 * Math.abs(dK))
					dirStr += dI > 0 ? "east" : "west";
				else if (Math.abs(dK) > 2 * Math.abs(dI))
					dirStr += dK > 0 ? "south" : "north";
				else
					dirStr += dI > 0 ? (dK > 0 ? "southeast" : "northeast") : (dK > 0 ? "southwest" : "northwest");
				player.addChatComponentMessage(new ChatComponentText("** Built city " + dirStr + " (" + args[0] + "," + args[1] + "," + args[2] + ")! **"));
			}
		}
	}

	//****************************  FUNCTION - cityIsSeparated *************************************************************************************//
	public boolean cityIsSeparated(World world, int i, int k, int cityType) {
		if (cityLocations.containsKey(world)) {
			for (int[] location : cityLocations.get(world)) {
				if (location[2] == cityType && Math.abs(location[0] - i) + Math.abs(location[1] - k) < (cityType == CITY_TYPE_UNDERGROUND ? config.undergroundMinCitySeparation : config.minCitySeparation)) {
					return false;
				}
			}
		}
		return true;
	}

	//****************************  FUNCTION - generate *************************************************************************************//
	@Override
	public final void generate(World world, Random random, int i, int k) {
		if (config.cityBuiltMessage && world.playerEntities != null)
			while (citiesBuiltMessages.size() > 0)
				chatCityBuilt(citiesBuiltMessages.remove());
		if (cityStyles.size() > 0 && cityIsSeparated(world, i, k, world.provider.dimensionId) && random.nextFloat() < config.globalFrequency) {
			(new WorldGenWalledCity(this, world, random, i, k, config.triesPerChunk, config.globalFrequency)).run();
		}
		if (undergroundCityStyles.size() > 0 && cityIsSeparated(world, i, k, CITY_TYPE_UNDERGROUND) && random.nextFloat() < config.undergroundGlobalFrequency) {
			WorldGeneratorThread wgt = new WorldGenUndergroundCity(this, world, random, i, k, 1, config.undergroundGlobalFrequency);
			int maxSpawnHeight = Building.findSurfaceJ(world, i, k, Building.WORLD_MAX_Y, false, Building.IGNORE_WATER) - WorldGenUndergroundCity.MAX_DIAM / 2 - 5; //44 at sea level
			int minSpawnHeight = MAX_FOG_HEIGHT + WorldGenUndergroundCity.MAX_DIAM / 2 - 8; //34, a pretty thin margin. Too thin for underocean cities?
			if (minSpawnHeight <= maxSpawnHeight)
				wgt.setSpawnHeight(minSpawnHeight, maxSpawnHeight, false);
			(wgt).run();
		}
	}

	//****************************  FUNCTION - loadDataFiles *************************************************************************************//
	@Override
	public final void loadDataFiles() {
		try {
            logger.info("Loading options and templates for the Walled City Generator.");

            config = new WalledCityConfig(CONFIG_DIRECTORY, logger);
            config.initialize();
            sharedConfig = config.sharedConfig;

			File stylesDirectory = new File(CONFIG_DIRECTORY, templateFolderName);
            cityStyles = TemplateWall.loadWallStylesFromDir(stylesDirectory, logger);
            TemplateWall.loadStreets(cityStyles, new File(stylesDirectory, STREET_TEMPLATES_FOLDER_NAME), logger);
			for (int m = 0; m < cityStyles.size(); m++) {
				if (cityStyles.get(m).underground) {
					TemplateWall uws = cityStyles.remove(m);
					uws.streets.add(uws); //underground cities have no outer walls, so this should be a street style
					undergroundCityStyles.add(uws);
					m--;
				}
			}
            logger.info("Template loading complete.");

            logger.info("Probability of city generation attempt per chunk explored is " + sharedConfig.globalFrequency + ", with " + sharedConfig.triesPerChunk + " tries per chunk.");
		} catch (Exception e) {
			errFlag = true;
            logger.fatal("There was a problem loading the walled city mod");
            logger.fatal(e);
		}
		if (config.globalFrequency < 0.000001 && config.undergroundGlobalFrequency < 0.000001)
			errFlag = true;
		dataFilesLoaded = true;
	}

	//Load templates after mods have loaded so we can check whether any modded blockIDs are valid
	@EventHandler
	public void modsLoaded(FMLPostInitializationEvent event) {
		if (!dataFilesLoaded)
			loadDataFiles();
		if (!errFlag) {
			GameRegistry.registerWorldGenerator(this, 0);
		}
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		cityFiles = new HashMap<World, File>();
		cityLocations = new HashMap<World, List<int[]>>();
		cityDoors = new HashMap<Integer, List<VillageDoorInfo>>();
		logger = event.getModLog();
		templateFolderName = "walledcity";
        if(event.getSourceFile().getName().endsWith(".jar") && event.getSide().isClient()){
            try {
                Class.forName("mods.mud.ModUpdateDetector").getDeclaredMethod("registerMod", ModContainer.class, String.class, String.class).invoke(null,
                        FMLCommonHandler.instance().findContainerFor(this),
                        "https://raw.github.com/GotoLink/Generatormods/master/update.xml",
                        "https://raw.github.com/GotoLink/Generatormods/master/changelog.md"
                );
            } catch (Throwable e) {
            }
        }
	}

	//****************************  FUNCTION - saveCityLocations *************************************************************************************//
	public void saveCityLocations(World world) {
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new BufferedWriter(new FileWriter(cityFiles.get(world), true)));
			BufferedReader br = new BufferedReader(new FileReader(cityFiles.get(world)));
			if (br.readLine() == null) {
				pw.println("City locations in " + world.provider.getDimensionName() + " of : " + world.getWorldInfo().getWorldName());
			}
			br.close();
			int[] location = cityLocations.get(world).get(cityLocations.get(world).size() - 1);
			pw.println(new StringBuilder(Integer.toString(location[0])).append(",").append(Integer.toString(location[1])).append(",").append(Integer.toString(location[2])));
		} catch (IOException e) {
            logger.warn(e);
		} finally {
			if (pw != null)
				pw.close();
		}
	}

	@Override
	public String toString() {
		return "WalledCityMod";
	}

	//****************************  FUNCTION - updateWorldExplored *************************************************************************************//
	@Override
	public void updateWorldExplored(World world) {
		super.updateWorldExplored(world);
		File cityFile = new File(getWorldSaveDir(world), world.provider.getDimensionName() + CITY_FILE_SAVE);
		if (cityFiles.isEmpty() || !cityFiles.containsKey(world))
			cityFiles.put(world, cityFile);
		try {
			if (!cityFile.createNewFile() && !cityLocations.containsKey(world))
				cityLocations.put(world, getCityLocs(cityFile, logger));
		} catch (IOException e) {
            logger.warn(e);
		}
	}

	public static List<int[]> getCityLocs(File city, Logger logger) {
		List<int[]> cityLocs = new ArrayList<int[]>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(city));
			for (String read = br.readLine(); read != null; read = br.readLine()) {
				String[] split = read.split(",");
				if (split.length == 3) {
					cityLocs.add(new int[] { Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]) });
				}
			}
		} catch (IOException e) {
            logger.warn(e);
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException e) {
			}
		}
		return cityLocs;
	}
}
