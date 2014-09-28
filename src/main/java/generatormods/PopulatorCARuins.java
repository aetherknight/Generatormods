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

import generatormods.caruins.config.CARuinsConfig;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.registry.GameRegistry;

import java.io.BufferedReader;
import java.io.PrintWriter;

import java.util.Random;

import net.minecraft.world.World;

/**
 * Main class that hooks into Forge for CARuins. It loads configuration and
 * sets up the world generation it adds.
 */
@Mod(modid = "CARuins", name = "Cellular Automata Generator", version = BuildingExplorationHandler.VERSION, dependencies = "after:ExtraBiomes,BiomesOPlenty", acceptableRemoteVersions = "*")
public class PopulatorCARuins extends BuildingExplorationHandler {
	@Instance("CARuins")
	public static PopulatorCARuins instance;

    public CARuinsConfig config;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		logger = event.getModLog();
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

	@EventHandler
	public void serverStarting(FMLServerStartingEvent event) {
		event.registerServerCommand(new CommandBuild());
        event.registerServerCommand(new CommandScan());
	}

	@Override
	public final void loadDataFiles() {
		try {
			initializeLogging("Loading options for the Cellular Automata Generator.");

            config = new CARuinsConfig(CONFIG_DIRECTORY, logger);
            config.initialize();
            sharedConfig = config.sharedConfig;

			finalizeLoading(false, "ruin");
		} catch (Exception e) {
			errFlag = true;
			logOrPrint("There was a problem loading the Cellular Automata Generator: " + e.getMessage(), "SEVERE");
			lw.println("There was a problem loading the Cellular Automata Generator: " + e.getMessage());
			e.printStackTrace();
		} finally {
			if (lw != null)
				lw.close();
		}
		if (config.globalFrequency < 0.000001 || config.caRules == null || config.caRules.size() == 0)
			errFlag = true;
		dataFilesLoaded = true;
	}

	@Override
	public final void generate(World world, Random random, int i, int k) {
		if (random.nextFloat() < config.globalFrequency)
			(new WorldGenCARuins(this, world, random, i, k, config.triesPerChunk, config.globalFrequency)).run();
	}

	@Override
	public String toString() {
		return "CARuins";
	}

	@EventHandler
	public void modsLoaded(FMLPostInitializationEvent event) {
		if (!dataFilesLoaded)
			loadDataFiles();
		if (!errFlag) {
			GameRegistry.registerWorldGenerator(this, 2);
		}
	}
}
