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
import generatormods.common.Handedness;
import generatormods.common.RoofStyle;
import generatormods.common.Shape;
import generatormods.common.TemplateRule;
import generatormods.walledcity.LayoutCode;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import static generatormods.common.WorldHelper.WORLD_MAX_Y;

/*
 * BuildingTower builds a procedurally generated tower.
 */
public class BuildingTower extends Building {
	public final static int FLOOR_HAUNTED_CHANCE = 50, HAUNTED_CHEST_CHANCE = 60;
	public final static int TOWER_UNIV_MIN_WIDTH = 5, TOWER_LEVELING = 12;
	public final static int SURFACE_PORTAL_ODDS = 20, NETHER_PORTAL_ODDS = 10;
	public final static int BOOKSHELF_ODDS = 3, BED_ODDS = 5, CAULDRON_ODDS = 8, BREWING_STAND_ODDS = 8, ENCHANTMENT_TABLE_ODDS = 12;

    /**
     * Builds a tower.
     *
     * @param doorOffset1 X-offset of the ground floor door from center
     * @param doorOffset2 Second X-offset of the ground floor door from center
     * @param hanging If true, taper away tower base if hanging over air or water.
     * @return True if tower was built (dependency: buildOver)
     */
	public void build(int doorOffset1, int doorOffset2, boolean hanging) {
		//check against spawner chance to see if haunted.
		//If hasUndeadSpawner =>	Don't make torches anywhere in tower.
		//							Each floor has a 50% chance of building spawners except ground floor.
		//If floor hasUndeadSpawner =>	Always build spawner_count spawners (resampling for each spawner).
		//								No windows/doors unless ground floor.
		boolean undeadTower = false, ghastTower = false;
		if (SpawnerRule != TemplateRule.RULE_NOT_PROVIDED) {
			if(SpawnerRule.hasUndeadSpawner())
                undeadTower = true;
            ghastTower =
                    roofStyle == RoofStyle.CRENEL
                            && SpawnerRule.getBlockOrHole(world.rand).equals(GHAST_SPAWNER);
			if (ghastTower || random.nextInt(100) > SpawnerRule.chance)
				undeadTower = false;
		}
		if (undeadTower && bHeight - baseHeight < 9)
			bHeight = baseHeight + 9;
		if (baseHeight < 0)
			baseHeight = 0;
		//buffer - dimensions have an offset of one on each side (except top) to fit in roof overhang and floor.
		//So to translate from Building coord system need to add +1 to all entries, annoying but that's life.
		buffer = new BlockAndMeta[bWidth + 2][bHeight + minHorizDim + 3][bLength + 2];
		for (int x1 = 0; x1 < bWidth + 2; x1++)
			for (int y1 = 0; y1 < bLength + 2; y1++)
				for (int z1 = 0; z1 < bHeight + minHorizDim + 3; z1++)
					buffer[x1][z1][y1] = PRESERVE_BLOCK;
		//========================================= build it ==========================================
		//*** sidewalls and base ***
		for (int x1 = 0; x1 < bWidth; x1++) {
			for (int y1 = 0; y1 < bLength; y1++) {
				if (!(circular && circle_shape[x1][y1] < 0)) {
					//buffer[x1+1][THeight][y1+1]=TRule.getBlockOrHole(random);
					for (int z1 = baseHeight - 1; z1 < bHeight; z1++) {
						if (circular && (circle_shape[x1][y1] == 1) //circular sidewalls condition
								|| !circular && (x1 == 0 || x1 == bWidth - 1 || y1 == 0 || y1 == bLength - 1)) //rectangular sidewalls condition
						{
							buffer[x1 + 1][z1 + 1][y1 + 1] = bRule.getBlockOrHole(world.rand);
						} else
							buffer[x1 + 1][z1 + 1][y1 + 1] = HOLE_BLOCK_NO_LIGHTING;
					}
					//column above source point
					for (int z1 = -1; z1 < baseHeight - 1; z1++)
						buffer[x1 + 1][z1 + 1][y1 + 1] = bRule.getBlockOrHole(world.rand);
					//column below source point, set zmin to taper overhangs
					//int zmin=hanging && y1>=TWidth/2 && isWallable(x1,-BUILDDOWN,y1) ?  Math.max(2*(y1-TWidth/2)+3*Math.abs(x1-TWidth/2)-5*TWidth/2,-BUILDDOWN) : -BUILDDOWN;
					buildDown(x1, -2, y1, bRule, TOWER_LEVELING, 2, (bLength - y1 - 1) / 2);
				}
			}
		}
		//*** floors and windows/doors ***
		int sideWindowY = bLength / 2;
		for (int z1 = baseHeight - 1; z1 < bHeight - 3; z1 += 4) {
			//windows/doors
			int winDoorX1 = Math.min(Math.max(bWidth / 2 + (z1 == baseHeight - 1 ? doorOffset1 : 0), 1), bWidth - 2);
			int winDoorX2 = Math.min(Math.max(bWidth / 2 + (z1 == baseHeight - 1 ? doorOffset2 : 0), 1), bWidth - 2);
			int torchX1 = winDoorX1 + (winDoorX1 == bWidth - 2 ? -1 : 1);
			int torchX2 = winDoorX2 + (winDoorX2 == bWidth - 2 ? -1 : 1);
			boolean floorHasUndeadSpawner = undeadTower && z1 > baseHeight - 1 && random.nextInt(100) < FLOOR_HAUNTED_CHANCE;
			if (z1 == baseHeight - 1 || !floorHasUndeadSpawner) {
				int winH = z1 > baseHeight - 1 ? 2 : 3;
				buildWindowOrDoor(0, z1 + 2, sideWindowY, -1, 0, winH);
				buildWindowOrDoor(bWidth - 1, z1 + 2, sideWindowY, 1, 0, winH);
				buildWindowOrDoor(winDoorX1, z1 + 2, 0, 0, -1, winH);
				buildWindowOrDoor(winDoorX2, z1 + 2, bLength - 1, 0, 1, winH);
			}
			//floor
			for (int y1 = 1; y1 < bLength - 1; y1++)
				for (int x1 = 1; x1 < bWidth - 1; x1++)
					if (!circular || circle_shape[x1][y1] == 0)
						buffer[x1 + 1][z1 + 1][y1 + 1] = bRule.primaryBlock.get() == Blocks.log ? new BlockAndMeta(Blocks.planks, 0): bRule.getBlockOrHole(world.rand);
						//door torches
						if (!undeadTower && bRule.chance == 100) {
							buffer[torchX1 + 1][z1 + 3 + 1][1 + (circular && bLength == 6 ? 1 : 0) + 1] = NORTH_FACE_TORCH_BLOCK;
							buffer[torchX2 + 1][z1 + 3 + 1][bLength - 2 - (circular && bLength == 6 ? 1 : 0) + 1] = SOUTH_FACE_TORCH_BLOCK;
						}
						//*** mob spawners***
						if (floorHasUndeadSpawner && z1 == baseHeight + 4 && bRule.chance == 100) {
							for (int x1 = bWidth / 2 - 1; x1 <= bWidth / 2 + 1; x1++) {
								for (int y1 = 1; y1 < Math.min(3, sideWindowY - 1); y1++)
									if (!circular || circle_shape[x1][y1] == 0)
										buffer[x1 + 1][z1 + 1][y1 + 1] = HOLE_BLOCK_LIGHTING;
								for (int y1 = bLength - Math.min(3, sideWindowY - 1); y1 < bLength - 1; y1++)
									if (!circular || circle_shape[x1][y1] == 0)
										buffer[x1 + 1][z1 + 1][y1 + 1] = HOLE_BLOCK_LIGHTING;
							}
						} else if (SpawnerRule != TemplateRule.RULE_NOT_PROVIDED && random.nextInt(100) < SpawnerRule.chance && !ghastTower) {
                            BlockAndMeta spawnerBlock = SpawnerRule.getNonAirBlock(world.rand);
							if (!spawnerBlock.equals(GHAST_SPAWNER))
								buffer[bWidth / 2 + 1][z1 + 1 + 1][sideWindowY + 1] = spawnerBlock;
						}
						//chests
						//System.out.println("checking for chest");
						if (ChestRule != TemplateRule.RULE_NOT_PROVIDED && random.nextInt(100) < ChestRule.chance)
							buffer[bWidth - 2 + 1][z1 + 1 + 1][sideWindowY - 1 + 1] = ChestRule.getNonAirBlock(world.rand);
						else if (floorHasUndeadSpawner && random.nextInt(100) < HAUNTED_CHEST_CHANCE) //even if no chest rule, can have chests if floorIsHaunted
							buffer[bWidth - 2 + 1][z1 + 1 + 1][sideWindowY - 1 + 1] = z1 < 15 ? TOWER_CHEST_BLOCK : HARD_CHEST_BLOCK;
						if (z1 == baseHeight - 1)
							z1++; //ground floor is one block higher
		}
		//*** ladder ***
		int topFloorHeight = ((bHeight - baseHeight - 4) / 4) * 4 + baseHeight + 1;
        int ladderHeight =
                roofStyle == RoofStyle.CRENEL ? bHeight : (bHeight - baseHeight < 8 ? 0
                        : topFloorHeight); // don't continue through top floor unless crenellated
		for (int z1 = baseHeight; z1 < ladderHeight; z1++)
			buffer[1 + 1][z1 + 1][sideWindowY - 1 + 1] = EAST_FACE_LADDER_BLOCK;
		//*** roof ***
		buildRoof();
        if (undeadTower && roofStyle == RoofStyle.CRENEL)
			buffer[1 + 1][bHeight > baseHeight + 12 ? baseHeight + 9 : bHeight + 1][sideWindowY - 1 + 1] = bRule.getBlockOrHole(world.rand);
		//*** run decay ***
		int zLim = bRule.chance >= 100 ? buffer[0].length : propagateCollapse(bRule.chance);
		//*** build from buffer ***
		//build tower
		for (int x1 = 1; x1 < buffer.length - 1; x1++) {
			for (int y1 = 1; y1 < buffer[0][0].length - 1; y1++) {
				if (!circular || circle_shape[x1 - 1][y1 - 1] >= 0) {
					for (int z1 = 0; z1 < Math.min(bHeight, zLim); z1++) {
						setBlockLocal(x1 - 1, z1 - 1, y1 - 1, buffer[x1][z1][y1]);
					}
				}
			}
		}
		//build roof
		for (int x1 = 0; x1 < buffer.length; x1++) {
			for (int y1 = 0; y1 < buffer[0][0].length; y1++) {
				for (int z1 = bHeight; z1 < zLim; z1++) {
					setBlockLocal(x1 - 1, z1 - 1, y1 - 1, buffer[x1][z1][y1]);
				}
			}
		}
		//*** prettify any stairs outside entrance/exit ***
		for (int x1 = 1; x1 < bWidth - 1; x1++) {
			if (isStairBlock(x1, baseHeight, -1) && getBlockIdLocal(x1, baseHeight, -2) == bRule.primaryBlock.get())
				setBlockLocal(x1, baseHeight, -1, bRule.primaryBlock);
			if (isStairBlock(x1, baseHeight, bLength) && getBlockIdLocal(x1, baseHeight, bLength + 1) == bRule.primaryBlock.get())
				setBlockLocal(x1, baseHeight, bLength, bRule.primaryBlock);
		}
		//furniture
		if (PopulateFurniture) {
			for (int z1 = baseHeight; z1 < bHeight - 2; z1 += 4) {
				for (int m = 0; m < bLength * bWidth / 25; m++) { //scale to floor area
					if (!undeadTower && random.nextInt(BED_ODDS) == 0)
						populateBeds(z1);
					if (bHeight - baseHeight > 8 && random.nextInt(BOOKSHELF_ODDS) == 0)
						populateBookshelves(z1);
					if (random.nextInt(CAULDRON_ODDS) == 0)
						populateFurnitureColumn(z1, new BlockAndMeta[] { new BlockAndMeta(Blocks.cauldron, random.nextInt(4)) });
					if (z1 > 12 && random.nextInt(BREWING_STAND_ODDS) == 0)
						populateFurnitureColumn(z1, new BlockAndMeta[] { bRule.primaryBlock, new BlockAndMeta(Blocks.brewing_stand, random.nextInt(2) + 1)});
					if (z1 > 20 && random.nextInt(ENCHANTMENT_TABLE_ODDS) == 0)
						populateFurnitureColumn(z1, new BlockAndMeta[] { new BlockAndMeta(Blocks.enchanting_table, 0)});
				}
				if (z1 == baseHeight)
					z1++;
			}
		}
		if (ghastTower)
			populateGhastSpawner(bHeight + 1);
        else if (roofStyle == RoofStyle.CRENEL && bHeight > 22)
			populatePortal(bHeight + 1);
		//*** debug signs ***
		if (BuildingWall.DEBUG_SIGNS) {
			String[] lines = new String[] { IDString().split(" ")[0], IDString().split(" ")[1], localCoordString(bWidth / 2, baseHeight, bLength / 2) };
			setSignOrPost(bWidth / 2, baseHeight, bLength / 2, true, 12, lines);
		}
        flushDelayed();
	}

	//****************************************  FUNCTION  - buildXYRotated *************************************************************************************//
	public void buildXYRotated(int p, int q, int r, BlockAndMeta block, boolean rotated) {
		if (rotated)
			buffer[r][q][p] = block;
		else
			buffer[p][q][r] = block;
	}

	public boolean isObstructedRoof(int ybuffer) {
        int rBuffer =
                (roofStyle == RoofStyle.CRENEL ? 1
                        : (roofStyle == RoofStyle.DOME || roofStyle == RoofStyle.CONE) ? 0 : -1);
        int rHeight = (roofStyle == RoofStyle.CRENEL ? 2 : minHorizDim / 2);
		if (isObstructedSolid(new int[] { rBuffer, bHeight, Math.max(rBuffer, ybuffer) }, new int[] { bWidth - 1 - rBuffer, bHeight + rHeight, bLength - 1 - rBuffer })) {
            logger.warn("Cannot build Tower " + IDString() + ". Obstructed!");
			return true;
		}
		return false;
	}

	//****************************************  FUNCTION  - propagateCollapse  *************************************************************************************//
	public int propagateCollapse(int buildChance) {
		//int buildChance= buildingRule==null ? 100 : buildingRule.chance;
		int xLim = buffer.length, zLim = buffer[0].length, yLim = buffer[0][0].length;
		//now do a support calculation
		int[][][] support = new int[xLim][zLim][yLim];
		for (int x = 0; x < xLim; x++)
			for (int z = 1; z < zLim; z++)
				for (int y = 0; y < yLim; y++)
					support[x][z][y] = 0;
		for (int x = 0; x < xLim; x++)
			for (int y = 0; y < yLim; y++)
				if (buffer[x][0][y].get() != Blocks.air)
					support[x][0][y] = 2;
		for (int z = 1; z < zLim; z++) {
			boolean levelCollapsed = true;
			for (int x = 0; x < xLim; x++) {
				for (int y = 0; y < yLim; y++) {
					if (buffer[x][z][y].get() != Blocks.air && BlockProperties.get(buffer[x][z - 1][y].get()).isLoaded && support[x][z - 1][y] > 0) {
						support[x][z][y] = 2;
						levelCollapsed = false;
					}
				}
			}
			if (levelCollapsed)
				return z;
			for (int m = 0; m < 4; m++) {
				for (int x = 0; x < xLim; x++) {
					for (int y = 0; y < yLim; y++) {
						if (buffer[x][z][y].get() != Blocks.air && support[x][z][y] == 0) {
							int neighbors = 0;
							if (x < xLim - 1 && BlockProperties.get(buffer[x + 1][z][y].get()).isLoaded)
								neighbors += support[x + 1][z][y];
							if (x > 0 && BlockProperties.get(buffer[x - 1][z][y].get()).isLoaded)
								neighbors += support[x - 1][z][y];
							if (y < yLim - 1 && BlockProperties.get(buffer[x][z][y + 1].get()).isLoaded)
								neighbors += support[x][z][y + 1];
							if (y > 0 && BlockProperties.get(buffer[x][z][y - 1].get()).isLoaded)
								neighbors += support[x][z][y - 1];
							if (neighbors > random.nextInt(4))
								support[x][z][y] = 1;
						}
					}
				}
			}
			//remove blocks if no support
			for (int x = 0; x < xLim; x++) {
				for (int y = 0; y < yLim; y++) {
					if (support[x][z][y] == 0 && !buffer[x][z][y].equals(Building.PRESERVE_BLOCK))
						buffer[x][z][y] = HOLE_BLOCK_LIGHTING;
					//else buffer[x][z][y]=new int[]{40+support[x][z][y],0};
				}
			}
		}
		return zLim;
	}

	//****************************************  FUNCTION - queryCanBuild *************************************************************************************//
	public boolean queryCanBuild(int ybuffer, boolean overlapTowers) {
        int rooftopJ =
                j0 + bHeight + (roofStyle == RoofStyle.CONE ? minHorizDim : minHorizDim / 2) + 2;
		if (rooftopJ > WORLD_MAX_Y)
			bHeight -= rooftopJ - WORLD_MAX_Y;
		if (bHeight < baseHeight + 4) {
			return false;
		}
		//check if obstructed at roof
		if (isObstructedRoof(ybuffer)) {
			//if(BuildingWall.DEBUG) FMLLog.getLogger().info("Tower blocked in roof.");
			return false;
		}
		//check if obstructed on body
        if (layoutGenerator != null) {
			int[] pt1 = getIJKPt(overlapTowers ? bWidth / 4 : 0, 0, overlapTowers ? bLength / 4 : ybuffer), pt2 = getIJKPt(overlapTowers ? 3 * bWidth / 4 - 1 : bWidth - 1, 0,
					overlapTowers ? 3 * bLength / 4 - 1 : bLength - 1);
            if (layoutGenerator.layoutIsClear(pt1, pt2, LayoutCode.TOWER))
                layoutGenerator.setLayoutCode(pt1, pt2, LayoutCode.TOWER);
            else
				return false;
		} else if (!overlapTowers) {
			if (isObstructedFrame(3, ybuffer)) {
				//if(BuildingWall.DEBUG) FMLLog.getLogger().info("Tower blocked in frame.");
				return false;
			}
		}
		//if(BuildingWall.DEBUG) FMLLog.getLogger().info("canbuildtower");
		return true;
	}

	//****************************************  FUNCTION  - buildRoof  *************************************************************************************//
	private void buildRoof() {
		//If roofRule=DEFAULT_ROOF_RULE, do wooden for sloped roofstyles and TRule otherwise
		//If roofRule=cobblestone, do cobblestone for all roofstyles
		//If roofRule=sandstone/step, do wooden for steep roofstyle and sandstone/step otherwise
		//Otherwise do wooden for sloped roofstyles, and roofRule otherwise
		if (roofRule == TemplateRule.RULE_NOT_PROVIDED) {
            roofRule =
                    (roofStyle == RoofStyle.STEEP || roofStyle == RoofStyle.SHALLOW
                            || roofStyle == RoofStyle.TRIM || roofStyle == RoofStyle.TWO_SIDED) ? new TemplateRule(
                            Blocks.planks, 0, "") : bRule;
		}
		int stepMeta = roofRule.primaryBlock.toStep().getMeta();
		TemplateRule stepRule = new TemplateRule(Blocks.stone_slab, stepMeta, roofRule.chance);
		TemplateRule doubleStepRule = (stepMeta == 2) ? new TemplateRule(Blocks.planks, 0, roofRule.chance) : new TemplateRule(Blocks.double_stone_slab, stepMeta, roofRule.chance);
        TemplateRule trimRule =
                roofStyle == RoofStyle.TRIM ? new TemplateRule(
                        bRule.primaryBlock.get() == Blocks.cobblestone ? Blocks.log
                                : Blocks.cobblestone, 0, roofRule.chance) : stepRule;
        Block roof = roofRule.primaryBlock.toStair();
        TemplateRule northStairsRule = new TemplateRule(roof, STAIRS_DIR_TO_META.get(Dir.NORTH), roofRule.chance);
		TemplateRule southStairsRule = new TemplateRule(roof, STAIRS_DIR_TO_META.get(Dir.SOUTH), roofRule.chance);
		TemplateRule eastStairsRule = new TemplateRule(roof, STAIRS_DIR_TO_META.get(Dir.EAST), roofRule.chance);
		TemplateRule westStairsRule = new TemplateRule(roof, STAIRS_DIR_TO_META.get(Dir.WEST), roofRule.chance);
		//======================================== build it! ================================================
        if (roofStyle == RoofStyle.CRENEL) { //crenelated
			if (circular) {
				for (int y1 = 0; y1 < bLength; y1++) {
					for (int x1 = 0; x1 < bWidth; x1++) {
						if (circle_shape[x1][y1] >= 0)
							buffer[x1 + 1][bHeight + 1][y1 + 1] = bRule.getBlockOrHole(world.rand);
                        if (Shape.CIRCLE_CRENEL[minHorizDim][x1][y1] == 1)
							buffer[x1 + 1][bHeight + 1 + 1][y1 + 1] = bRule.getBlockOrHole(world.rand);
					}
				}
			} else { //square
				for (int y1 = 0; y1 < bLength; y1++)
					for (int x1 = 0; x1 < bWidth; x1++)
						buffer[x1 + 1][bHeight + 1][y1 + 1] = bRule.getBlockOrHole(world.rand);
				for (int m = 0; m < bWidth; m += 2) {
					if (!(getBlockIdLocal(m, bHeight, -1) == bRule.primaryBlock.get() || getBlockIdLocal(m, bHeight - 1, -1) == bRule.primaryBlock.get()))
						buffer[m + 1][bHeight + 1 + 1][0 + 1] = (m + 1) % 2 == 0 ? HOLE_BLOCK_LIGHTING : bRule.getBlockOrHole(world.rand);
					if (!(getBlockIdLocal(m, bHeight, bLength) == bRule.primaryBlock.get() || getBlockIdLocal(m, bHeight - 1, bLength) == bRule.primaryBlock.get()))
						buffer[m + 1][bHeight + 1 + 1][bLength - 1 + 1] = (m + 1) % 2 == 0 ? HOLE_BLOCK_LIGHTING : bRule.getBlockOrHole(world.rand);
				}
				for (int m = 0; m < bLength; m += 2) {
					if (!(getBlockIdLocal(-1, bHeight, m) == bRule.primaryBlock.get() || getBlockIdLocal(-1, bHeight - 1, m) == bRule.primaryBlock.get()))
						buffer[0 + 1][bHeight + 1 + 1][m + 1] = (m + 1) % 2 == 0 ? HOLE_BLOCK_LIGHTING : bRule.getBlockOrHole(world.rand);
					if (!(getBlockIdLocal(bWidth, bHeight, m) == bRule.primaryBlock.get() || getBlockIdLocal(bWidth, bHeight - 1, m) == bRule.primaryBlock.get()))
						buffer[bWidth - 1 + 1][bHeight + 1 + 1][m + 1] = (m + 1) % 2 == 0 ? HOLE_BLOCK_LIGHTING : bRule.getBlockOrHole(world.rand);
				}
				for (int y1 = 1; y1 < bLength - 1; y1++)
					for (int x1 = 1; x1 < bWidth - 1; x1++)
						if (isWallBlock(x1, bHeight, y1)
								&& !(isWallBlock(x1 + 1, bHeight, y1) || isWallBlock(x1 - 1, bHeight, y1) || isWallBlock(x1, bHeight, y1 + 1) || isWallBlock(x1, bHeight, y1 - 1)))
							buffer[x1 + 1][bHeight + 1 - 1 + 1][y1 + 1] = HOLE_BLOCK_LIGHTING;
			}
			buffer[2][bHeight + 1][bLength / 2] = EAST_FACE_LADDER_BLOCK;
			buffer[2][bHeight + 2][bLength / 2] = new BlockAndMeta(Blocks.trapdoor, 3);
        } else if (roofStyle == RoofStyle.STEEP || roofStyle == RoofStyle.TRIM
                || (roofStyle == RoofStyle.SHALLOW && (bWidth < 6 || bLength < 6))) { // 45 degrees
                                                                                      // sloped
			for (int m = 0; m < (minHorizDim + 1) / 2; m++) {
				for (int x1 = m; x1 < bWidth - m; x1++) {
					for (int y1 = m; y1 < bLength - m; y1++) {
						buffer[x1 + 1][bHeight + m + 1][y1 + 1] = HOLE_BLOCK_LIGHTING;
						if (m == (bWidth + 1) / 2 - 1)
							buffer[x1 + 1][bHeight + m + 1 + 1][y1 + 1] = trimRule.getBlockOrHole(world.rand);
					}
					buffer[x1 + 1][bHeight + m + 1][m - 1 + 1] = northStairsRule.getBlockOrHole(world.rand);
					buffer[x1 + 1][bHeight + m + 1][bLength - m + 1] = southStairsRule.getBlockOrHole(world.rand);
					buffer[x1 + 1][bHeight + m + 1][m + 1] = doubleStepRule.getBlockOrHole(world.rand);
					buffer[x1 + 1][bHeight + m + 1][bLength - m - 1 + 1] = doubleStepRule.getBlockOrHole(world.rand);
				}
				for (int y1 = m; y1 < bLength - m; y1++) {
					buffer[m - 1 + 1][bHeight + m + 1][y1 + 1] = eastStairsRule.getBlockOrHole(world.rand);
					buffer[bWidth - m + 1][bHeight + m + 1][y1 + 1] = westStairsRule.getBlockOrHole(world.rand);
					buffer[m + 1][bHeight + m + 1][y1 + 1] = doubleStepRule.getBlockOrHole(world.rand);
					buffer[bWidth - m - 1 + 1][bHeight + m + 1][y1 + 1] = doubleStepRule.getBlockOrHole(world.rand);
				}
				buffer[m - 1 + 1][bHeight + m + 1][m - 1 + 1] = trimRule.getBlockOrHole(world.rand);
				buffer[m - 1 + 1][bHeight + m + 1][bLength - m + 1] = trimRule.getBlockOrHole(world.rand);
				buffer[bWidth - m + 1][bHeight + m + 1][m - 1 + 1] = trimRule.getBlockOrHole(world.rand);
				buffer[bWidth - m + 1][bHeight + m + 1][bLength - m + 1] = trimRule.getBlockOrHole(world.rand);
			}
        } else if (roofStyle == RoofStyle.SHALLOW) { //22 degrees sloped
			for (int z12 = -1; z12 < (minHorizDim + 1) / 2; z12++) {
				int z1 = (z12 + 1) / 2;
				if ((z12 + 1) % 2 == 0) {
					for (int y1 = z12 + 1; y1 < bLength - 2 * z1; y1++) {
						buffer[z12 + 1][z1 + bHeight + 1][y1 + 1] = stepRule.getBlockOrHole(world.rand);
						buffer[bWidth - z12 - 1 + 1][z1 + bHeight + 1][y1 + 1] = stepRule.getBlockOrHole(world.rand);
					}
					for (int x1 = z12 + 1; x1 < bWidth - 2 * z1; x1++) {
						buffer[x1 + 1][z1 + bHeight + 1][z12 + 1] = stepRule.getBlockOrHole(world.rand);
						buffer[x1 + 1][z1 + bHeight + 1][bLength - z12 - 1 + 1] = stepRule.getBlockOrHole(world.rand);
					}
					buffer[z12 + 1][z1 + bHeight + 1][z12 + 1] = doubleStepRule.getBlockOrHole(world.rand);
					buffer[z12 + 1][z1 + bHeight + 1][bLength - z12 - 1 + 1] = doubleStepRule.getBlockOrHole(world.rand);
					buffer[bWidth - z12 - 1 + 1][z1 + bHeight + 1][z12 + 1] = doubleStepRule.getBlockOrHole(world.rand);
					buffer[bWidth - z12 - 1 + 1][z1 + bHeight + 1][bLength - z12 - 1 + 1] = doubleStepRule.getBlockOrHole(world.rand);
				} else {
					for (int y1 = z12; y1 < bLength - z12; y1++)
						for (int x1 = z12; x1 < bWidth - z12; x1++)
							buffer[x1 + 1][z1 + bHeight + 1][y1 + 1] = doubleStepRule.getBlockOrHole(world.rand);
					for (int y1 = z12 + 2; y1 < bLength - z12 - 2; y1++)
						for (int x1 = z12 + 2; x1 < bWidth - z12 - 2; x1++)
							buffer[x1 + 1][z1 + bHeight + 1][y1 + 1] = HOLE_BLOCK_LIGHTING;
					buffer[z12 + 1][z1 + bHeight + 1 + 1][z12 + 1] = stepRule.getBlockOrHole(world.rand);
					buffer[z12 + 1][z1 + bHeight + 1 + 1][bLength - z12 - 1 + 1] = stepRule.getBlockOrHole(world.rand);
					buffer[bWidth - z12 - 1 + 1][z1 + bHeight + 1 + 1][z12 + 1] = stepRule.getBlockOrHole(world.rand);
					buffer[bWidth - z12 - 1 + 1][z1 + bHeight + 1 + 1][bLength - z12 - 1 + 1] = stepRule.getBlockOrHole(world.rand);
				}
			}
        } else if (roofStyle == RoofStyle.DOME) { //dome
			for (int z1 = 0; z1 < (minHorizDim + 1) / 2; z1++) {
                int diam = Shape.SPHERE_SHAPE[minHorizDim][z1];
				for (int y1 = 0; y1 < diam; y1++) {
					for (int x1 = 0; x1 < diam; x1++) {
                        if (Shape.CIRCLE_SHAPE[diam][x1][y1] >= 0)
                            buffer[x1 + (bWidth - diam) / 2 + 1][bHeight + z1 + 1 + 1][y1 + (bLength - diam) / 2 + 1] = (Shape.CIRCLE_SHAPE[diam][x1][y1] == 1 || z1 >= (minHorizDim + 1) / 2 - 2) ? roofRule
									.getBlockOrHole(world.rand) : HOLE_BLOCK_LIGHTING;
									if (z1 < (minHorizDim - 1) / 2) {
                                        int nextDiam = Shape.SPHERE_SHAPE[minHorizDim][z1 + 1];
										int x2 = x1 - (diam - nextDiam) / 2, y2 = y1 - (diam - nextDiam) / 2;
                                        if (Shape.CIRCLE_SHAPE[diam][x1][y1] == 0 && (x2 < 0 || y2 < 0 || x2 >= nextDiam || y2 >= nextDiam || Shape.CIRCLE_SHAPE[nextDiam][x2][y2] != 0))
											buffer[x1 + (bWidth - diam) / 2 + 1][bHeight + z1 + 1 + 1][y1 + (bLength - diam) / 2 + 1] = roofRule.getBlockOrHole(world.rand);
									}
					}
				}
			}
        } else if (roofStyle == RoofStyle.CONE) { //cone
			int prevDiam = 0;
			for (int z1 = 0; z1 < minHorizDim + 1; z1++) {
				int diam = minHorizDim % 2 == 0 ? 2 * ((minHorizDim - z1 + 1) / 2) : 2 * ((minHorizDim - z1) / 2) + 1;
				for (int y1 = 0; y1 < diam; y1++) {
					for (int x1 = 0; x1 < diam; x1++) {
                        if (Shape.CIRCLE_SHAPE[diam][x1][y1] >= 0)
                            buffer[x1 + (bWidth - diam) / 2 + 1][bHeight + z1 + 1 + 1][y1
                                    + (bLength - diam) / 2 + 1] =
                                    Shape.CIRCLE_SHAPE[diam][x1][y1] == 1 ? roofRule
                                            .getBlockOrHole(world.rand) : HOLE_BLOCK_LIGHTING;
                        if (z1 > 0
                                && Shape.CIRCLE_SHAPE[diam][x1][y1] != 0
                                && Shape.CIRCLE_SHAPE[prevDiam][x1 + (prevDiam - diam) / 2][y1
                                        + (prevDiam - diam) / 2] == 0)
								buffer[x1 + (bWidth - diam) / 2 + 1][bHeight + z1 + 1][y1 + (bLength - diam) / 2 + 1] = roofRule.getBlockOrHole(world.rand);
					}
				}
				prevDiam = diam;
			}
        } else if (roofStyle == RoofStyle.TWO_SIDED) { //Two Sided
			//roof peak will follow the major horizontal axis
			//if X-axis is the major axis, rot will be false, minAxLen==bLength, maxAxLen==bWidth, and p==x1, r==y1.
			boolean rot = bLength > minHorizDim;
			int minAxLen = rot ? bWidth : bLength, maxAxLen = rot ? bLength : bWidth;
			BlockAndMeta forwardsStairsRule = rot ? eastStairsRule.getBlockOrHole(world.rand) : northStairsRule.getBlockOrHole(world.rand), backwardsStirRule = rot ? westStairsRule
					.getBlockOrHole(world.rand) : southStairsRule.getBlockOrHole(world.rand);
					for (int m = 0; m <= minHorizDim / 2; m++) {
						for (int p = 0; p < maxAxLen; p++) {
							for (int r = m + 1; r < minAxLen - m - 1; r++)
								buildXYRotated(p + 1, bHeight + m + 1, r + 1, HOLE_BLOCK_LIGHTING, rot);
							buildXYRotated(p + 1, bHeight + m + 1, m - 1 + 1, forwardsStairsRule, rot);
							buildXYRotated(p + 1, bHeight + m + 1, minAxLen - m + 1, backwardsStirRule, rot);
							if (!(minHorizDim % 2 == 0 && m == minHorizDim / 2)) {
								buildXYRotated(p + 1, bHeight + m + 1, m + 1, doubleStepRule.getBlockOrHole(world.rand), rot);
								buildXYRotated(p + 1, bHeight + m + 1, minAxLen - m - 1 + 1, doubleStepRule.getBlockOrHole(world.rand), rot);
							}
						}
						for (int r = m; r < minAxLen - m; r++) {
							if (!(minHorizDim % 2 == 1 && m == minHorizDim / 2)) {
								buildXYRotated(1, bHeight + m + 1, r + 1, (circular && circle_shape[0][r] < 0) ? doubleStepRule.getBlockOrHole(world.rand) : bRule.getBlockOrHole(world.rand), rot);
								buildXYRotated(maxAxLen - 1 + 1, bHeight + m + 1, r + 1,
										(circular && circle_shape[bLength - 1][r] < 0) ? doubleStepRule.getBlockOrHole(world.rand) : bRule.getBlockOrHole(world.rand), rot);
							}
						}
					}
		}
		//close up corner overhangs
        if (circular
                && (roofStyle == RoofStyle.STEEP || roofStyle == RoofStyle.TRIM
                        || roofStyle == RoofStyle.SHALLOW || roofStyle == RoofStyle.TWO_SIDED)) {
			for (int x1 = 0; x1 < minHorizDim; x1++) {
				for (int y1 = 0; y1 < minHorizDim; y1++) {
					if (circle_shape[x1][y1] < 0)
						buffer[x1 + 1][bHeight + 1][y1 + 1] = doubleStepRule.getBlockOrHole(world.rand);
					if (circle_shape[x1][y1] == 1) {
						for (int z1 = bHeight; z1 < bHeight + minHorizDim; z1++) {
							if (buffer[x1 + 1][z1 + 1][y1 + 1] == HOLE_BLOCK_LIGHTING)
								buffer[x1 + 1][z1 + 1][y1 + 1] = bRule.getBlockOrHole(world.rand);
							else
								break;
						}
					}
				}
			}
		}
        if (roofStyle == RoofStyle.DOME || roofStyle == RoofStyle.CONE) {
			int xBuff = (bWidth - minHorizDim) / 2, yBuff = (bLength - minHorizDim) / 2;
			for (int x1 = 0; x1 < minHorizDim; x1++) {
				for (int y1 = 0; y1 < minHorizDim; y1++) {
					if (circle_shape[x1][y1] >= 0)
						buffer[x1 + xBuff + 1][bHeight + 1][y1 + yBuff + 1] = circle_shape[x1][y1] == 0 ? HOLE_BLOCK_LIGHTING : bRule.getBlockOrHole(world.rand);
					else if (!circular)
						buffer[x1 + xBuff + 1][bHeight + 1][y1 + yBuff + 1] = bRule.getBlockOrHole(world.rand);
				}
			}
			if (!circular) {
				for (int y1 = 0; y1 < minHorizDim; y1++) {
					for (int x1 = 0; x1 < xBuff; x1++)
						buffer[x1 + 1][bHeight + 1][y1 + 1] = bRule.getBlockOrHole(world.rand);
					for (int x1 = xBuff + minHorizDim; x1 < bWidth; x1++)
						buffer[x1 + 1][bHeight + 1][y1 + 1] = bRule.getBlockOrHole(world.rand);
				}
				for (int x1 = 0; x1 < minHorizDim; x1++) {
					for (int y1 = 0; y1 < yBuff; y1++)
						buffer[x1 + 1][bHeight + 1][y1 + 1] = bRule.getBlockOrHole(world.rand);
					for (int y1 = yBuff + minHorizDim; y1 < bLength; y1++)
						buffer[x1 + 1][bHeight + 1][y1 + 1] = bRule.getBlockOrHole(world.rand);
				}
			}
		}
	}

	private void populateBookshelves(int z) {
		int x1 = random.nextInt(bWidth - 2) + 1;
		int y1 = random.nextInt(bLength - 2) + 1;
		Dir dir = Dir.randomDir(random);
		int xinc = dir.x;
		int yinc = dir.y;
		//find a wall
		while (true) {
			if (x1 < 1 || x1 >= bWidth - 1 || y1 < 1 || y1 >= bLength - 1 || !isFloor(x1, z, y1))
				return;
			if (BlockProperties.get(getBlockIdLocal(x1 + xinc, z, y1 + yinc)).isArtificial && BlockProperties.get(getBlockIdLocal(x1 + xinc, z - 1, y1 + yinc)).isArtificial
					&& getBlockIdLocal(x1 + xinc, z - 1, y1 + yinc) != Blocks.ladder) {
				break;
			}
			x1 += xinc;
			y1 += yinc;
		}
		for (int m = 0; m < 2; m++) {
			for (int z1 = z; z1 < z + 1 + random.nextInt(3); z1++) {
				if (getBlockIdLocal(x1, z1, y1) != Blocks.air || !isWallBlock(x1 + xinc, z1, y1 + yinc))
					break;
				setBlockLocal(x1, z1, y1, Blocks.bookshelf);
			}
			x1 += dir.rotate(1).x;
			y1 += dir.rotate(1).y;
			if (!isFloor(x1, z, y1))
				break;
		}
	}

	private boolean populatePortal(int z) {
		if (world.provider.isHellWorld) {
			if (random.nextInt(NETHER_PORTAL_ODDS) != 0)
				return false;
		} else if (random.nextInt(SURFACE_PORTAL_ODDS) != 0)
			return false;
		boolean hasSupport = false;
		for (int y1 = bLength / 2 - 2; y1 < bLength / 2 + 2; y1++) {
			if (getBlockIdLocal(bWidth / 2, z, y1) != Blocks.air)
				return false;
			if (getBlockIdLocal(bWidth / 2, z - 1, y1) != Blocks.air)
				hasSupport = true;
		}
		if (!hasSupport)
			return false;
		for (int y1 = bLength / 2 - 2; y1 < bLength / 2 + 2; y1++) {
			setBlockLocal(bWidth / 2, z, y1, Blocks.obsidian);
			setBlockLocal(bWidth / 2, z + 4, y1, Blocks.obsidian);
		}
		for (int z1 = z + 1; z1 < z + 4; z1++) {
			setBlockLocal(bWidth / 2, z1, bLength / 2 - 2, Blocks.obsidian);
			setBlockLocal(bWidth / 2, z1, bLength / 2 - 1, Blocks.portal);
			setBlockLocal(bWidth / 2, z1, bLength / 2, Blocks.portal);
			setBlockLocal(bWidth / 2, z1, bLength / 2 + 1, Blocks.obsidian);
		}
		return true;
	}

    public int baseHeight, minHorizDim;
    public RoofStyle roofStyle;
	public final boolean PopulateFurniture, MakeDoors, circular;
	private BlockAndMeta[][][] buffer;
	private int[][] circle_shape;
	private TemplateRule roofRule, SpawnerRule, ChestRule;

    public BuildingTower(int ID_, Building parent, boolean circular_, RoofStyle roofStyle_,
            Dir dir_, Handedness axXHand_, boolean centerAligned_, int TWidth_, int THeight_,
            int TLength_, int[] sourcePt) {
        super(ID_, parent.config, parent.bRule, dir_, axXHand_, centerAligned_, new int[] {TWidth_,
                THeight_, TLength_}, sourcePt);
		baseHeight = 0;
		roofStyle = roofStyle_;
		minHorizDim = Math.min(bWidth, bLength);
        circle_shape = Shape.CIRCLE_SHAPE[minHorizDim];
		circular = circular_;
		ChestRule = TemplateRule.RULE_NOT_PROVIDED;
		roofRule = bRule;
		SpawnerRule = TemplateRule.RULE_NOT_PROVIDED;
		PopulateFurniture = false;
		MakeDoors = false;
		if (circular)
			bLength = bWidth = minHorizDim; //enforce equal horizontal dimensions if circular
	}

    /**
     * <pre>
     *      ----
     *     -    -
     *    -      -
     *   -        -
     *  -          -
     * -------------- bHeight
     * -            -
     * -            -
     * -            -
     * --------------
     * -            -
     * -            -
     * -            -
     * --------------
     * -            -
     * -            -
     * -            -
     * -            -  baseHeight==ws.WalkHeight
     * --------------  baseHeight-1 (floor)
     * </pre>
     */
    public BuildingTower(int ID_, BuildingWall wall, Dir dir_, Handedness axXHand_,
            boolean centerAligned_, int TWidth_, int THeight_, int TLength_, int[] sourcePt) {
        super(ID_, wall.config, wall.towerRule, dir_, axXHand_, centerAligned_, new int[] {TWidth_,
                THeight_, TLength_}, sourcePt);
		baseHeight = wall.WalkHeight;
		roofStyle = wall.roofStyle;
		minHorizDim = Math.min(bWidth, bLength);
        circle_shape = Shape.CIRCLE_SHAPE[minHorizDim];
		circular = wall.circular;
		ChestRule = wall.ws.ChestRule;
		roofRule = wall.roofRule;
		SpawnerRule = wall.ws.SpawnerRule;
		PopulateFurniture = wall.ws.PopulateFurniture;
		MakeDoors = wall.ws.MakeDoors;
		if (circular)
			bLength = bWidth = minHorizDim; //enforce equal horizontal dimensions if circular
	}

	//****************************************  FUNCTION  - buildWindowOrDoor *************************************************************************************//
	//builds either a window or a door depending on whether there is a floor outside of the aperture.
	private void buildWindowOrDoor(int x, int z, int y, int xFace, int yFace, int height) {
		boolean buildWoodDoor = false;
		if (isFloor(x + xFace, z - 1, y + yFace) || isFloor(x + xFace, z - 2, y + yFace)) {
			z--;
			if (MakeDoors)
				buildWoodDoor = true;
		}
		if (!BlockProperties.get(getBlockIdLocal(x, z + height - 2, y + yFace)).isWallable)
			return;
		if (buildWoodDoor) {
            int metadata =
                    xFace == 0 ? (yFace > 0 ? DOOR_DIR_TO_META.get(Dir.SOUTH) : DOOR_DIR_TO_META
                            .get(Dir.NORTH)) : (xFace > 0 ? DOOR_DIR_TO_META.get(Dir.WEST)
                            : DOOR_DIR_TO_META.get(Dir.EAST));
			buffer[x + 1][z + 1][y + 1] = new BlockAndMeta(Blocks.wooden_door, metadata);
			buffer[x + 1][z + 1 + 1][y + 1] = new BlockAndMeta(Blocks.wooden_door, random.nextBoolean() ? 8 : 9);
			if (isFloor(x + xFace, z - 1, y + yFace) && x + xFace + 1 >= 0 && x + xFace + 1 < buffer.length && y + yFace + 1 >= 0 && y + yFace + 1 < buffer[0][0].length) {
				buffer[x + xFace + 1][z - 1 + 1][y + yFace + 1] = bRule.primaryBlock.toStep();//build a step-up
			}
		} else
			for (int z1 = z; z1 < z + height; z1++)
				buffer[x + 1][z1 + 1][y + 1] = HOLE_BLOCK_LIGHTING; //carve out the aperture
		//clear a step in front of window if there is a floor at z+1
		if (isFloor(x + xFace, z + 2, y + yFace) && isWallBlock(x + xFace, z, y + yFace) && x + xFace + 1 >= 0 && x + xFace + 1 < buffer.length && y + yFace + 1 >= 0
				&& y + yFace + 1 < buffer[0][0].length)
			buffer[x + xFace + 1][z + 1 + 1][y + yFace + 1] = HOLE_BLOCK_LIGHTING;
	}

	//****************************************  FUNCTIONS  - populators *************************************************************************************//

    /**
     * Creates a bed at the specified local z level. Beds are made of 2 blocks:
     * the head of the bed and the foot of the bed. Bed metadata meanings (from
     * <a href="http://minecraft.gamepedia.com/Data_values#Beds">Minecraft
     * Wiki</a>):
     *
     * <table>
     * <tr><th>Value</th><th>Meaning</th></tr>
     * <tr><td>0x0</td><td>Head is pointing South</td></tr>
     * <tr><td>0x1</td><td>Head is pointing West</td></tr>
     * <tr><td>0x2</td><td>Head is pointing North</td></tr>
     * <tr><td>0x3</td><td>Head is pointing East</td></tr>
     * <tr><td>+0x4 (bit)</td><td>Bed is occupied</td></tr>
     * <tr><td>+0x8 (bit)</td><td>Head of the bed</td></tr>
     * </table>
     *
     * TODO: The logic in this method appears to create invalid beds. The
     * direction chosen here, and the "y" value of the Dir (which is inverted
     * for north-south/z-axis only) don't seem to agree. Should probably be
     * fixed, but want to try to unit test as much as possible first.
     * BED_DIR_TO_META might also be useful here.
     */
	private void populateBeds(int z) {
        Dir dir = Dir.randomDir(random);
		int x1 = random.nextInt(bWidth - 2) + 1;
		int y1 = random.nextInt(bLength - 2) + 1;
        int x2 = x1 + dir.x;
        int y2 = y1 + dir.y;
		if (isFloor(x1, z, y1) && hasNoDoorway(x1, z, y1) && isFloor(x2, z, y2) && hasNoDoorway(x2, z, y2)) {
            setBlockLocal(x1, z, y1, Blocks.bed, dir.ordinal() + 8); //head of the bed?
            setBlockLocal(x2, z, y2, Blocks.bed, dir.ordinal());
		}
	}

	private void populateFurnitureColumn(int z, BlockAndMeta[] block) {
		int x1 = random.nextInt(bWidth - 2) + 1;
		int y1 = random.nextInt(bLength - 2) + 1;
		if (isFloor(x1, z, y1) && hasNoDoorway(x1, z, y1)) {
			for (int z1 = 0; z1 < block.length; z1++)
				setBlockLocal(x1, z + z1, y1, block[z1]);
		}
	}

	private boolean populateGhastSpawner(int z) {
		for (int tries = 0; tries < 5; tries++) {
			int x1 = random.nextInt(bWidth - 2) + 1;
			int y1 = random.nextInt(bLength - 2) + 1;
			if (isFloor(x1, z, y1)) {
				setBlockLocal(x1, z, y1, GHAST_SPAWNER);
				return true;
			}
		}
		return false;
	}
}
