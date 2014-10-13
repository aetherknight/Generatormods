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

import generatormods.buildings.BuildingDoubleWall;
import generatormods.common.Dir;
import generatormods.common.TemplateWall;
import generatormods.common.config.ChestContentsSpec;
import generatormods.common.config.ChestType;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.minecraft.world.World;
import org.apache.logging.log4j.Logger;

/*
 * WorldGenGreatWall creates a great wall in Minecraft. This class is chiefly a
 * WorldGeneratorThread wrapper for a BuildingDoubleWall. It also checks
 * curviness and length.
 */
public class WorldGenGreatWall extends WorldGeneratorThread {
	private List<TemplateWall> wallStyles;
    private double curveBias;

    public WorldGenGreatWall(World world, Random random, int chunkI, int chunkK, int triesPerChunk,
            double chunkTryProb, Logger logger, Map<ChestType, ChestContentsSpec> chestConfigs,
            List<TemplateWall> wallStyles, double curveBias) {
        super(world, random, chunkI, chunkK, triesPerChunk, chunkTryProb, logger, chestConfigs);
        this.wallStyles = wallStyles;
        this.curveBias = curveBias;
	}

	@Override
	public boolean generate(int i0, int j0, int k0) {
        logger.debug("Attempting to generate GreatWall near ("+i0+","+j0+","+k0+")");
        TemplateWall ws = TemplateWall.pickBiomeWeightedWallStyle(wallStyles, world, i0, k0, world.rand, false);
		if (ws == null)
			return false;
        BuildingDoubleWall dw =
                new BuildingDoubleWall(10 * (random.nextInt(9000) + 1000), this, ws,
                        Dir.randomDir(random), 1, new int[] {i0, j0, k0});
		if (!dw.plan())
			return false;
        logger.info("Building GreatWall at ("+i0+","+j0+","+k0+")");
		//calculate the integrated curvature
		if (curveBias > 0.01) {
			//Perform a probabilistic test
			//Test formula considers both length and curvature, bias is towards longer and curvier walls.
			double curviness = 0;
			for (int m = 1; m < dw.wall1.bLength; m++)
				curviness += (dw.wall1.xArray[m] == dw.wall1.xArray[m - 1] ? 0 : 1) + (dw.wall1.zArray[m] == dw.wall1.zArray[m - 1] ? 0 : 1);
			for (int m = 1; m < dw.wall2.bLength; m++)
				curviness += (dw.wall2.xArray[m] == dw.wall2.xArray[m - 1] ? 0 : 1) + (dw.wall2.zArray[m] == dw.wall2.zArray[m - 1] ? 0 : 1);
			curviness /= (double) (2 * (dw.wall1.bLength + dw.wall2.bLength - 1));
			//R plotting - sigmoid function
			/*
			 * pwall<-function(curviness,curvebias)
			 * 1/(1+exp(-30*(curviness-(curvebias/5))))
			 * plotpwall<-function(curvebias){ plot(function(curviness)
			 * pwall(curviness
			 * ,curvebias),ylim=c(0,1),xlim=c(0,0.5),xlab="curviness"
			 * ,ylab="p",main=paste("curvebias=",curvebias)) } plotpwall(0.5)
			 */
			double p = 1.0 / (1.0 + Math.exp(-30.0 * (curviness - (curveBias / 5.0))));
			if (random.nextFloat() > p && curviness != 0) {
				logger.debug("Rejected great wall, curviness=" + curviness + ", length=" + (dw.wall1.bLength + dw.wall1.bLength - 1) + ", P=" + p);
				return false;
			}
		}
		dw.build(LAYOUT_CODE_NOCODE);
		dw.buildTowers(true, true, ws.MakeGatehouseTowers, false, false);
		return true;
	}
}
