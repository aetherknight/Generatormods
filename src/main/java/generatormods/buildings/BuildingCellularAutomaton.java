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

import generatormods.caruins.CARule;
import generatormods.caruins.CAState;
import generatormods.caruins.seeds.ISeed;
import generatormods.codex.BlockProperties;
import generatormods.common.BlockAndMeta;
import generatormods.common.BlockExtended;
import generatormods.common.Dir;
import generatormods.common.Handedness;
import generatormods.common.TemplateRule;
import generatormods.config.CARuinsConfig;
import generatormods.config.chests.ChestType;
import generatormods.util.IntUtil;
import generatormods.walledcity.LayoutCode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumSkyBlock;
import static generatormods.caruins.CAState.ALIVE;
import static generatormods.caruins.CAState.DEAD;
import static generatormods.codex.DirToMetaMappings.LADDER_DIR_TO_META;
import static generatormods.codex.DirToMetaMappings.STAIRS_DIR_TO_META;
import static generatormods.util.WorldUtil.HIT_WATER;
import static generatormods.util.WorldUtil.IGNORE_WATER;
import static generatormods.util.WorldUtil.SEA_LEVEL;
import static generatormods.util.WorldUtil.WORLD_MAX_Y;
import static generatormods.util.WorldUtil.findSurfaceJ;

/*
 * BuildingCellularAutomaton creates Cellular Automata-derived towers.
 */
public class BuildingCellularAutomaton extends Building {
	private final float MEAN_SIDE_LENGTH_PER_POPULATE = 15.0f;
	private final static int HOLE_FLOOR_BUFFER = 2, UNREACHED = -1;

    private CAState[][][] layers = null;
    public CAState[][] seed = null;
    private CAState[] birthRule = null;
    private CAState[] survivalRule = null;
	private final TemplateRule lowLightSpawnerRule, mediumLightNarrowSpawnerRule, mediumLightWideSpawnerRule;
	int[][] fBB;
	int zGround = 0;

    public BuildingCellularAutomaton(IBuildingConfig config, TemplateRule bRule_, Dir bDir_,
            Handedness axXHand_, boolean centerAligned_, int width, int height, int length,
            ISeed seed, CARule caRule_, TemplateRule[] spawnerRules, int[] sourcePt) {
        super(0, config, bRule_, bDir_, axXHand_, centerAligned_,
                new int[] {width, height, length}, sourcePt);
        this.seed = seed.makeSeed(random);
        if ((bWidth - this.seed.length) % 2 != 0)
			bWidth++; //so seed can be perfectly centered
        if ((bLength - this.seed[0].length) % 2 != 0)
			bLength++;
        birthRule = caRule_.getBirthRule();
        survivalRule = caRule_.getSurvivalRule();
        mediumLightNarrowSpawnerRule =
                spawnerRules != null ? spawnerRules[0]
                        : CARuinsConfig.DEFAULT_MEDIUM_LIGHT_NARROW_SPAWNER_RULE;
        mediumLightWideSpawnerRule =
                spawnerRules != null ? spawnerRules[1]
                        : CARuinsConfig.DEFAULT_MEDIUM_LIGHT_WIDE_SPAWNER_RULE;
        lowLightSpawnerRule =
                spawnerRules != null ? spawnerRules[2]
                        : CARuinsConfig.DEFAULT_LOW_LIGHT_SPAWNER_RULE;
	}

	public void build(boolean SmoothWithStairs, boolean makeFloors) {
		Block stairsBlock = SmoothWithStairs ? bRule.primaryBlock.toStair() : Blocks.air;
        Map<Dir, TemplateRule> stairs = new HashMap<Dir, TemplateRule>();
        stairs.put(Dir.NORTH, new TemplateRule(stairsBlock, STAIRS_DIR_TO_META.get(Dir.NORTH), bRule.chance));
        stairs.put(Dir.EAST, new TemplateRule(stairsBlock, STAIRS_DIR_TO_META.get(Dir.EAST), bRule.chance));
        stairs.put(Dir.SOUTH, new TemplateRule(stairsBlock, STAIRS_DIR_TO_META.get(Dir.SOUTH), bRule.chance));
        stairs.put(Dir.WEST, new TemplateRule(stairsBlock, STAIRS_DIR_TO_META.get(Dir.WEST), bRule.chance));
        int[] floorBlockCounts = new int[bHeight];
		ArrayList<ArrayList<int[]>> floorBlocks = new ArrayList<ArrayList<int[]>>();
		for (int m = 0; m < bHeight; m++) {
			floorBlockCounts[m] = 0;
			floorBlocks.add(new ArrayList<int[]>());
		}
		int centerX = (fBB[0][0] + fBB[1][0]) / 2;
		int[][] holeLimits = new int[bLength][2];
        for (int z = 0; z < bLength; z++) {
            holeLimits[z][0] = centerX;
            holeLimits[z][1] = centerX + 1;
		}
        for (int y = bHeight - 1; y >= 0; y--) {
            //for(int z=0; z<bLength; z++){
            //holeLimits[z][0]=centerX; holeLimits[z][1]=centerX+1; }
			for (int x = 0; x < bWidth; x++) {
                for (int z = 0; z < bLength; z++) {
                    //if(fBB[0][y]<=x && x<=fBB[1][y] && fBB[2][y]<=z && z<=fBB[3][y])
                    //	setBlockLocal(x,y,z,GLASS_ID);
                    if (layers[y][x][z] == ALIVE)
                        setBlockLocal(x, y, z, bRule);
                    else if (y > 0 && layers[y - 1][x][z] == ALIVE) { //if a floor block
						//if in central core
                        if (y < bHeight - 5 && fBB[0][y] <= x && x <= fBB[1][y] && fBB[2][y] <= z
                                && z <= fBB[3][y]) {
							if (makeFloors) {
                                floorBlocks.get(y).add(new int[] {x, z});
                                if (x - HOLE_FLOOR_BUFFER < holeLimits[z][0])
                                    holeLimits[z][0] = Math.max(fBB[0][y], x - HOLE_FLOOR_BUFFER);
                                if (x + HOLE_FLOOR_BUFFER > holeLimits[z][1])
                                    holeLimits[z][1] = Math.min(fBB[1][y], x + HOLE_FLOOR_BUFFER);
                                floorBlockCounts[y]++;
							}
						}
						//try smoothing with stairs here
                        else if (stairsBlock != Blocks.air
                                && (y == bHeight - 1 || layers[y + 1][x][z] != ALIVE)) {
                            // z+1 present and (we are at the edge or...
                            if (z + 1 < bLength
                                    && layers[y][x][z + 1] == ALIVE
                                    && (z - 1 < 0 ||
                                    // z-1 empty and..
                                    (layers[y][x][z - 1] != ALIVE
                                    // not obstructing gaps to the sides
                                            && (x + 1 == bWidth || !(layers[y][x + 1][z] != ALIVE && layers[y][x + 1][z - 1] == ALIVE)) && (x - 1 < 0 || !(layers[y][x - 1][z] != ALIVE && layers[y][x - 1][z - 1] == ALIVE)))
                                            && random.nextInt(100) < bRule.chance))
                                setBlockLocal(x, y, z, stairs.get(Dir.NORTH));
                            else if (z - 1 >= 0
                                    && layers[y][x][z - 1] == ALIVE
                                    && (z + 1 == bLength || (layers[y][x][z + 1] != ALIVE
                                            && (x + 1 == bWidth || !(layers[y][x + 1][z] != ALIVE && layers[y][x + 1][z + 1] == ALIVE)) && (x - 1 < 0 || !(layers[y][x - 1][z] != ALIVE && layers[y][x - 1][z + 1] == ALIVE)))
                                            && random.nextInt(100) < bRule.chance))
                                setBlockLocal(x, y, z, stairs.get(Dir.SOUTH));
                            else if (x + 1 < bWidth
                                    && layers[y][x + 1][z] == ALIVE
                                    && (x - 1 < 0 || (layers[y][x - 1][z] != ALIVE
                                            && (z + 1 == bLength || !(layers[y][x][z + 1] != ALIVE && layers[y][x - 1][z + 1] == ALIVE)) && (z - 1 < 0 || !(layers[y][x][z - 1] != ALIVE && layers[y][x - 1][z - 1] == ALIVE)))
                                            && random.nextInt(100) < bRule.chance))
                                setBlockLocal(x, y, z, stairs.get(Dir.EAST));
                            else if (x - 1 >= 0
                                    && layers[y][x - 1][z] == ALIVE
                                    && (x + 1 == bWidth || (layers[y][x + 1][z] != ALIVE
                                            && (z + 1 == bLength || !(layers[y][x][z + 1] != ALIVE && layers[y][x + 1][z + 1] == ALIVE)) && (z - 1 < 0 || !(layers[y][x][z - 1] != ALIVE && layers[y][x + 1][z - 1] == ALIVE)))
                                            && random.nextInt(100) < bRule.chance))
                                setBlockLocal(x, y, z, stairs.get(Dir.WEST));
						}
					}
				}
			}
			//now clear a hole surrounding the central floor volume
            for (int z = 0; z < bLength; z++)
                for (int x = holeLimits[z][0] + 1; x <= holeLimits[z][1] - 1; x++)
                    if (layers[y][x][z] != ALIVE
                            && !BlockProperties.get(getBlockIdLocal(x, y, z)).isArtificial)
                        setBlockLocal(x, y, z, Blocks.air);
			//then gradually taper hole limits...
            if (y % 2 == 0) {
                for (int z = 0; z < bLength; z++) {
                    holeLimits[z][0] =
                            holeLimits[z][0] < centerX ? (holeLimits[z][0] + 1) : centerX;
                    holeLimits[z][1] =
                            holeLimits[z][1] > (centerX + 1) ? (holeLimits[z][1] - 1) : centerX + 1;
				}
			}
		}
        flushDelayed();
		if (makeFloors)
			buildFloors(floorBlockCounts, floorBlocks);
	}

	public void buildFloors(int[] floorBlockCounts, ArrayList<ArrayList<int[]>> floorBlocks) {
		ArrayList<int[]> floors = new ArrayList<int[]>();
		while (true) {
			int maxFloorBlocks = floorBlockCounts[1], maxFloor = 1;
			for (int floor = 2; floor < bHeight - 5; floor++) {
				if (floorBlockCounts[floor - 1] + floorBlockCounts[floor] + floorBlockCounts[floor + 1] > maxFloorBlocks) { //add the two floors since we can raise one to the other
					maxFloorBlocks = floorBlockCounts[floor - 1] + floorBlockCounts[floor] + floorBlockCounts[floor + 1];
					maxFloor = floor;
				}
			}
			if (maxFloorBlocks > 20) {
				boolean[][] layout = new boolean[bWidth][bLength];
				for (int x = 0; x < bWidth; x++)
					for (int y = 0; y < bLength; y++)
						layout[x][y] = false;
				for (int[] pt : floorBlocks.get(maxFloor - 1))
					makeFloorCrossAt(pt[0], maxFloor, pt[1], layout);
				for (int[] pt : floorBlocks.get(maxFloor))
					makeFloorCrossAt(pt[0], maxFloor, pt[1], layout);
				for (int[] pt : floorBlocks.get(maxFloor + 1))
					makeFloorCrossAt(pt[0], maxFloor, pt[1], layout);
				floors.add(new int[] { maxFloor, maxFloorBlocks });
				if (maxFloor - 3 >= 0)
					floorBlockCounts[maxFloor - 3] = 0;
				if (maxFloor - 2 >= 0)
					floorBlockCounts[maxFloor - 2] = 0;
				floorBlockCounts[maxFloor - 1] = 0;
				floorBlockCounts[maxFloor] = 0;
				floorBlockCounts[maxFloor + 1] = 0;
				if (maxFloor + 2 < bHeight)
					floorBlockCounts[maxFloor + 2] = 0;
				if (maxFloor + 3 < bHeight)
					floorBlockCounts[maxFloor + 3] = 0;
			} else
				break;
            flushDelayed();
		}
		//populate
		Collections.sort(floors, new Comparator<int[]>() {
			@Override
			public int compare(int[] o1, int[] o2) {
				int a = o1[0];
				int b = o2[0];
				return a < b ? -1 : a == b ? 0 : 1;
			}
		});
		for (int m = floors.size() - 1; m >= 0; m--) {
			if (m > 0)
				populateLadderOrStairway(floors.get(m - 1)[0], floors.get(m)[0], true);
			do {
				populateFloor(floors.get(m)[0], floors.get(m)[1]);
			} while (random.nextFloat() < 1.0f - MEAN_SIDE_LENGTH_PER_POPULATE / MathHelper.sqrt_float(floors.get(m)[1]));
		}
	}

	public boolean plan(boolean bury, int MinHeightBeforeOscillation) {
        //layers is y-flipped from usual orientation so y=0 is the top
        layers = new CAState[bHeight][bWidth][bLength];
        for (int y = 0; y < bHeight; y++)
			for (int x = 0; x < bWidth; x++)
                for (int z = 0; z < bLength; z++)
                    layers[y][x][z] = DEAD;
		int[][] BB = new int[4][bHeight];
		BB[0][0] = (bWidth - seed.length) / 2;
		BB[1][0] = (bWidth - seed.length) / 2 + seed.length - 1;
		BB[2][0] = (bLength - seed[0].length) / 2;
		BB[3][0] = (bLength - seed[0].length) / 2 + seed[0].length - 1;
		for (int x = 0; x < seed.length; x++)
            for (int z = 0; z < seed[0].length; z++)
                layers[0][BB[0][0] + x][BB[2][0] + z] = seed[x][z];
		int crystallizationHeight = UNREACHED;
        for (int y = 1; y < bHeight; y++) {
			boolean layerIsAlive = false;
            boolean layerIsFixed = crystallizationHeight == UNREACHED && y >= 1;
            boolean layerIsPeriod2 = crystallizationHeight == UNREACHED && y >= 2;
            boolean layerIsPeriod3 = crystallizationHeight == UNREACHED && y >= 3;
            BB[0][y] = bWidth / 2;
            BB[1][y] = bWidth / 2;
            BB[2][y] = bLength / 2;
            BB[3][y] = bLength / 2;
            for (int x = Math.max(0, BB[0][y - 1] - 1); x <= Math.min(bWidth - 1, BB[1][y - 1] + 1); x++) {
                for (int z = Math.max(0, BB[2][y - 1] - 1); z <= Math.min(bLength - 1,
                        BB[3][y - 1] + 1); z++) {
					//try the 8 neighboring points in previous layer
					int neighbors = 0;
					for (int x1 = Math.max(x - 1, 0); x1 <= Math.min(x + 1, bWidth - 1); x1++)
                        for (int y1 = Math.max(z - 1, 0); y1 <= Math.min(z + 1, bLength - 1); y1++)
                            if (!(x1 == x && y1 == z))
                                if (layers[y - 1][x1][y1] == ALIVE)
                                    neighbors += 1;
					//update this layer based on the rule
                    if (layers[y - 1][x][z] == ALIVE)
                        layers[y][x][z] = survivalRule[neighbors];
                    else
                        layers[y][x][z] = birthRule[neighbors];
					//culling checks and update bounding box
                    if (layers[y][x][z] == ALIVE) {
                        if (x < BB[0][y])
                            BB[0][y] = x;
                        if (x > BB[1][y])
                            BB[1][y] = x;
                        if (z < BB[2][y])
                            BB[2][y] = z;
                        if (z > BB[3][y])
                            BB[3][y] = z;
						layerIsAlive = true;
					}
                    if (layers[y][x][z] != layers[y - 1][x][z])
						layerIsFixed = false;
                    if (y >= 2 && layers[y][x][z] != layers[y - 2][x][z])
						layerIsPeriod2 = false;
                    if (y >= 3 && layers[y][x][z] != layers[y - 3][x][z])
						layerIsPeriod3 = false;
				}
			}
			if (!layerIsAlive) {
                if (y <= MinHeightBeforeOscillation) {
                    logger.debug(
                            "Rejecting because layer {} is not alive and is less than or equal to MinHeightBeforeOscillation: {}",
                            y, MinHeightBeforeOscillation);
					return false;
                }
                bHeight = y;
				break;
			}
			if (layerIsFixed) {
                if (y - 1 <= MinHeightBeforeOscillation) {
                    logger.debug(
                            "Rejecting because layer {} is fixed and layer {} is less than or equal to MinHeightBeforeOscillation: {}",
                            y, (y - 1), MinHeightBeforeOscillation);
					return false;
                }
                crystallizationHeight = y - 1;
			}
			if (layerIsPeriod2) {
                if (y - 2 <= MinHeightBeforeOscillation) {
                    logger.debug(
                            "Rejecting because layer {} is period2 and layer {} is less than or equal to MinHeightBeforeOscillation: {}",
                            y, (y - 2), MinHeightBeforeOscillation);
					return false;
                }
                crystallizationHeight = y - 2;
			}
			if (layerIsPeriod3) {
                if (y - 3 <= MinHeightBeforeOscillation) {
                    logger.debug(
                            "Rejecting because layer {} is period3 and layer {} is less than or equal to MinHeightBeforeOscillation: {}",
                            y, (y - 3), MinHeightBeforeOscillation);
					return false;
                }
                crystallizationHeight = y - 3;
			}
            if (crystallizationHeight > UNREACHED && y > 2 * crystallizationHeight) {
                bHeight = y;
				break;
			}
		}
		//prune top layer
		int topLayerCount = 0, secondLayerCount = 0;
		for (int x = 0; x < bWidth; x++) {
            for (int z = 0; z < bLength; z++) {
                if (layers[0][x][z] == ALIVE)
                    topLayerCount += 1;
                if (layers[1][x][z] == ALIVE)
                    secondLayerCount += 1;
			}
		}
		if (2 * topLayerCount >= 3 * secondLayerCount) {
			for (int x = 0; x < bWidth; x++) {
                for (int z = 0; z < bLength; z++) {
                    if (layers[0][x][z] == ALIVE && layers[1][x][z] == DEAD)
                        layers[0][x][z] = DEAD;
				}
			}
		}
		//now resize building dimensions and shift down
        int minX = IntUtil.min(BB[0]);
        int maxX = IntUtil.max(BB[1]);
        int minZ = IntUtil.min(BB[2]);
        int maxZ = IntUtil.max(BB[3]);

		bWidth = maxX - minX + 1;
        bLength = maxZ - minZ + 1;
        //do a height check to see we are not at the edge of a cliff etc.
        if (!shiftBuidlingJDown(15)) {
            logger.debug("Rejecting because the surface below the building varies by more than 15 meters");
            return false;
        }
		boolean hitWater = false;
        if (birthRule[2] != ALIVE) { //if not a 2-rule
			int[] heights = new int[] { findSurfaceJ(world, getI(bWidth - 1, 0), getK(bWidth - 1, 0), j0 + 10, false, 0),
					findSurfaceJ(world, getI(0, bLength - 1), getK(0, bLength - 1), j0 + 10, false, 0),
					findSurfaceJ(world, getI(bWidth - 1, bLength - 1), getK(bWidth - 1, bLength - 1), j0 + 10, false, 0),
					findSurfaceJ(world, getI(bWidth / 2, bLength / 2), getK(bWidth / 2, bLength / 2), j0 + 10, false, 0) };
			for (int height : heights)
				hitWater |= height == HIT_WATER;
		}
		if (j0 + bHeight > WORLD_MAX_Y - 1)
			j0 = WORLD_MAX_Y - bHeight - 1; //stay 1 below top to avoid lighting problems
		if (bury && !hitWater) {
            zGround =
                    birthRule[2] == ALIVE ? Math.max(0,
                            bHeight - bWidth / 3 - random.nextInt(bWidth)) : random
                            .nextInt(3 * bHeight / 4);
			if (j0 - zGround < 5)
				zGround = j0 - 5;
			j0 -= zGround; //make ruin partially buried
		}
		//shift level and floor arrays
        CAState[][][] layers2 = new CAState[bHeight][bWidth][bLength]; //shrunk in all 3 dimensions
		fBB = new int[4][bHeight];
        for (int y = 0; y < bHeight; y++) {
            int lZ = bHeight - y - 1;
			for (int x = 0; x < bWidth; x++) {
                for (int z = 0; z < bLength; z++) {
                    layers2[y][x][z] = layers[lZ][x + minX][z + minZ];
				}
			}
			//floor bounding box
            fBB[0][y] = BB[0][lZ] - minX + (BB[1][lZ] - BB[0][lZ]) / 4;
            fBB[1][y] = BB[1][lZ] - minX - (BB[1][lZ] - BB[0][lZ]) / 4;
            fBB[2][y] = BB[2][lZ] - minZ + (BB[3][lZ] - BB[2][lZ]) / 4;
            fBB[3][y] = BB[3][lZ] - minZ - (BB[3][lZ] - BB[2][lZ]) / 4;
		}
		layers = layers2;
		return true;
	}

	//unlike other Buildings, this should be called after plan()
	public boolean queryCanBuild(int ybuffer, boolean nonLayoutFrameCheck) {
        LayoutCode layoutCode = bWidth * bLength > 120 ? LayoutCode.TOWER : LayoutCode.TEMPLATE;
        if (layoutGenerator != null) {
            if (layoutGenerator.layoutIsClear(getIJKPt(0, 0, ybuffer),
                    getIJKPt(bWidth - 1, 0, bLength - 1), layoutCode))
                layoutGenerator.setLayoutCode(getIJKPt(0, 0, ybuffer),
                        getIJKPt(bWidth - 1, 0, bLength - 1), layoutCode);
            else {
                logger.debug("Cannot build because layout is not clear");
				return false;
            }
		} else if (nonLayoutFrameCheck) {
            if (isObstructedFrame(0, ybuffer)) {
                logger.debug("Cannot build because the frame is obstructed");
				return false;
            }
		}
		return true;
	}

	public boolean shiftBuidlingJDown(int maxShift) {
		//try 4 corners and center
		int[] heights = new int[] { findSurfaceJ(world, getI(bWidth - 1, 0), getK(bWidth - 1, 0), j0 + 10, false, IGNORE_WATER),
				findSurfaceJ(world, getI(0, bLength - 1), getK(0, bLength - 1), j0 + 10, false, IGNORE_WATER),
				findSurfaceJ(world, getI(bWidth - 1, bLength - 1), getK(bWidth - 1, bLength - 1), j0 + 10, false, IGNORE_WATER),
				findSurfaceJ(world, getI(bWidth / 2, bLength / 2), getK(bWidth / 2, bLength / 2), j0 + 10, false, IGNORE_WATER) };
        int minHeight = IntUtil.min(heights);
        if (IntUtil.max(heights) - minHeight > maxShift)
			return false;
		else
			j0 = minHeight;
		return true;
	}

    private void makeFloorAt(int x, int y, int z, boolean[][] layout) {
        if (layout[x][z])
			return;
        if (BlockProperties.get(getBlockIdLocal(x, y, z)).isArtificial
                && BlockProperties.get(getBlockIdLocal(x, y + 1, z)).isArtificial) { // pillar
            if (!BlockProperties.get(getBlockIdLocal(x, y + 2, z)).isArtificial)
                setBlockLocal(x, y + 2, z, bRule);
			return;
		}
        if (!BlockProperties.get(getBlockIdLocal(x, y - 1, z)).isArtificial) { //raise to floor
            BlockAndMeta idAndMeta = bRule.getNonAirBlock(world.rand);
            setBlockWithLightingLocal(x, y - 1, z, idAndMeta, true);
		}
        removeBlockWithLighting(x, y, z);
        removeBlockWithLighting(x, y + 1, z);
        layout[x][z] = true;
	}

    private void makeFloorCrossAt(int x, int y, int z, boolean[][] layout) {
        makeFloorAt(x, y, z, layout);
        if (x - 1 >= fBB[0][y])
            makeFloorAt(x - 1, y, z, layout);
        if (x + 1 <= fBB[1][y])
            makeFloorAt(x + 1, y, z, layout);
        if (z - 1 >= fBB[2][y])
            makeFloorAt(x, y, z - 1, layout);
        if (z + 1 <= fBB[2][y])
            makeFloorAt(x, y, z + 1, layout);
	}

    private ChestType pickCAChestType(int y) {
        if (Math.abs(zGround - y) > random.nextInt(1 + y > zGround ? (bHeight - zGround) : zGround)
                && (y > zGround ? (bHeight - zGround) : zGround) > 20)
            return random.nextBoolean() ? ChestType.MEDIUM : ChestType.HARD;
        else
            return random.nextBoolean() ? ChestType.EASY : ChestType.MEDIUM;
	}

    private void populateFloor(int y, int floorBlocks) {
		TemplateRule spawnerSelection = null;
        int fWidth = fBB[1][y] - fBB[0][y], fLength = fBB[3][y] - fBB[2][y];
		if (fWidth <= 0 || fLength <= 0)
			return;
		//spawners
		if (random.nextInt(100) < 70) {
			for (int tries = 0; tries < 8; tries++) {
                int x = random.nextInt(fWidth) + fBB[0][y], z = random.nextInt(fLength) + fBB[2][y];
                if (isFloor(x, y, z)) {
                    int[] pt = getIJKPt(x, y, z);
					int lightVal = world.getSavedLightValue(EnumSkyBlock.Sky, pt[0], pt[1], pt[2]);
					//Choose spawner types. There is some kind of bug where where lightVal coming up as zero, even though it is not.
                    if (lightVal < 5 && !(lightVal == 0 && j0 + y > SEA_LEVEL))
						spawnerSelection = lowLightSpawnerRule;
					else if (lightVal < 10)
						spawnerSelection = floorBlocks > 70 ? mediumLightWideSpawnerRule : mediumLightNarrowSpawnerRule;
						if (spawnerSelection != null) {
                            setBlockLocal(x, y, z, spawnerSelection);
							break;
						}
				}
			}
		}
		//chest
		if (random.nextInt(100) < (spawnerSelection == null ? 20 : 70)) {
			for (int tries = 0; tries < 8; tries++) {
                int x = random.nextInt(fWidth) + fBB[0][y], z = random.nextInt(fLength) + fBB[2][y];
                if (isFloor(x, y, z)) {
                    setBlockLocal(x, y - 1, z, new BlockExtended(Blocks.chest, 0,
                            pickCAChestType(y).toString()));
                    setBlockLocal(x, y - 2, z, bRule);
					if (random.nextBoolean()) {
						break; //chance of > 1 chest. Expected # of chests is one.
					}
				}
			}
		}
		//1 TNT trap
		int s = random.nextInt(1 + random.nextInt(5)) - 2;
		for (int tries = 0; tries < s; tries++) {
            int x = random.nextInt(fWidth) + fBB[0][y], z = random.nextInt(fLength) + fBB[2][y];
            if (isFloor(x, y, z)) {
                setBlockLocal(x, y, z, Blocks.stone_pressure_plate);
                setBlockLocal(x, y - 1, z, Blocks.tnt);
                setBlockLocal(x, y - 2, z, bRule);
			}
		}
		//2 TNT trap
		s = spawnerSelection == null ? random.nextInt(1 + random.nextInt(7)) - 2 : 0;
		for (int tries = 0; tries < s; tries++) {
            int x = random.nextInt(fWidth) + fBB[0][y], z = random.nextInt(fLength) + fBB[2][y];
            if (isFloor(x, y, z) && isFloor(x, y, z = 1)) {
				for (int x1 = x - 1; x1 <= x + 1; x1++)
                    for (int z1 = z - 1; z1 <= z + 2; z1++)
                        for (int y1 = y - 3; y1 <= y - 2; y1++)
                            setBlockLocal(x1, y1, z1, bRule);
                setBlockLocal(x, y, z, Blocks.stone_pressure_plate);
                setBlockLocal(x, y - 2, z, Blocks.tnt);
                setBlockLocal(x, y - 2, z + 1, Blocks.tnt);
			}
		}
		//dispenser trap
		if (spawnerSelection == null || random.nextBoolean()) {
			for (int tries = 0; tries < 10; tries++) {
                int x = random.nextInt(fWidth) + fBB[0][y], z = random.nextInt(fLength) + fBB[2][y];
                BuildingDispenserTrap bdt =
                        new BuildingDispenserTrap(config, bRule, Dir.randomDir(random), 1,
                                getIJKPt(x, y, z));
				if (bdt.queryCanBuild(2)) {
                    bdt.build(random.nextBoolean() ? BuildingDispenserTrap.MissileType.ARROW
                            : BuildingDispenserTrap.MissileType.DAMAGE_POTION, true);
					break;
				}
			}
		}
	}

    private void populateLadderOrStairway(int y1, int y2, boolean buildStairs) {
        int fWidth = fBB[1][y2] - fBB[0][y2];
        int fLength = fBB[3][y2] - fBB[2][y2];
		if (fWidth <= 0 || fLength <= 0)
			return;
		BuildingSpiralStaircase bss;
		for (int tries = 0; tries < 8; tries++) {
            int x = random.nextInt(fWidth) + fBB[0][y2];
            int z = random.nextInt(fLength) + fBB[2][y2];
            if (isFloor(x, y2, z)) {
				Dir dir = Dir.randomDir(random);
                if (buildStairs
                        && (bss =
                                new BuildingSpiralStaircase(config, bRule, dir, Handedness.R_HAND,
                                        false, y1 - y2 + 1, getIJKPt(x, y2 - 1, z)))
                                .bottomIsFloor()) {
					bss.build(0, 0);
                } else if (isFloor(x, y1, z)) {
					//ladder
                    for (int y = y1; y < y2; y++) {
						setBlockLocal(x + dir.x, y, z + dir.z, bRule);
                        setBlockLocal(x, y, z, Blocks.ladder,
                                LADDER_DIR_TO_META.get(dir.opposite()));
					}
				}
				return;
			}
		}
	}
}
