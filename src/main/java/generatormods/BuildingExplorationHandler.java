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

import generatormods.common.Util;
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
	protected boolean errFlag = false;
	private List<World> currentWorld = new ArrayList<World>();

    public SharedConfig sharedConfig;

	//**************************** FORGE WORLD GENERATING HOOK ****************************************************************************//
	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
        if (errFlag)
            return;
		if (world.getWorldInfo().isMapFeaturesEnabled() && !(world.provider instanceof WorldProviderEnd)) {
			//if structures are enabled can generate in any world except in The End, if id is in AllowedDimensions list
			if (sharedConfig.allowedDimensions.contains(world.provider.dimensionId)) {
                updateWorldExplored(world);
                generate(world, random, chunkX * 16, chunkZ * 16);
			}
		}
	}

	abstract public void generate(World world, Random random, int i, int k);

	public void updateWorldExplored(World world) {
		if (isNewWorld(world)) {
            logger.info("Starting to survey " + world.provider.getDimensionName() + " for generation...");
		}
	}

	protected boolean isNewWorld(World world) {
		if (currentWorld.isEmpty()) {
			currentWorld.add(world);
			return true;
		} else if (currentWorld.contains(world)) {
			return false;
		} else {
            File newdir = Util.getWorldSaveDir(world);
			for (World w : currentWorld) {
				//check the filename in case we changed of dimension
                File olddir = Util.getWorldSaveDir(w);
				if (newdir != null && olddir != null && olddir.compareTo(newdir) != 0) {
					// new world has definitely been created.
					currentWorld.add(world);
					return true;
				}
			}
			return false;
		}
	}
}
