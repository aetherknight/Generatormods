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

import generatormods.config.templates.TemplateRule;
import generatormods.config.templates.TemplateWall;
import generatormods.util.blocks.BlockAndMeta;
import generatormods.util.build.BlockProperties;
import generatormods.util.build.Dir;
import generatormods.util.build.Handedness;
import generatormods.util.build.RoofStyle;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import static generatormods.util.WorldUtil.SEA_LEVEL;
import static generatormods.util.WorldUtil.WORLD_MAX_Y;
import static generatormods.util.build.DirToMetaMappings.STAIRS_DIR_TO_META;

/*
 * BuildingUndergroundEntranceway builds a passageway from an underground city to the surface.
 */
public class BuildingUndergroundEntranceway extends Building {
	private final static int PASSAGE_HEIGHT = 6, PASSAGE_WIDTH = 4, SUPPORT_INTERVAL = 6;
	private final TemplateWall ws;
	public BuildingWall street;
	private final Block stairsID;

    public BuildingUndergroundEntranceway(int ID_, IBuildingConfig config, TemplateWall ws_,
            Dir dir_, int[] sourcePt) {
        super(ID_, config, ws_.TowerRule, dir_, Handedness.R_HAND, false, new int[] {PASSAGE_WIDTH,
                PASSAGE_HEIGHT, 0}, sourcePt);
		ws = ws_;
		stairsID = ws.rules[ws.template[0][0][ws.WWidth / 2]].primaryBlock.toStair();
		//wallBlockRule=new TemplateRule(new int[]{ws_.TowerBlock,0});
	}

	public boolean build() {
		for (; bLength < WORLD_MAX_Y - j0; bLength++) {
			if (BlockProperties.get(getBlockIdLocal(0, bLength + PASSAGE_HEIGHT, bLength)).isWater)
				return false;
            if (j0 + bLength > SEA_LEVEL - 10
                    && isArtificialWallBlock(0, bLength + PASSAGE_HEIGHT, bLength))
				return false;
            if (j0 + bLength > SEA_LEVEL
                    && (BlockProperties.get(getBlockIdLocal(0, bLength, bLength)).isWallable || BlockProperties
                            .get(getBlockIdLocal(PASSAGE_WIDTH - 1, bLength, bLength)).isWallable)) {
				bLength--;
				break;
			}
		}
		//build a tower to mark top
        BuildingTower tower =
                new BuildingTower(0, this, true, RoofStyle.DOME, bDir, bHand, false, 10, 7, 10,
                        getIJKPt(-3, bLength, bLength - 7));
		tower.build(0, 0, true);
		//entranceway
        for (int y = 0; y < bLength; y++) {
			for (int x = -1; x <= PASSAGE_WIDTH; x++) {
                for (int y1 = 1; y1 <= PASSAGE_HEIGHT; y1++) {
                    if (x == -1 || x == PASSAGE_WIDTH || y1 == PASSAGE_HEIGHT) {
                        if (BlockProperties.get(getBlockIdLocal(x, y + y1, y)).isFlowing)
                            setBlockLocal(x, y + y1, y, Blocks.stone);
					} else
                        setBlockLocal(x, y + y1, y, Blocks.air);
				}
			}
			for (int x = 0; x < PASSAGE_WIDTH; x++) {
                setBlockLocal(x, y, y, random.nextInt(100) < bRule.chance ? stairsID : Blocks.air,
                        STAIRS_DIR_TO_META.get(Dir.NORTH));
                buildDown(x, y - 1, y, TemplateRule.STONE_RULE, 20, 0, 3);
			}
            flushDelayed();
            if (y % SUPPORT_INTERVAL == 0 && y <= bLength - PASSAGE_HEIGHT) {
                for (int y1 = -1; y1 < PASSAGE_HEIGHT; y1++) {
                    setBlockLocal(0, y + y1, y, bRule);
                    setBlockLocal(PASSAGE_WIDTH - 1, y + y1, y, bRule);
				}
				for (int x = 0; x < PASSAGE_WIDTH; x++)
                    setBlockLocal(x, y + PASSAGE_HEIGHT - 1, y, bRule);
			}
            if (y % SUPPORT_INTERVAL == SUPPORT_INTERVAL / 2 && y <= bLength - PASSAGE_HEIGHT) {
				if (random.nextInt(2) == 0)
                    setBlockLocal(0, y + PASSAGE_HEIGHT - 3, y, BlockAndMeta.EAST_FACING_TORCH);
				if (random.nextInt(2) == 0)
                    setBlockLocal(PASSAGE_WIDTH - 1, y + PASSAGE_HEIGHT - 3, y,
                            BlockAndMeta.WEST_FACING_TORCH);
			}
		}
		//reinforce start of entrance
        for (int z = bLength; z >= bLength - PASSAGE_HEIGHT - 4; z--) {
            buildDown(-1, z >= bLength - PASSAGE_HEIGHT - 1 ? bLength - 1 : z + PASSAGE_HEIGHT, z,
                    bRule, 20, 7, 3);
            buildDown(PASSAGE_WIDTH, z >= bLength - PASSAGE_HEIGHT - 1 ? bLength - 1 : z
                    + PASSAGE_HEIGHT, z, bRule, 20, 7, 3);
            if (z < bLength - PASSAGE_HEIGHT) {
				for (int x = 0; x < PASSAGE_WIDTH; x++)
                    setBlockLocal(x, z + PASSAGE_HEIGHT, z, bRule);
			}
		}
		//for(int x=0; x<PASSAGE_WIDTH; x++) buildDown(x, bLength-1, bLength, bRule,20,4,3);
        flushDelayed();
        street =
                new BuildingWall(bID, config, ws, bDir.opposite(), bHand.opposite(), ws.MaxL, true,
                        getIJKPt((PASSAGE_WIDTH - ws.WWidth) / 2, 0, -1));
		street.plan(1, 0, BuildingWall.DEFAULT_LOOKAHEAD, true);
		return true;
	}

    @Override
    protected BlockAndMeta getDelayedStair(Block blc, int...block){
        return new BlockAndMeta(blc, block[3]);
    }
}
