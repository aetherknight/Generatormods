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
package generatormods.buildings;

import generatormods.common.BlockAndMeta;
import generatormods.common.BlockProperties;
import generatormods.common.Dir;
import generatormods.common.TemplateTML;
import generatormods.common.WorldHelper;
import generatormods.gen.WorldGeneratorThread;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

import static generatormods.common.WorldHelper.IGNORE_WATER;

/*
 * BuildingTML generates a building from a .tml template.
 */
public class BuildingTML extends Building {
	final TemplateTML tmlt;

    public BuildingTML(int ID_, IBuildingConfig config, Dir bDir_, int axXHand_,
            boolean centerAligned_, TemplateTML tmlt_, int[] sourcePt) {
        super(ID_, config, null, bDir_, axXHand_, centerAligned_, new int[] {tmlt_.width,
                tmlt_.height, tmlt_.length}, sourcePt);
		tmlt = tmlt_;
		j0 -= tmlt.embed;
	}

	public void build() {
		tmlt.setFixedRules(world.rand);
		//build base
		int[][] base = tmlt.namedLayers.get("base");
		for (int y = 0; y < bLength; y++)
			for (int x = 0; x < bWidth; x++) {
				if (base != null)
					buildDown(x, -1, y, tmlt.rules[base[y][x]], tmlt.leveling, 0, 0);
				else
                    WorldHelper.fillDown(getSurfaceIJKPt(x, y, j0 - 1, true, IGNORE_WATER), j0 - 1,
                            world);
			}
		//clear overhead
		for (int z = bHeight; z < tmlt.cutIn + tmlt.embed; z++)
			for (int y = 0; y < bLength; y++)
				for (int x = 0; x < bWidth; x++) {
					setBlockLocal(x, z, y, Blocks.air);
				}
		//build
		for (int z = 0; z < bHeight; z++)
			for (int y = 0; y < bLength; y++)
				for (int x = 0; x < bWidth; x++) {
					setBlockLocal(x, z, y, tmlt.rules[tmlt.template[z][y][x]]);
				}
        flushDelayed();
	}

	public boolean queryCanBuild(int ybuffer) {
		if (j0 <= 0)
			return false;
		//Don't build if it would require leveling greater than tmlt.leveling
		for (int y = 0; y < bLength; y++)
			for (int x = 0; x < bWidth; x++)
				if (j0 - getSurfaceIJKPt(x, y, j0 - 1, true, 3)[1] > tmlt.leveling + 1)
					return false;
		//check to see if we are underwater
		if (tmlt.waterHeight != TemplateTML.NO_WATER_CHECK) {
			int waterCheckHeight = tmlt.waterHeight + tmlt.embed + 1; //have to unshift by embed
			if (BlockProperties.get(getBlockIdLocal(0, waterCheckHeight, 0)).isWater || BlockProperties.get(getBlockIdLocal(0, waterCheckHeight, bLength - 1)).isWater
					|| BlockProperties.get(getBlockIdLocal(bWidth - 1, waterCheckHeight, 0)).isWater || BlockProperties.get(getBlockIdLocal(bWidth - 1, waterCheckHeight, bLength - 1)).isWater)
				return false;
		}
        if (layoutGenerator != null) {
			//allow large templates to be built over streets by using the tower code to check
			//However, if we do build, always put down the template code
			int layoutCode = tmlt.buildOverStreets ? WorldGeneratorThread.LAYOUT_CODE_TOWER : WorldGeneratorThread.LAYOUT_CODE_TEMPLATE;
            if (layoutGenerator.layoutIsClear(this, tmlt.templateLayout, layoutCode)) {
                layoutGenerator.setLayoutCode(this, tmlt.templateLayout,
                        WorldGeneratorThread.LAYOUT_CODE_TEMPLATE);
			} else
				return false;
		} else {
			if (isObstructedFrame(0, ybuffer))
				return false;
		}
		return true;
	}

    @Override
    protected BlockAndMeta getDelayedStair(Block blc, int...block){
        return new BlockAndMeta(blc, block[3]);
    }
}
