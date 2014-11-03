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

import generatormods.util.WorldUtil;
import cpw.mods.fml.common.IWorldGenerator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.world.World;
import net.minecraft.world.WorldProviderEnd;
import net.minecraft.world.chunk.IChunkProvider;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * BuildingExplorationHandler is an abstract superclass for PopulatorWalledCity
 * and PopulatorGreatWall. It loads settings files and runs
 * WorldGeneratorThreads.
 */
public abstract class AbstractModule implements IWorldGenerator {
	public Logger logger;
    private boolean isDisabled = false;
	private List<World> currentWorld = new ArrayList<World>();
    protected File configDir;
    protected File jarFile;
    protected List<Integer> allowedDimensions;

    public AbstractModule(String parentModName, File configDir, File jarFile) {
        this.logger = LogManager.getLogger(parentModName + "." + toString());
        this.configDir = configDir;
        this.jarFile = jarFile;
    }

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
        if (isDisabled())
            return;
		if (world.getWorldInfo().isMapFeaturesEnabled() && !(world.provider instanceof WorldProviderEnd)) {
            // if structures are enabled can generate in any world except in The End, if id is in
            // AllowedDimensions list
            if (allowedDimensions.contains(world.provider.dimensionId)) {
                updateWorldExplored(world);
                generate(world, random, chunkX * 16, chunkZ * 16);
			}
		}
	}

	abstract public void generate(World world, Random random, int i, int k);

	public void updateWorldExplored(World world) {
		if (isNewWorld(world)) {
            logger.info("Starting to survey {} for generation...",
                    world.provider.getDimensionName());
		}
	}

	protected boolean isNewWorld(World world) {
		if (currentWorld.isEmpty()) {
			currentWorld.add(world);
			return true;
		} else if (currentWorld.contains(world)) {
			return false;
		} else {
            File newdir = WorldUtil.getWorldSaveDir(world);
			for (World w : currentWorld) {
				//check the filename in case we changed of dimension
                File olddir = WorldUtil.getWorldSaveDir(w);
				if (newdir != null && olddir != null && olddir.compareTo(newdir) != 0) {
					// new world has definitely been created.
					currentWorld.add(world);
					return true;
				}
			}
			return false;
		}
	}

    protected void disable(String reason) {
        disable(reason, null);
    }

    protected void disable(String reason, Throwable e) {
        isDisabled = true;
        if (logger != null) {
            if (e != null)
                logger.fatal("Disabling " + this.toString() + ": " + reason, e);
            else
                logger.fatal("Disabling " + this.toString() + ": " + reason);
        }
    }

    /* Whether this component is disabled or not. */
    public boolean isDisabled() {
        return isDisabled;
    }
}
