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

import generatormods.PopulatorGreatWall;
import generatormods.buildings.BuildingWall;
import generatormods.common.Dir;
import generatormods.common.Handedness;
import generatormods.common.TemplateWall;

import java.util.List;
import java.util.Random;

import net.minecraft.world.World;

@SuppressWarnings("unused")
public class WorldGenSingleWall extends WorldGeneratorThread {
	private int[] pt;
	private List<TemplateWall> wallStyles;
	private int[] placedCoords = null;

	public WorldGenSingleWall(PopulatorGreatWall gw, World world, Random random, int[] pt) {
        super(world, random, pt[0] >> 4, pt[2] >> 4, 0, 0.0, gw.logger, gw.sharedConfig.chestConfigs);
		this.pt = pt;
        wallStyles = gw.wallStyles;
        placedCoords = gw.placedCoords;
	}

	@Override
	public boolean generate(int i0, int j0, int k0) {
        TemplateWall ws = TemplateWall.pickBiomeWeightedWallStyle(wallStyles, world, i0, k0, world.rand, false);
        BuildingWall wall =
                new BuildingWall(0, this, ws, Dir.NORTH, Handedness.R_HAND, ws.MaxL, true, i0, j0, k0);
		//BuildingWall(int ID_, WorldGeneratorThread wgt_,WallStyle ws_,int dir_,int axXHand_, int maxLength_,int i0_,int j0_, int k0_){
        wall.setTarget(placedCoords);
		wall.plan(1, 0, ws.MergeWalls ? ws.WWidth : BuildingWall.DEFAULT_LOOKAHEAD, false);
		//plan(int Backtrack, int startN, int depth, int lookahead, boolean stopAtWall) throws InterruptedException {
		if (wall.bLength >= wall.y_targ) {
			wall.smooth(ws.ConcaveDownSmoothingScale, ws.ConcaveUpSmoothingScale, true);
			wall.buildFromTML();
			wall.makeBuildings(true, true, true, false, false);
            placedCoords = null;
		}
		return true;
	}
}
