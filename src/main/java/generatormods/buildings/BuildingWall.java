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

import generatormods.caruins.seeds.ISeed;
import generatormods.caruins.seeds.SymmetricSeed;
import generatormods.common.BlockAndMeta;
import generatormods.common.BlockExtended;
import generatormods.common.BlockProperties;
import generatormods.common.Dir;
import generatormods.common.Handedness;
import generatormods.common.PickWeighted;
import generatormods.common.TemplateRule;
import generatormods.common.TemplateTML;
import generatormods.common.TemplateWall;
import generatormods.common.Util;
import generatormods.walledcity.LayoutCode;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.ChunkCoordinates;

import org.apache.logging.log4j.Logger;

/*
 * BuildingWall plans and builds a wall that flows along Minecraft's terrain.
 */
public class BuildingWall extends Building {
	public enum FailType {
		NOTHING, OBSTRUCTED, UNDERWATER, TOOSTEEPDOWN, TOOSTEEPUP, HITWALL, CANNOTEXPLORE, HITTARGET, MAXLENGTH
	}
    protected final static Block[] STEP_TO_STAIRS = { Blocks.stone_brick_stairs, Blocks.sandstone_stairs, Blocks.oak_stairs, Blocks.stone_stairs, Blocks.brick_stairs, Blocks.stone_brick_stairs, Blocks.nether_brick_stairs,
            Blocks.quartz_stairs };
	public final static boolean DEBUG = false;
	public final static boolean DEBUG_SIGNS = false;
	public final static int SEARCHDOWN = 2, MIN_SEARCHUP = 2;
	public final static int DEFAULT_LOOKAHEAD = 5, MIN_BRANCH_IMPROVEMENT = 15;
	public final static int MAX_BACKTRACK_DEPTH = 2;
	public final static int OVERHEAD_CLEARENCE = 4, OVERHEAD_TREE_CLEARENCE = 8;
	public final static int NO_GATEWAY = -1, NO_MIN_J = -1;
	private final static int MIN_GATEWAY_ROAD_LENGTH = 20;
	//**** WORKING VARIABLES ****
	public int i1, j1, k1;
	public int n0 = 0;
	public int WalkHeight; //this is absolute, same as WallStyle
	public int maxLength;
	public int[] xArray, zArray;
	public int gatewayStart = NO_GATEWAY, gatewayEnd = NO_GATEWAY;
	public TemplateWall ws;
	public boolean target = false, circular = false;
	public int x_targ, z_targ, y_targ;
	public int minJ = NO_MIN_J;
	private boolean hitMaxDepth = false;
	public FailType failCode = FailType.NOTHING;
	public TemplateTML endBTemplate = null; //either a template or DEFAULT_TOWER
	public int endBLength = 0; //length of end tower
	private BlockAndMeta halfStairValue = new BlockAndMeta(Blocks.stone_slab, 2); // half step based on bRule
	public int roofStyle;
	public TemplateRule towerRule, roofRule;
	public final int Backtrack;

	public BuildingWall(BuildingWall bw, int maxLength_, int i1_, int j1_, int k1_) {
        super(bw.bID, bw.config, bw.bRule, bw.bDir, bw.bHand, false, new int[] {bw.bWidth,
                bw.bHeight, 0}, new int[] {i1_, j1_, k1_});
		constructorHelper(bw.ws, maxLength_, i1_, j1_, k1_);
        Backtrack = bw.Backtrack;
		target = bw.target;
		x_targ = bw.x_targ;
		z_targ = bw.z_targ;
		y_targ = bw.y_targ;
	}

    public BuildingWall(int ID_, IBuildingConfig config, TemplateWall ws_, Dir dir_,
            Handedness axXHand_, int maxLength_, boolean endTowers, int i1_, int j1_, int k1_) {
        super(ID_, config, ws_.rules[ws_.template[0][0][ws_.WWidth / 2]], dir_, axXHand_, false,
                new int[] {ws_.WWidth, ws_.WHeight, 0}, new int[] {i1_, j1_, k1_});
		constructorHelper(ws_, maxLength_, i1_, j1_, k1_);
		pickTowers(random.nextFloat() < ws.CircularProb, endTowers);
        Backtrack = config.getBacktrackLength();
		if (maxLength > 0) {
			xArray[0] = 0;
			zArray[0] = 0;
		}
	}

    public BuildingWall(int ID_, IBuildingConfig config, TemplateWall ws_, Dir dir_,
            Handedness axXHand_, int maxLength_, boolean endTowers, int[] sourcePt) {
        super(ID_, config, ws_.rules[ws_.template[0][0][ws_.WWidth / 2]], dir_, axXHand_, false,
                new int[] {ws_.WWidth, ws_.WHeight, 0}, sourcePt);
		constructorHelper(ws_, maxLength_, sourcePt[0], sourcePt[1], sourcePt[2]);
		pickTowers(random.nextFloat() < ws.CircularProb, endTowers);
        Backtrack = config.getBacktrackLength();
		if (maxLength > 0) {
			xArray[0] = 0;
			zArray[0] = 0;
		}
	}

    /**
     * Builds a planned wall from a template.
     */
	public void buildFromTML() {
		if (ws == null) {
            logger.error("Tried to build wall from template but no template was given!");
			return;
		}
		setCursor(0);
        if (bLength > 0) {
            logger.debug("Built " + ws.name + " wall " + IDString() + ", length " + (bLength) + " from " + localCoordString(xArray[0], zArray[0], 0) + " to "
                    + localCoordString(xArray[bLength - 1], zArray[bLength - 1], bLength - 1));
        } else {
            logger.warn("Wall too short to build! " + IDString() + "length=" + bLength + " at " + localCoordString(0, 0, 0));
        }
        // TODO: ??? why are we checking this here?
        logger.debug("Wall planning was terminated due to: " + failString());
		int lN = 0;
		BlockAndMeta idAndMeta;
		int layer[][];
		//get named layers
		int base[] = ws.template[0][0]; //defaults to bottom line of first layer
		if (ws.namedLayers.containsKey("base"))
			base = (ws.namedLayers.get("base"))[ws.length - 1];
		int[][] shifted, shiftedLeft, shiftedRight, shiftedUp, shiftedDown;
		shifted = ws.namedLayers.containsKey("shifted") ? ws.namedLayers.get("shifted") : ws.template[0];
		shiftedLeft = ws.namedLayers.containsKey("shifted_left") ? ws.namedLayers.get("shifted_left") : shifted;
		shiftedRight = ws.namedLayers.containsKey("shifted_right") ? ws.namedLayers.get("shifted_right") : shifted;
		shiftedUp = ws.namedLayers.containsKey("shifted_up") ? ws.namedLayers.get("shifted_up") : shifted;
		shiftedDown = ws.namedLayers.containsKey("shifted_down") ? ws.namedLayers.get("shifted_down") : shifted;
		for (setCursor(0); n0 < bLength; setCursor(n0 + 1)) {
			if (n0 == 0)
				layer = shifted;
			else if (xArray[n0 - 1] < xArray[n0])
				layer = shiftedRight;
			else if (xArray[n0 - 1] > xArray[n0])
				layer = shiftedLeft;
			else if (zArray[n0 - 1] < zArray[n0])
				layer = shiftedUp;
			else if (zArray[n0 - 1] > zArray[n0])
				layer = shiftedDown;
			else if (n0 == bLength - 1 || xArray[n0 + 1] != xArray[n0] || zArray[n0 + 1] != zArray[n0])
				layer = shifted;
			else
				layer = ws.template[lN];
			if (layer == ws.template[lN])
				lN = (lN + 1) % ws.height;
			else
				lN = 0;
			//wall
			for (int x1 = 0; x1 < bWidth; x1++) {
				boolean keepWallFromAbove = true;
				for (int z1 = bHeight + OVERHEAD_CLEARENCE - 1; z1 >= -ws.embed; z1--) {
					boolean wallBlockPresent = isWallBlock(x1, z1, 0);
					idAndMeta = z1 < bHeight ? ws.rules[layer[z1 + ws.embed][x1]].getBlockOrHole(world.rand) : HOLE_BLOCK_NO_LIGHTING;
					//starting from top, preserve old wall block until we run into a non-wall block
					if (keepWallFromAbove && wallBlockPresent && idAndMeta.get() == Blocks.air) {
						continue;
					} else
						keepWallFromAbove = false;
					if (idAndMeta.get() == Blocks.air && idAndMeta instanceof BlockExtended && ((BlockExtended) idAndMeta).info.equals(TemplateRule.SPECIAL_STAIR)) {
						if (!wallBlockPresent && !BlockProperties.get(getBlockIdLocal(x1, z1, 0)).isWater) {
							if (n0 > 0 && zArray[n0 - 1] > zArray[n0]) { //stairs, going down
								if ((n0 == 1 || zArray[n0 - 2] == zArray[n0 - 1]) && (n0 == bLength - 1 || zArray[n0] == zArray[n0 + 1]))
									setSpecialBlockLocal(x1, z1, 0, idAndMeta.get(), idAndMeta.getMeta(), ((BlockExtended) idAndMeta).info);
								else
									setBlockLocal(x1, z1, 0, STEP_TO_STAIRS[-idAndMeta.getMeta() > 7 ? -idAndMeta.getMeta() - 8 : -idAndMeta.getMeta()], 2);
							} else if (n0 < bLength - 1 && zArray[n0] < zArray[n0 + 1]) { //stairs, going up
								if ((n0 == 0 || zArray[n0 - 1] == zArray[n0]) && (n0 == bLength - 2 || zArray[n0 + 1] == zArray[n0 + 2]))
									setSpecialBlockLocal(x1, z1, 0, idAndMeta.get(), idAndMeta.getMeta(), ((BlockExtended) idAndMeta).info);
								else
									setBlockLocal(x1, z1, 0, STEP_TO_STAIRS[-idAndMeta.getMeta() > 7 ? -idAndMeta.getMeta() - 8 : -idAndMeta.getMeta()], 3);
							} else
								setBlockLocal(x1, z1, 0, idAndMeta.get());
						}
					} else { //not a stair
						// if merging walls, don't clutter with crenelations etc.
						if (z1 >= WalkHeight
								&& (x1 == 0 && (wallBlockPresent || isWallBlock(-1, WalkHeight - 1, 0) || isWallBlock(-1, WalkHeight - 2, 0)) || x1 == bWidth - 1
										&& (wallBlockPresent || isFloor(bWidth, WalkHeight - 1, 0) || isWallBlock(bWidth, WalkHeight - 2, 0)))) {
							continue;
						}
						if (idAndMeta.get() == Blocks.air && idAndMeta.getMeta() == 0 && z1 < bHeight)
                            removeBlockWithLighting(x1, z1, 0); //force lighting update for holes
						else
							setBlockLocal(x1, z1, 0, idAndMeta); //straightforward build from template
					}
				}
			}
			//base
			for (int x1 = 0; x1 < bWidth; x1++)
				buildDown(x1, -1 - ws.embed, 0, ws.rules[base[x1]], ws.leveling, Math.min(2, ws.embed), 3);
			clearTrees();
			mergeWallLayer();
			//DEBUGGING, creates signs with ID/distance info
			/*
			 * if(DEBUG_SIGNS && (n0) % 10==0){ //String[] lines=new
			 * String[]{IDString
			 * ().split(" ")[0],IDString().split(" ")[1],"Dist:"+n+
			 * " / "+planL,globalCoordString(1,WalkHeight,0)}; String[]
			 * lines=new
			 * String[]{IDString().split(" ")[0],xArray[n0]+"","Dist:"+n0+
			 * " / "+bLength,localCoordString(1,WalkHeight,0)};
			 * setSignOrPost(1,WalkHeight,0,true,8,lines);
			 * setSignOrPost(-1,WalkHeight-1,0,false,3,lines);
			 * setSignOrPost(bWidth,WalkHeight-1,0,false,2,lines); }
			 */
		}//end main loop
        flushDelayed();
		setCursor(0);
	}

    /**
     * Builds a gateway and road on one side of gateway. Call after build() and before buildTowers().
	 *
     * startScan,endScan - bounds of where to look to place gateway
     * @param scanWindow
     * @param scanStart Bounds of where to look to place gateway?
     * @param gateHeight Height of the gateway in the wall
     * @param gateWidth Width of the gateway in the wall
     * @param rs Wall style of avenues
     * @param flankTHand The hand to build flanking towers on. 0 =&gt; n0 flanking towers.
     * @param XMaxLen Maximum length of avenues for the +X side avenues
     * @param XTarget The target point fo the +X side avenues
     * @param XHand The internal handedness of the +X side avenues
     * @param antiXMaxLen Maximum length of avenues for the -X side avenues
     * @param antiXTarget The target point fo the -X side avenues
     * @param antiXHand The internal handedness of the -X side avenues
     *
     * @return y-position where gateway was built or -1 if not gateway was built.
     */
    public BuildingWall[] buildGateway(int[] scanWindow, int scanStart, int gateHeight,
            int gateWidth, TemplateWall rs, Handedness flankTHand, int XMaxLen, int[] XTarget,
            Handedness XHand, int antiXMaxLen, int[] antiXTarget, Handedness antiXHand) {
		BuildingWall[] avenues = null;
		if (rs != null)
			gateWidth = rs.WWidth;
		if (scanStart < scanWindow[0])
			scanStart = scanWindow[0];
		if (scanStart > scanWindow[1])
			scanStart = scanWindow[1];
		int scanA = scanStart, scanB = scanStart + 1;
		for (boolean aOrB = true; scanA >= scanWindow[0] || scanB <= scanWindow[1]; aOrB = !aOrB) {
			setCursor(aOrB ? scanA : scanB);
			if (aOrB) {
				if (scanA < scanWindow[0])
					continue;
				scanA -= 3;
			} else {
				if (scanB > scanWindow[1])
					continue;
				scanB += 3;
			}
			if (n0 - gateWidth - 1 >= 0) {
				if (curvature(zArray[n0], zArray[n0 - gateWidth / 2], zArray[n0 - gateWidth - 1], 1) == 0 && curvature(xArray[n0], xArray[n0 - gateWidth / 2], xArray[n0 - gateWidth - 1], 0) == 0) {
					int tw = ws.pickTWidth(circular, world.rand), th = ws.getTMaxHeight(circular);
					if (rs != null) {
                        avenues =
                                new BuildingWall[] {
                                        new BuildingWall(bID, config, rs, bDir.rotate(bHand.num),
                                                XHand, XMaxLen, false, getIJKPt(bWidth, 0,
                                                        XHand != bHand ? 1 - gateWidth : 0)),
                                        new BuildingWall(bID, config, rs, bDir.rotate(bHand.num),
                                                antiXHand, antiXMaxLen, false, getIJKPt(-1, 0,
                                                        antiXHand == bHand ? 1 - gateWidth : 0))};
                        avenues[0].setTarget(XTarget == null ? getIJKPt(bWidth + tw, 0,
                                XHand != bHand ? 1 - gateWidth : 0) : XTarget);
						avenues[0].plan(1, 0, DEFAULT_LOOKAHEAD, true);
						if (XTarget == null && avenues[0].bLength >= tw) {
							avenues[0].target = false;
							avenues[0].plan(tw + 1, 0, DEFAULT_LOOKAHEAD, true);
						}
						if (avenues[0].bLength >= MIN_GATEWAY_ROAD_LENGTH) {
							avenues[1].setTarget(antiXTarget == null ? getIJKPt(-1 - tw, 0, antiXHand == bHand ? 1 - gateWidth : 0) : antiXTarget);
							avenues[1].plan(1, 0, DEFAULT_LOOKAHEAD, true);
							if (antiXTarget == null && avenues[1].bLength >= tw) {
								avenues[1].target = false;
								avenues[1].plan(tw + 1, 0, DEFAULT_LOOKAHEAD, true);
							}
						}
					}
					//build it
					//gateway is built from n0-gateWidth+1 to n0
					if (rs == null || avenues[1].bLength >= MIN_GATEWAY_ROAD_LENGTH) {
						if (rs != null) {
							avenues[0].smooth(10, 10, false);
							avenues[1].smooth(10, 10, false);
						}
						Block fenceBlock = bRule.chance < 100 || bRule.primaryBlock.get() == Blocks.nether_brick ? Blocks.nether_brick_fence : Blocks.fence;
                        int fenceX;
                        if (flankTHand == null)
                            fenceX = bWidth / 2;
                        else if (flankTHand == bHand)
                            fenceX = bWidth - 2 + ws.TowerXOffset;
                        else
                            fenceX = 1 - ws.TowerXOffset;
						gateHeight = Math.min(gateHeight, bHeight - 1);
						for (int y1 = 0; y1 > -gateWidth; y1--) {
							//gateway
							for (int x1 = 0; x1 < bWidth; x1++)
								for (int z1 = 0; z1 < gateHeight; z1++)
									if (!((y1 == 0 || y1 == 1 - gateWidth) && z1 == gateHeight - 1))
										setBlockLocal(x1, z1, y1, Blocks.air);
							//fence gate
							for (int z1 = gateHeight - 2; z1 < gateHeight; z1++)
								if (random.nextInt(100) < bRule.chance)
									setBlockLocal(fenceX, z1, y1, fenceBlock);
						}
                        if (flankTHand == bHand)
							setBlockLocal(-1 - ws.TowerXOffset, gateHeight - 2, -gateWidth, WEST_FACE_TORCH_BLOCK);
                        if (flankTHand == bHand)
							setBlockLocal(-1 - ws.TowerXOffset, gateHeight - 2, 1, WEST_FACE_TORCH_BLOCK);
						if (flankTHand != bHand)
							setBlockLocal(bWidth + ws.TowerXOffset, gateHeight - 2, -gateWidth, EAST_FACE_TORCH_BLOCK);
						if (flankTHand != bHand)
							setBlockLocal(bWidth + ws.TowerXOffset, gateHeight - 2, 1, EAST_FACE_TORCH_BLOCK);
						//build flanking towers
						if (n0 + gateWidth + tw > bLength)
                            flankTHand = null;
                        if (flankTHand != null) {
							int tnMid1 = n0 - gateWidth - tw / 2;
							int tnMid2 = n0 + tw / 2 + 1;
							int x1 = flankTHand == bHand ? bWidth - 1 + ws.TowerXOffset : -ws.TowerXOffset;
							//preceding tower
                            new BuildingTower(0, this, bDir.rotate(flankTHand), bHand, true, tw,
                                    th, tw, getIJKPtAtN(tnMid1, x1, 0, 0)).build(0, 0, false);
							//following tower
                            new BuildingTower(0, this, bDir.rotate(flankTHand), bHand.opposite(), true, tw,
                                    th + zArray[tnMid1] - zArray[tnMid2], tw, getIJKPtAtN(tnMid2,
                                            x1, 0, 0)).build(0, 0, false);
						}
                        flushDelayed();
						//stairs up to wall
						gatewayStart = n0 - gateWidth + 1;
						gatewayEnd = n0;
						if (bWidth + 2 * ws.TowerXOffset >= 5) {
							int ngw1 = n0 - gateWidth, ngw2 = n0 + 1;
                            int x2 =
                                    (flankTHand == null || flankTHand == bHand) ? 1 - ws.TowerXOffset
                                            : bWidth - 2 + ws.TowerXOffset;
							for (int n1 = ngw1; n1 > ngw1 - 5; n1--) {
								if (zArray[n1 - 3] == zArray[n1] && xArray[n1 - 3] == xArray[ngw1 + 1]) {
                                    new BuildingSpiralStaircase(config, bRule, bDir, bHand, false,
                                            -WalkHeight, getIJKPtAtN(n1, x2, WalkHeight - 2, -3))
                                            .build(1, ngw1 - n1 + 4);
									gatewayStart = n1 - 3;
									break;
								}
							}
							for (int n1 = ngw2; n1 < ngw2 + 5; n1++) {
								if (zArray[n1 + 3] == zArray[n1] && xArray[n1 + 3] == xArray[ngw2 - 1]) {
                                    new BuildingSpiralStaircase(config, bRule, bDir.opposite(),
                                            bHand.opposite(), false, -WalkHeight, getIJKPtAtN(n1, x2,
                                                    WalkHeight - 2, 3)).build(1, n1 - ngw2 + 5);
									gatewayEnd = n1 + 3;
									break;
								}
							}
						}
                        gatewayStart -= (flankTHand != null ? tw + ws.BuildingInterval / 2 : 0);
                        gatewayEnd += (flankTHand != null ? tw + ws.BuildingInterval / 2 : 0);
						return avenues;
					}
				}
			}
		}
		return null;
	}

	//****************************************  FUNCTION  - failString  *************************************************************************************//
	public String failString() {
		switch (failCode) {
		case OBSTRUCTED:
			return "Obstructed.";
		case UNDERWATER:
			return "Underwater.";
		case TOOSTEEPDOWN:
			return "Too Steep Down.";
		case TOOSTEEPUP:
			return "Too Steep Up.";
		case HITWALL:
			return "Hit Wall";
		case CANNOTEXPLORE:
			return "Could not explore";
		case HITTARGET:
			return "Hit Target";
		case MAXLENGTH:
			return "Max length (" + maxLength + ") reached.";
		case NOTHING:
		default:
			return "No Fail.";
		}
	}

	//****************************************  FUNCTION  - getIJKPtAtN *************************************************************************************//
	public int[] getIJKPtAtN(int n, int x, int z, int y) {
		if (n == n0)
			return getIJKPt(x, z, y);
		return getIJKPt(x + xArray[n] - xArray[n0], z + zArray[n] - zArray[n0], y + n - n0);
	}

	//****************************************  FUNCTION - makeBuildings *************************************************************************************//
	public void makeBuildings(boolean buildOnL, boolean buildOnR, boolean makeGatehouseTowers, boolean overlapTowers, boolean isAvenue) {
		if (ws == null) {
			logger.error("Tried to build towers but wall style was null!");
			return;
		}
		if (!ws.MakeBuildings)
			return;
		makeGatehouseTowers = makeGatehouseTowers && ws.makeDefaultTower.weight > 0 && !circular;
		int cursorStart = Math.max(ws.getTMaxWidth(circular) + 3, 2 * ws.BuildingInterval / 3);
		//main loop
		for (setCursor(cursorStart); n0 < bLength; setCursor(n0 + 1)) {
			//don't built if there's a gateway
			if (gatewayStart != NO_GATEWAY && n0 >= gatewayStart && n0 <= gatewayEnd + ws.getTMaxWidth(circular) + 2) {
				setCursor(gatewayEnd + ws.getTMaxWidth(circular) + 2);
				if (n0 >= bLength)
					break;
			}
			//tw is used to see if curvature permits building a tower here. Non default tower buildings will also
			//use this tw for curvature even though it is incorrect since they will determine their own width.
			//This may cause so building-over but that's OK.
			//tw is also passed to  as the actual width for default towers inside makeBuilding().
			int tw = ws.pickTWidth(circular, world.rand);
			//towers are built from n0-2 to n0-tw-1
			//n0 and nBack used to calculat curvature are 2 further from nMid
			int nMid = n0 - tw / 2 - 2, nBack = n0 - tw - 3;
            Handedness clearSide =
                    Handedness
                            .fromInt(-(bHand.num)
                                    * Integer.signum(curvature(xArray[nBack], xArray[nMid],
                                            xArray[n0], 0)));
            if (clearSide == null) {
				if (buildOnL && buildOnR)
                    clearSide = Handedness.hands[random.nextInt(2)];
				else
                    clearSide = buildOnL ? Handedness.L_HAND : Handedness.R_HAND;
			}
			//try tower types
			if (makeGatehouseTowers && curvature(zArray[nBack], zArray[nMid], zArray[n0], 0) == 0 && curvature(xArray[nBack], xArray[nMid], xArray[n0], 2) == 0) {
				//FMLLog.getLogger().info("Building gatehouse for "+IDString()+" at n="+n0+" "+globalCoordString(0,0,0)+" width "+tw);
                BuildingTower tower =
                        new BuildingTower(bID + n0, this, bDir.opposite(), bHand.opposite(), true,
                                tw, ws.pickTHeight(circular, world.rand), circular ? tw
                                        : ws.pickTWidth(circular, world.rand), getIJKPtAtN(nMid,
                                        bWidth / 2, 0, tw / 2));
				if (!tower.isObstructedRoof(-1)) {
                    if (layoutGenerator != null)
                        layoutGenerator.setLayoutCode(tower.getIJKPt(0, 0, 0),
                                tower.getIJKPt(tw - 1, 0, tw - 1), LayoutCode.TOWER);
					tower.build(xArray[n0 - 1] - xArray[nMid], xArray[nBack + 1] - xArray[nMid], false);
					setCursor(n0 + ws.BuildingInterval - 1);
				}
            } else if ((buildOnL && clearSide == Handedness.L_HAND)
                    || (buildOnR && clearSide == Handedness.R_HAND)) { // side towers
				//FMLLog.getLogger().info("Building side tower for "+IDString()+" at n="+n0+" "+globalCoordString(0,0,0)+" with clearSide="+clearSide+" width "+tw);
				TemplateTML template = ws.buildings.get(PickWeighted.pickWeightedOption(world.rand, ws.buildingWeights[0], ws.buildingWeights[1]));
				int ybuffer = -ws.TowerXOffset + (isAvenue ? 0 : 1);
				int[] pt = getIJKPtAtN(nMid, clearSide == bHand ? (bWidth - ybuffer) : ybuffer - 1, 0, 0);
                if (makeBuilding(template, tw, ybuffer, overlapTowers, bDir.rotate(clearSide), pt))
					setCursor(n0 + ws.BuildingInterval - 1);
			}
		}
		setCursor(0);
		//build towers at endpoints
		if (endBLength >= BuildingTower.TOWER_UNIV_MIN_WIDTH) {
			int endTN = circular ? bLength - 2 : bLength - 1;
			if (endTN < 0)
				endTN = 0;
			int[] pt = getIJKPtAtN(endTN, bWidth / 2, 0, 1);
			makeBuilding(endBTemplate, ws.pickTWidth(circular, world.rand), 1, overlapTowers, bDir, pt);
		}
	}

    /**
     * Plans the wall.
     * <p>
     * <b>Assumptions</b>
     * <p>
     * xarray and zarray contain planned values up to startN-1 inclusive.
     * <p>
     * <b>Side Effects</b>
     * <p>
     * <ul>
     *   <li>planL set to total length now planned.</li>
     *   <li>xarry and zarry are filled up to planL.</li>
     *   <li>hitMaxDepth true if planning was terminated due to depth==MAX_BACKTRACK_DEPTH.</li>
     *   <li>failString contains termination rationale.</li>
     * </ul>
     *
     * @param startN
     * @param depth
     * @param lookahead
     * @param stopAtWall
     *
     * @return Length of the new wall planned.
     */
	public int plan(int startN, int depth, int lookahead, boolean stopAtWall) {
		if (startN < 1 || startN >= maxLength) {
            logger.error("Bad start length at BuildingWall.plan:" + startN);
			return 0;
		}
		int fails = 0;
		setOriginLocal(i1, j1, k1, xArray[startN - 1], zArray[startN - 1], startN);
		bLength = startN;
        logger.debug("planWall " + IDString() + ", depth=" + depth + " n=" + startN + " maxlLen="
                + maxLength + " at " + (new ChunkCoordinates(i0, j0, k0)));
		//int searchUp=Math.min(Math.max(MIN_SEARCHUP,WalkHeight+1),MAX_SEARCHUP);
		int searchUp = MIN_SEARCHUP;
		int obstructionHeight = WalkHeight > 4 ? WalkHeight + 1 : bHeight + 1;
		while (true) {
			int gradx = 0, gradz = 0;
			failCode = FailType.NOTHING;
			for (int x1 = -1; x1 <= bWidth; x1++) {
				for (int z1 = -SEARCHDOWN; z1 <= searchUp; z1++) {
					Block blockId = getBlockIdLocal(x1, z1, 0);
					if (!BlockProperties.get(blockId).isWallable) {
						gradz++;
						gradx += Integer.signum(2 * x1 - bWidth + 1);
					} else if (BlockProperties.get(blockId).isWater)
						gradx -= Integer.signum(2 * x1 - bWidth + 1);
					//hit another wall, want to ignore sandstone that appears naturally in deserts
					if ((stopAtWall || z1 < -2) && isArtificialWallBlock(x1, z1, 0))
						failCode = FailType.HITWALL;
				}
				if (BlockProperties.get(getBlockIdLocal(x1, ws.waterHeight + 1, 0)).isWater)
					failCode = FailType.UNDERWATER;
				if (!isWallable(x1, obstructionHeight, 0) && failCode == FailType.NOTHING)
					failCode = FailType.OBSTRUCTED;
			}
			gradz = (gradz + (bWidth + 2) / 2) / (bWidth + 2) - SEARCHDOWN;
			if (failCode == FailType.HITWALL)
				gradz = 0;
			if (failCode == FailType.NOTHING && gradz < -1)
				failCode = FailType.TOOSTEEPDOWN;
			if (failCode == FailType.NOTHING && gradz > 4)
				failCode = FailType.TOOSTEEPUP;
			gradz = Integer.signum(gradz);
			if (minJ != NO_MIN_J && zArray[bLength - 1] + gradz + j1 < minJ)
				gradz = 0; //don't go below minJ
			if (gradz == 0) {
				int HorizForceThreshold = bWidth / 2;
				int bias = target ? Integer.signum(xArray[bLength - 1] - x_targ) * (2 * HorizForceThreshold) : 0;
				gradx = (gradx > HorizForceThreshold + bias ? 1 : (gradx < -HorizForceThreshold + bias ? -1 : 0));
			} else
				gradx = 0;
			setOriginLocal(i0, j0, k0, gradx, gradz, 1);
			xArray[bLength] = xArray[bLength - 1] + gradx;
			zArray[bLength] = zArray[bLength - 1] + gradz;
			bLength++;
			//%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%   TERMINATION / BACKTRACKING   %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
			if (failCode == FailType.NOTHING)
				fails = 0;
			else
				fails++;
			if (target && bLength > y_targ) {
				failCode = FailType.HITTARGET;
				break;
			} else if (bLength >= maxLength) {
				failCode = FailType.MAXLENGTH;
				break;
			} else if (failCode == FailType.HITWALL || failCode == FailType.UNDERWATER) {
				bLength -= fails;
				break;
			} else if (fails >= lookahead) {
				bLength -= fails; //planL should be at first failed position at end of loop
				if (bLength - startN < Backtrack || (bLength - startN < MIN_BRANCH_IMPROVEMENT && depth != 0)) {
					break; //loop termination condition 2
				}
				if (depth >= MAX_BACKTRACK_DEPTH) { //loop termination condition 2
					hitMaxDepth = true; //may still be able to proceed, note this so we can do so from root
					break; //loop termination condition 3
				} else {
					logger.debug("\nTrying branches for "+IDString()+", depth="+depth+" at n="+bLength+" x="+(xArray[bLength])+" z="+(zArray[bLength]));
					int improvement, bestImprovement = 0;
					BuildingWall branch, bestBranch = null;
					//String[] branchNames={"Down","Minus","Straight","Plus","Up"};
					for (int zAx = 0; zAx <= 1; zAx++) {
						for (int d = -1; d <= 1; d++) {
							if (!(zAx == 0 && d == 0)) {
								branch = new BuildingWall(this, maxLength, i1, j1, k1);
								for (int m = 0; m < Backtrack; m++) {
									branch.xArray[bLength - Backtrack + m] = xArray[bLength - Backtrack] + (1 - zAx) * (d * m);
									branch.zArray[bLength - Backtrack + m] = zArray[bLength - Backtrack] + zAx * (d * m);
								}
								improvement = branch.plan(bLength, depth + 1, lookahead, stopAtWall);
								if (improvement > bestImprovement) {
									bestBranch = branch;
									bestImprovement = improvement;
								}
							}
						}
					}
					if (bestImprovement + bLength > maxLength)
						bestImprovement = maxLength - bLength;
					if (bestImprovement > 0) {
						//if(DEBUG==3) System.out.println("Chose branch="+bestBranch.branchName+" for wall "+IDString()+"depth="+depth+" at n="+planL+" with added length="+bestImprovement);
						for (int m = bLength - Backtrack; m < bLength + bestImprovement; m++) {
							xArray[m] = bestBranch.xArray[m];
							zArray[m] = bestBranch.zArray[m];
							//failString=bestBranch.failString;
							failCode = bestBranch.failCode;
						}
						hitMaxDepth = bestBranch.hitMaxDepth;
						bLength += bestImprovement;
					}
					//else if(DEBUG) FMLLog.getLogger().info("Could not improve wall "+IDString()+" at n="+bLength+"\n");
					if (depth == 0 && hitMaxDepth && bLength < maxLength) {
						hitMaxDepth = false;
						fails = 1;
						//if(DEBUG) FMLLog.getLogger().info("Hit max search depth, continuing planning wall "+IDString()+"at n="+bLength+" from root");
					} else {
						break; //we have added branches if any and did not hit max depth, so break
					}
				}
				//if(DEBUG && planL>startN) printWall(startN);
			}
		}//end main loop
		if (depth == 0) {
			bLength -= endBLength;
			if (bLength < startN)
				bLength = startN;
		}
		setCursor(0);
		return bLength - startN;
	}

	public void printWall() {
        printWall(0);
	}

	public void printWall(int start) {
        logger.debug("Printing " + IDString() + " wall from n=" + start + " to n=" + (bLength - 1));
        String temp = "";
        for (int m = start; m < bLength; m++) {
            if (m % 10 == 0)
                temp += "|";
            if (m % 100 == 0)
                temp += "||";
            temp += xArray[m] + ",";
            if (m > 0 && Math.abs(xArray[m] - xArray[m - 1]) > 1)
                temp += "(ERROR: X-slope>1 at n=" + m + ")";
        }
        logger.debug(temp);
        temp = "";
        for (int m = start; m < bLength; m++) {
            if (m % 10 == 0)
                temp += "|";
            if (m % 100 == 0)
                temp += "||";
            temp += zArray[m] + ",";
            if (m > 0 && Math.abs(zArray[m] - zArray[m - 1]) > 1)
                temp += "(ERROR: Z-slope>1 at n=" + m + ")";
        }
        logger.debug(temp);
	}

	public boolean ptIsToXHand(int[] pt, int buffer) {
		setCursor(0);
		if (ws.TowerXOffset < 0)
			buffer -= ws.TowerXOffset;
		int ptY = getY(pt);
		if (ptY < 0)
			return getX(pt) >= buffer;
		if (ptY >= bLength)
			return getX(pt) >= xArray[bLength - 1] + buffer;
		return getX(pt) >= xArray[ptY] + buffer;
	}

    public boolean queryLayout(LayoutCode layoutCode) {
		for (int n = 0; n < bLength; n++) {
			setCursor(n);
            if (layoutGenerator != null
                    && !layoutGenerator.layoutIsClear(getIJKPt(0, 0, 0),
                            getIJKPt(bWidth - 1, 0, 0), layoutCode)) {
				setCursor(0);
				return false;
			}
		}
		setLayoutCode(layoutCode);
		return true;
	}

	//****************************************  FUNCTION  - setCursor  *************************************************************************************//
	//Sets building class cursor to wall origin
	public void setCursor(int n) {
		n0 = n;
		if (n0 >= 0 && (n0 < bLength || bLength == 0)) {
			setOriginLocal(i1, j1, k1, bLength == 0 ? 0 : xArray[n0], bLength == 0 ? 0 : zArray[n0], n0);
		}
	}

    public void setLayoutCode(LayoutCode layoutCode) {
		for (int n = 0; n < bLength; n++) {
			setCursor(n);
            if (layoutGenerator != null)
                layoutGenerator.setLayoutCode(getIJKPt(0, 0, 0), getIJKPt(bWidth - 1, 0, 0),
                        layoutCode);
		}
		setCursor(0);
	}

	//****************************************  FUNCTION  - setMinJ *************************************************************************************//
	public BuildingWall setMinJ(int minJ_) {
		minJ = minJ_;
		return this;
	}

    /**
     * Sets a target coordinate that the plan function can use to path towards.
     *
     * Will change EW and axY to reflect direction to target.
     *
     * @param targ An int array with the (x,y,z) of the target coordinate.
     * @return True if target is acceptable and reachable, false otherwise.
     */
	public boolean setTarget(int[] targ) {
        ChunkCoordinates localTarget = new ChunkCoordinates(targ[0], targ[1], targ[2]);
		if (targ[1] > 20 && Math.abs(j1 - targ[1]) < Math.max(Math.abs(i1 - targ[0]), Math.abs(k1 - targ[2]))) {
			target = true;
            setPrimaryAx((Math.abs(i1 - targ[0]) > Math.abs(k1 - targ[2]) ? (targ[0] > i1 ? Dir.EAST
                    : Dir.WEST)
                    : (targ[2] > k1 ? Dir.SOUTH : Dir.NORTH)));
			setCursor(0);
			x_targ = getX(targ);
			z_targ = getZ(targ);
			y_targ = getY(targ);
            logger.debug("Set target for "+IDString()+"to "+localCoordString(x_targ,z_targ,y_targ)+"!");
        } else {
            logger.debug("Could not set target for " + IDString() + ", targ=" + localTarget
                    + " (i,j,k)=" + (new ChunkCoordinates(i1, j1, k1)));
        }
		return target;
	}

	//****************************************  FUNCTION  - setTowers *************************************************************************************//
	public BuildingWall setTowers(BuildingWall bw) {
		circular = bw.circular;
		roofStyle = bw.roofStyle;
		towerRule = bw.towerRule;
		roofRule = bw.roofRule;
		endBLength = bw.endBLength;
		return this;
	}

	//****************************************  FUNCTION - smooth  *************************************************************************************//
	public void smooth(int convexWindow, int concaveWindow, boolean flattenEnds) {
		smooth(logger, xArray, 0, bLength - 1, convexWindow, concaveWindow, flattenEnds);
		smooth(logger, zArray, 0, bLength - 1, convexWindow, concaveWindow, flattenEnds);
	}

	//****************************************  FUNCTION - clearTrees *************************************************************************************//
	private void clearTrees() {
		for (int x1 = 0; x1 < bWidth; x1++)
			for (int z1 = bHeight + OVERHEAD_CLEARENCE; z1 < bHeight + OVERHEAD_TREE_CLEARENCE; z1++) {
				Block block = getBlockIdLocal(x1, z1, 0);
                if (BlockProperties.get(block).isTree)
                    setBlockLocal(x1, z1, 0, Blocks.air); //kill trees aggressively
			}
	}

	private void constructorHelper(TemplateWall ws_, int maxLength_, int i1_, int j1_, int k1_) {
		i1 = i1_;
		j1 = j1_;
		k1 = k1_;
		ws = ws_;
		WalkHeight = ws.WalkHeight;
		maxLength = maxLength_;
		xArray = new int[maxLength];
		zArray = new int[maxLength];
		bLength = 0;
		halfStairValue = bRule.primaryBlock.toStep();
	}

    private boolean makeBuilding(TemplateTML template, int tw, int ybuffer, boolean overlapTowers, Dir dir, int[] pt) {
		if (template == ws.makeDefaultTower) {
			int maxBL = bDir == dir ? endBLength : circular ? tw : ws.pickTWidth(false, world.rand);
			//FMLLog.getLogger().info("Querying "+(circular? "circular " : "square ")+(bDir==dir ? "end" : "side")+" tower, ybuffer="+ybuffer+".");
			for (int tl = maxBL; tl >= ws.getTMinWidth(circular); tl--) {
                BuildingTower tower =
                        new BuildingTower(bID + n0, this, dir, Handedness.R_HAND, true,
                                circular ? tl : tw, ws.pickTHeight(circular, world.rand), tl, pt);
				if (tower.queryCanBuild(ybuffer, overlapTowers)) {
					tower.build(0, 0, true);
					return true;
				}
			}
		} else if (template == ws.makeCARuin) {
			byte[][] caRule = ws.CARuinAutomataRules.get(random.nextInt(ws.CARuinAutomataRules.size()));
			for (int tries = 0; tries < 10; tries++) {
                ISeed seed = new SymmetricSeed(ws.CARuinContainerWidth, 0.5F);
                BuildingCellularAutomaton bca =
                        new BuildingCellularAutomaton(config, ws.CARuinRule, dir,
                                Handedness.R_HAND, true, ws.CARuinContainerWidth,
                                ws.CARuinMinHeight
                                        + random.nextInt(ws.CARuinMaxHeight - ws.CARuinMinHeight
                                                + 1), ws.CARuinContainerWidth,
                                seed.makeSeed(world.rand), caRule, null, pt);
				if (bca.plan(false, 12) && bca.queryCanBuild(ybuffer, ws.CARuinContainerWidth <= 15)) {
					bca.build(true, true);
					return true;
				}
			}
			//We've failed. If an end building, try making a tower instead
			if (bDir == dir && ws.makeDefaultTower.weight > 0) {
				return makeBuilding(ws.makeDefaultTower, tw, ybuffer, overlapTowers, dir, pt);
			}
		} else {
            BuildingTML buildingTML =
                    new BuildingTML(bID + n0, config, dir, Handedness.R_HAND, true, template, pt);
			if (buildingTML.queryCanBuild(ybuffer)) {
				buildingTML.build();
				return true;
			}
			//We've failed. If an end building, try making a tower instead
			if (bDir == dir && ws.makeDefaultTower.weight > 0) {
				return makeBuilding(ws.makeDefaultTower, tw, ybuffer, overlapTowers, dir, pt);
			}
		}
		return false;
	}

	//****************************************  FUNCTION - mergeWallLayer *************************************************************************************//
	private void mergeWallLayer() {
		//if side is a floor one below, add a step down
		if (isFloor(-1, WalkHeight - 1, 0))
			setBlockLocal(-1, WalkHeight - 1, 0, halfStairValue.get(), halfStairValue.getMeta());
		if (isFloor(bWidth, WalkHeight - 1, 0))
			setBlockLocal(bWidth, WalkHeight - 1, 0, halfStairValue.get(), halfStairValue.getMeta());
		//      x
		// if  xxo are floors one above, add a step up
		//      x
		if (isFloor(-1, WalkHeight + 1, 0) && isFloor(-2, WalkHeight + 2, 0) && isFloor(-2, WalkHeight + 2, 1) && isFloor(-2, WalkHeight + 2, -1))
			setBlockLocal(0, WalkHeight, 0, halfStairValue.get(), halfStairValue.getMeta());
		if (isFloor(bWidth, WalkHeight + 1, 0) && isFloor(bWidth + 1, WalkHeight + 2, 0) && isFloor(bWidth + 1, WalkHeight + 2, 1) && isFloor(bWidth + 1, WalkHeight + 2, -1))
			setBlockLocal(bWidth - 1, WalkHeight, 0, halfStairValue.get(), halfStairValue.getMeta());
		//clean up stairs descending into this wall
		int[] pt = getIJKPt(-1, WalkHeight - 1, 0);
		Block id = world.getBlock(pt[0], pt[1], pt[2]);
		int meta = world.getBlockMetadata(pt[0], pt[1], pt[2]);
        if (BlockProperties.get(id).isStair
                && STAIRS_META_TO_DIR[meta < 4 ? meta : (meta - 4)] == bDir
                        .rotate(bHand.opposite())) {
            BlockAndMeta temp = new BlockAndMeta(id, meta).stairToSolid();
            world.setBlock(pt[0], pt[1], pt[2], temp.get(), temp.getMeta(), 2);
        }
		pt = getIJKPt(bWidth, WalkHeight - 1, 0);
		id = world.getBlock(pt[0], pt[1], pt[2]);
		meta = world.getBlockMetadata(pt[0], pt[1], pt[2]);
        if (BlockProperties.get(id).isStair
                && STAIRS_META_TO_DIR[meta < 4 ? meta : (meta - 4)] == bDir.rotate(bHand)) {
            BlockAndMeta temp = new BlockAndMeta(id, meta).stairToSolid();
            world.setBlock(pt[0], pt[1], pt[2], temp.get(), temp.getMeta(), 2);
        }
	}

	private void pickTowers(boolean circular_, boolean endTowers) {
		circular = circular_;
		if (ws != null) {
			roofStyle = ws.pickRoofStyle(circular, world.rand);
			towerRule = ws.TowerRule.getFixedRule(world.rand);
			roofRule = ws.getRoofRule(circular);
			if (roofRule != TemplateRule.RULE_NOT_PROVIDED)
				roofRule = roofRule.getFixedRule(world.rand);
			if (endTowers && ws.MakeEndTowers) {
				endBTemplate = ws.buildings.get(PickWeighted.pickWeightedOption(world.rand, ws.buildingWeights[0], ws.buildingWeights[1]));
				endBLength = endBTemplate == ws.makeDefaultTower ? ws.pickTWidth(circular, world.rand) + 1 //+1 allows some extra wiggle room for roof edges etc.
						: (endBTemplate == ws.makeCARuin ? ws.CARuinContainerWidth : endBTemplate.length);
			}
		}
	}

	public static void smooth(Logger logger, int[] arry, int a, int b, int convexWindow, int concaveWindow, boolean flattenEnds) {
		int n, smoothStart, leadSlope, shorterWinStart = a, longerWinStart = a;
		int shorterWinInitEnd = a + Math.min(concaveWindow, convexWindow), longerWinInitEnd = a + Math.max(concaveWindow, convexWindow);
		for (int winEnd = a + 2; winEnd <= b; winEnd++) {
			if (winEnd >= shorterWinInitEnd)
				shorterWinStart++;
			if (winEnd >= longerWinInitEnd)
				longerWinStart++;
			n = winEnd - 1;
			leadSlope = arry[winEnd] - arry[n];
			//check the smaller window in both directions, and the larger only in the given direction
			if (leadSlope * (arry[n] - arry[shorterWinStart]) < 0)
				smoothStart = shorterWinStart;
			else if (leadSlope * (arry[n] - arry[longerWinStart]) < 0 && leadSlope * (convexWindow - concaveWindow) < 0)
				smoothStart = longerWinStart;
			else
				smoothStart = -1;
			if (smoothStart >= 0) {
                String smoothingdebugStr = "smoothing: ";
                for (int m = smoothStart; m <= winEnd; m++)
                    smoothingdebugStr += (arry[m] + ",");
                logger.debug(smoothingdebugStr);
				do {
                    logger.debug("smoothing n=" + n + " " + arry[n - 1] + " " + arry[n] + " " + arry[winEnd]);
					arry[n] = arry[winEnd];
					n--;
				} while (n > smoothStart && arry[n] != arry[winEnd]);
			}
		}
		//flatten the last two positions on each end if they end on slopes
		if (flattenEnds && b - a >= 2) {
			if (arry[a] != arry[a + 2])
				arry[a] = arry[a + 2];
			if (arry[b] != arry[b - 2])
				arry[b] = arry[b - 2];
			if (arry[a + 1] != arry[a + 2])
				arry[a + 1] = arry[a + 2];
			if (arry[b - 1] != arry[b - 2])
				arry[b - 1] = arry[b - 2];
		}
	}

    /**
     * Determines the curvature of a curve based upon 3 sample points of the
     * line. A curve can be straight (0), concave in the posive direction (1),
     * concave in the negative direction (-1), have an increasing slope (2), or
     * have a decreasing slope (-2).
     *
     * @param a An endpoint
     * @param b Midpoint
     * @param c Another endpoint
     * @param wiggle allows for some leeway before slope is detected
     *
     * @return <ul>
     *   <li>0 if constant (000)</li>
     *   <li>1 if concave up (+0+),(00+),(+00)</li>
     *   <li>-1 if concave down (-0-),(-00),(00-)</li>
     *   <li>2 if increasing (-0+)</li>
     *   <li>-2 if decreasing (+0-)</li>
     * </ul>
     */
	private static int curvature(int a, int b, int c, int wiggle) {
        int d1 = Util.signum(a - b, wiggle);
        int d2 = Util.signum(c - b, wiggle);
		if (d1 * d2 < 0)
			return 2 * d2;
        return Integer.signum(d1 + d2);
	}
}
