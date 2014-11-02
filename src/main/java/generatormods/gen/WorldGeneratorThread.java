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
package generatormods.gen;

import generatormods.buildings.IBuildingConfig;
import generatormods.config.chests.ChestContentsSpec;
import generatormods.config.chests.ChestType;
import generatormods.walledcity.CityDataManager;
import generatormods.walledcity.ILayoutGenerator;

import java.util.Map;
import java.util.Random;

import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

import org.apache.logging.log4j.Logger;

import static generatormods.util.WorldUtil.HIT_WATER;
import static generatormods.util.WorldUtil.WORLD_MAX_Y;
import static generatormods.util.WorldUtil.findSurfaceJ;

/*
 * WorldGeneratorThread is a thread that generates structures in the Minecraft
 * world. It is intended to serially hand back and forth control with a
 * BuildingExplorationHandler (not to run parallel).
 */
public abstract class WorldGeneratorThread implements IBuildingConfig {
    public final Logger logger;
	public final World world;
	public final Random random;
    /* An absolute X-coorindate in the chunk being generated. */
    public final int chunkI;
    /* An absolute Z-coorindate in the chunk being generated. */
    public final int chunkK;
    /* The number of tries for the current chunk. */
    public final int triesPerChunk;
    /**
     * The probability that a particular try will attempt to generate. May
     * still fail for other reasons though.
     */
	public final double chunkTryProb;
	private int min_spawn_height = 0, max_spawn_height = 127;
	public boolean spawn_surface = true;
    public final Map<ChestType, ChestContentsSpec> chestItems;
    /* All WorldGeneratorThreads will have these, even if not used. */
	public int backtrackLength = 9;

    public WorldGeneratorThread(World world, Random random,
            int chunkI, int chunkK, int TriesPerChunk, double ChunkTryProb, Logger logger,
            Map<ChestType, ChestContentsSpec> chestItems) {
        this.world = world;
		this.random = random;
		this.chunkI = chunkI;
		this.chunkK = chunkK;
		this.triesPerChunk = TriesPerChunk;
		this.chunkTryProb = ChunkTryProb;
        this.logger = logger;
        this.chestItems = chestItems;
		max_spawn_height = WORLD_MAX_Y;
	}

	public abstract boolean generate(int i0, int j0, int k0);

	public void run() {
		boolean success = false;
		int tries = 0, j0 = 0, i0, k0;
		do {
			if (tries == 0 || this.random.nextDouble() < chunkTryProb) {
				i0 = chunkI + this.random.nextInt(16);
				k0 = chunkK + this.random.nextInt(16);
				if (spawn_surface) {
                    j0 = findSurfaceJ(this.world, i0, k0, WORLD_MAX_Y, true, 3) + 1;
				} else {
					j0 = min_spawn_height + this.random.nextInt(max_spawn_height - min_spawn_height + 1);
				}
				if (j0 > 0 && world.getBiomeGenForCoordsBody(i0, k0) != BiomeGenBase.ocean)
					success = generate(i0, j0, k0);
			}
			tries++;
        } while (!success && tries < triesPerChunk && j0 != HIT_WATER);
	}

	//****************************  FUNCTION - setSpawnHeight *************************************************************************************//
	public void setSpawnHeight(int min_spawn_height_, int max_spawn_height_, boolean spawn_surface_) {
		min_spawn_height = min_spawn_height_;
		max_spawn_height = max_spawn_height_;
		spawn_surface = spawn_surface_;
	}

    @Override
    public int getBacktrackLength() {
        return backtrackLength;
    }

    @Override
    public Map<ChestType, ChestContentsSpec> getChestConfigs() {
        return chestItems;
    }

    @Override
    public CityDataManager getCityDataManager() {
        return null;
    }

    @Override
    public ILayoutGenerator getLayoutGenerator() {
        return null;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public Random getRandom() {
        return random;
    }

    @Override
    public World getWorld() {
        return world;
    }
}
