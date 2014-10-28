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
import generatormods.common.Dir;
import generatormods.common.Handedness;
import generatormods.common.TemplateRule;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

import static generatormods.common.DirMeta.STAIRS_DIR_TO_META;

/*
 * BuildingSpiralStaircase plans and builds a 3x3 spiral staircase down from origin.
 */
public class BuildingSpiralStaircase extends Building {
    public BuildingSpiralStaircase(IBuildingConfig config, TemplateRule bRule_, Dir bDir_,
            Handedness axXHand_, boolean centerAligned_, int height, int[] sourcePt) {
        super(0, config, bRule_, bDir_, axXHand_, centerAligned_, new int[] {3, height, 3},
                sourcePt);
	}

	public boolean bottomIsFloor() {
		int x = calcBottomX(bHeight);
        int z = calcBottomZ(bHeight);
		Dir btDir = Dir.NORTH.rotate(-bHeight - 2);
		return isFloor(x + btDir.x, bHeight, z + btDir.z);
	}

    /**
     * Builds a clockwise down spiral staircase with central column at (x,y,z)
     * with end at top going in local direction topDir.
     *
     * @param zP is the z-value of the endpoint of a potential bottom passage
     * link. If zP==0, then no bottom passage.
     * <p>
     * y is fixed at top and bottom y varies depending on zP
     * <p>
     * Example, bheight=-7
     * <pre>
     * *|	y=0 leading stair
     * *|	y=-1 (start of loop)
     * o|
     *  |x
     *  xo
     * o|
     * o|
     *  |x
     *  |x	y=bHeight=-7, (2-in-a-row bottom stair), xfinal=2, yfinal=0
     * </pre>
     */
	public void build(int extraTopStairs, int zP) {
		Block stairsBlockId = bRule.primaryBlock.toStair();
		Dir sDir = Dir.SOUTH;
		setBlockLocal(1, 0, 1, bRule);
        if (zP == 1 && zP == 2)
            zP = 0;
        int jB0 = getSurfaceIJKPt(0, zP, j0 + bHeight + 2, true, 0)[1] + 1;
        int jB2 = getSurfaceIJKPt(2, zP, j0 + bHeight + 2, true, 0)[1] + 1;
        int pYInc = Integer.signum(zP);
		for (int n = 0; n <= extraTopStairs; n++)
			buildStairwaySegment(0, n, -n, 3, stairsBlockId, sDir);
        int x = 0, z = 1;
        setBlockLocal(x, 2, z, Blocks.air);
        for (int y = -1; y >= bHeight; y--) {
            buildStairwaySegment(x, y, z, 2, stairsBlockId, sDir);
            setBlockLocal(1, y, 1, bRule); //central column
			x -= sDir.x;
            z -= sDir.z;
            if (y == bHeight + 1) {
                y--; //bottommost stair is two in a row
                buildStairwaySegment(x, y, z, 3, stairsBlockId, sDir);
                setBlockLocal(1, y, 1, bRule);
				x -= sDir.x;
                z -= sDir.z;
			}
            buildHallwaySegment(x, y, z, 3);
			//Bottom stair can start from 3 out of 4 positions
			// pYInc
			//  ^
			//  s3 > s0
			//	^   v
			//  s2 < s1
            if (zP != 0) {
                int yP = (x == 0 ? jB0 : jB2) - j0;
                if (z == pYInc + 1 && Math.abs(z - zP) > y - yP //s3
                        || z == pYInc + 1 && Math.abs(z - zP) >= y - yP && sDir.z != 0 //s0
                        || z != pYInc + 1 && Math.abs(z - zP) > y - yP && sDir.rotate(1).z != 0) //s2
				{
					if (sDir.z != 0) {
                        setBlockLocal(x, y - 1, z, stairsBlockId, STAIRS_DIR_TO_META.get(sDir));
                        y--;
					}
                    for (int y1 = z + pYInc; y1 != zP; y1 += pYInc) {
                        if (y - yP > 0) {
                            y--;
                            buildStairwaySegment(x, y, y1, 3, stairsBlockId, Dir.EAST.rotate(pYInc));
						} else {
                            if (y1 == pYInc + 1 && !isWallBlock(x, y, y1 - pYInc)) //avoid undermining stairway above
                                buildHallwaySegment(x, y, y1, 2);
							else
                                buildHallwaySegment(x, y, y1, 3);
						}
					}
					break;
				}
			}
			sDir = sDir.rotate(1);
			x -= sDir.x;
            z -= sDir.z;
		}
        flushDelayed();
	}

    private void buildHallwaySegment(int x, int y, int z, int height) {
        setBlockLocal(x, y - 1, z, bRule);
        for (int z1 = y; z1 < y + height; z1++)
            setBlockLocal(x, z1, z, Blocks.air);
	}

    private void buildStairwaySegment(int x, int y, int z, int height, Block stairsBlockId, Dir sDir) {
        setBlockLocal(x, y - 1, z, bRule);
        setBlockLocal(x, y, z, stairsBlockId, STAIRS_DIR_TO_META.get(sDir));
        for (int z1 = y + 1; z1 <= y + height; z1++)
            setBlockLocal(x, z1, z, Blocks.air);
	}

    //calcBottomX, calcBottomZ are for use when zP==0
    private static int calcBottomX(int height) {
		if (height == 1)
			return 0;
		return 2 * ((-height - 1) / 2 % 2);
	}

    private static int calcBottomZ(int height) {
		if (height == 1)
			return 1;
		return 2 * (-height / 2 % 2);
	}

    @Override
    protected BlockAndMeta getDelayedStair(Block blc, int...block){
        return new BlockAndMeta(blc, block[3]);
    }
}
