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

import generatormods.PopulatorCARuins;
import generatormods.buildings.Building;
import generatormods.buildings.BuildingCellularAutomaton;
import generatormods.caruins.config.CARuinsConfig;
import generatormods.caruins.config.CARule;
import generatormods.caruins.config.SeedType;
import generatormods.caruins.config.WeightedCARule;
import generatormods.caruins.seeds.CircularSeed;
import generatormods.caruins.seeds.CruciformSeed;
import generatormods.caruins.seeds.ISeed;
import generatormods.caruins.seeds.LinearSeed;
import generatormods.caruins.seeds.SymmetricSeed;
import generatormods.common.Dir;
import generatormods.common.TemplateRule;

import java.util.Random;

import net.minecraft.world.World;
import net.minecraft.util.WeightedRandom;

public class WorldGenCARuins extends WorldGeneratorThread {
	private CARule caRule = null;

    private final CARuinsConfig config;

	public WorldGenCARuins(PopulatorCARuins ca, World world, Random random, int chunkI, int chunkK, int triesPerChunk, double chunkTryProb) {
		super(ca, world, random, chunkI, chunkK, triesPerChunk, chunkTryProb);
        config = ca.config;
	}

	@Override
	public boolean generate(int i0, int j0, int k0) {
        logger.debug("Attempting to generate CARuins near ("+i0+","+j0+","+k0+")");
        int ContainerWidth = config.containerWidth;
        int ContainerLength = config.containerLength;

		int th = config.minHeight + random.nextInt(config.maxHeight - config.minHeight + 1);
        if (caRule == null) //if we haven't picked in an earlier generate call
            caRule = ((WeightedCARule)WeightedRandom.getRandomItem(world.rand, config.caRules)).getRule();
        if (caRule == null)
            return false;
        ISeed seed = pickSeed();
        TemplateRule blockRule =
                config.blockRules[world.getBiomeGenForCoordsBody(i0, k0).biomeID + 1];
        //can use this to test out new Building classes
        //BuildingSpiralStaircase bss=new BuildingSpiralStaircase(this,blockRule,random.nextInt(4),2*random.nextInt(2)-1,false,-(random.nextInt(10)+1),new int[]{i0,j0,k0});
        //bss.build(0,0);
        //bss.bottomIsFloor();
        //return true;
        BuildingCellularAutomaton bca =
                new BuildingCellularAutomaton(this, blockRule, Dir.randomDir(random), 1, false,
                        ContainerWidth, th, ContainerLength, seed.makeSeed(world.rand),
                        caRule.toBytes(), null, new int[] {i0, j0, k0});
		if (bca.plan(true, config.minHeightBeforeOscillation) && bca.queryCanBuild(0, true)) {
            logger.info("Building CARuin at ("+i0+","+j0+","+k0+")");
			bca.build(config.smoothWithStairs, config.makeFloors);
			if (config.globalFrequency < 0.05 && random.nextInt(2) != 0) {
				for (int tries = 0; tries < 10; tries++) {
					int[] pt = new int[] { i0 + (2 * random.nextInt(2) - 1) * (ContainerWidth + random.nextInt(ContainerWidth)), 0,
							k0 + (2 * random.nextInt(2) - 1) * (ContainerWidth + random.nextInt(ContainerWidth)) };
					pt[1] = Building.findSurfaceJ(world, pt[0], pt[2], Building.WORLD_MAX_Y, true, 3) + 1;
                    logger.debug("Recursing");
					if (generate(pt[0], pt[1], pt[2])) {
                        logger.debug("Successfully Recursed");
						break;
					}
				}
			}
			return true;
		}
		return false;
	}

    private ISeed pickSeed() {
        SeedType seedCode =
                ((SeedType.Weighted) WeightedRandom.getRandomItem(world.rand, config.weightedSeeds))
                        .getSeedType();
        if (caRule.isFourRule()) // only use symmetric for 4-rules
            seedCode = SeedType.SYMMETRIC_SEED;
        switch (seedCode) {
            case SYMMETRIC_SEED:
                return new SymmetricSeed(Math.min(config.containerWidth, config.containerLength),
                        config.symmetricSeedDensity);
            case LINEAR_SEED:
                return new LinearSeed(config.containerWidth);
            case CIRCULAR_SEED:
                return new CircularSeed(Math.min(config.containerWidth, config.containerLength));
            default:
                return new CruciformSeed(Math.min(config.containerWidth, config.containerLength));
        }
    }
}
