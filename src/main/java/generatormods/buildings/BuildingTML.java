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

import generatormods.config.templates.TemplateTML;
import generatormods.util.WorldUtil;
import generatormods.util.blocks.BlockAndMeta;
import generatormods.util.build.BlockProperties;
import generatormods.util.build.Dir;
import generatormods.util.build.Handedness;
import generatormods.walledcity.LayoutCode;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import static generatormods.util.WorldUtil.IGNORE_WATER;

/*
 * BuildingTML generates a building from a .tml template.
 */
public class BuildingTML extends Building {
	final TemplateTML tmlt;

    public BuildingTML(int ID_, IBuildingConfig config, Dir bDir_, Handedness axXHand_,
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
        for (int z = 0; z < bLength; z++)
			for (int x = 0; x < bWidth; x++) {
				if (base != null)
                    buildDown(x, -1, z, tmlt.rules[base[z][x]], tmlt.leveling, 0, 0);
				else
                    WorldUtil.fillDown(getSurfaceIJKPt(x, z, j0 - 1, true, IGNORE_WATER), j0 - 1,
                            world);
			}
		//clear overhead
        for (int y = bHeight; y < tmlt.cutIn + tmlt.embed; y++)
            for (int z = 0; z < bLength; z++)
				for (int x = 0; x < bWidth; x++) {
                    setBlockLocal(x, y, z, Blocks.air);
				}
		//build
        for (int y = 0; y < bHeight; y++)
            for (int z = 0; z < bLength; z++)
				for (int x = 0; x < bWidth; x++) {
                    setBlockLocal(x, y, z, tmlt.rules[tmlt.template[y][z][x]]);
				}
        flushDelayed();
	}

	public boolean queryCanBuild(int ybuffer) {
		if (j0 <= 0)
			return false;
		//Don't build if it would require leveling greater than tmlt.leveling
        for (int z = 0; z < bLength; z++)
			for (int x = 0; x < bWidth; x++)
                if (j0 - getSurfaceIJKPt(x, z, j0 - 1, true, 3)[1] > tmlt.leveling + 1)
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
            LayoutCode layoutCode = tmlt.buildOverStreets ? LayoutCode.TOWER : LayoutCode.TEMPLATE;
            if (layoutGenerator.layoutIsClear(this, tmlt.templateLayout, layoutCode)) {
                // TODO: shouldn't this be layoutCode, and not always set to TEMPALTE?
                layoutGenerator.setLayoutCode(this, tmlt.templateLayout, LayoutCode.TEMPLATE);
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
