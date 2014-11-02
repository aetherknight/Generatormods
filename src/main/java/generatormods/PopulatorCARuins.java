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

import generatormods.caruins.config.CARuinsConfig;
import generatormods.commands.CommandBuild;
import generatormods.commands.CommandScan;
import generatormods.common.ModUpdateDetectorWrapper;
import generatormods.gen.WorldGenCARuins;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.registry.GameRegistry;

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
        ModUpdateDetectorWrapper.checkForUpdates(this, event);
	}

    @EventHandler
    public void modsLoaded(FMLPostInitializationEvent event) {
        loadConfiguration();
        if (!errFlag) {
            GameRegistry.registerWorldGenerator(this, 2);
        }
    }

	@EventHandler
	public void serverStarting(FMLServerStartingEvent event) {
		event.registerServerCommand(new CommandBuild());
        event.registerServerCommand(new CommandScan());
	}

    private final void loadConfiguration() {
		try {
            logger.info("Loading options for the Cellular Automata Generator.");

            config = new CARuinsConfig(CONFIG_DIRECTORY, logger);
            config.initialize();
            allowedDimensions = config.getAllowedDimensions();

            logger.info("Probability of ruin generation attempt per chunk explored is "
                    + config.getGlobalFrequency() + ", with " + config.getTriesPerChunk()
                    + " tries per chunk.");
		} catch (Exception e) {
			errFlag = true;
            logger.fatal("There was a problem loading the Cellular Automata Generator", e);
		}
        if (config.getGlobalFrequency() < 0.000001 || config.caRules == null
                || config.caRules.size() == 0)
			errFlag = true;
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
