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
package generatormods.builders;

import generatormods.buildings.Building;
import generatormods.buildings.BuildingDoubleWall;
import generatormods.buildings.BuildingTower;
import generatormods.buildings.BuildingWall;
import generatormods.config.chests.ChestContentsSpec;
import generatormods.config.chests.ChestType;
import generatormods.config.templates.TemplateWall;
import generatormods.util.WorldUtil;
import generatormods.util.build.BlockProperties;
import generatormods.util.build.Dir;
import generatormods.util.build.Handedness;
import generatormods.walledcity.CityDataManager;
import generatormods.walledcity.ILayoutGenerator;
import generatormods.walledcity.LayoutCode;
import generatormods.walledcity.WalledCityChatHandler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;

import org.apache.logging.log4j.Logger;

import static generatormods.util.WorldUtil.HIT_WATER;
import static generatormods.util.WorldUtil.IGNORE_WATER;
import static generatormods.util.WorldUtil.SEA_LEVEL;
import static generatormods.util.WorldUtil.WORLD_MAX_Y;
import static generatormods.util.WorldUtil.findSurfaceJ;

/**
 * Generates walled cities in the Minecraft world. Walled cities are composed of 4 wall template
 * BuildingWalls in a rough rectangle, filled with many street template BuildingDoubleWalls.
 */
public class WalledCityBuilder extends AbstractBuilder implements ILayoutGenerator {
	private final static int GATE_HEIGHT = 6;
	private final static int JMEAN_DEVIATION_SLOPE = 10;
	private final static int LEVELLING_DEVIATION_SLOPE = 18;
	private final static int MIN_CITY_LENGTH = 40;
	private final static int MIN_SIDE_LENGTH = 10; //can be less than MIN_CITY_LENGTH due to squiggles
	private final static float MAX_WATER_PERCENTAGE = 0.4f;

	//**** WORKING VARIABLES **** 
	private TemplateWall ows, sws;
	private BuildingWall[] walls;
    private Handedness axXHand;
    private Dir[] dir = null;
	private int Lmean, jmean;
	private final int cityType;
	private int corner1[], corner2[], mincorner[];
    public LayoutCode[][] layout;

    private WalledCityChatHandler chatHandler;
    public CityDataManager cityDataManager;
	private List<TemplateWall> cityStyles;
    private boolean rejectOnPreexistingArtifacts;

    public WalledCityBuilder(World world, Random random, int chunkI, int chunkK,
            int triesPerChunk, double chunkTryProb, Logger logger,
            Map<ChestType, ChestContentsSpec> chestConfigs, WalledCityChatHandler chatHandler,
            CityDataManager cityDataManager, List<TemplateWall> cityStyles,
            boolean rejectOnPreexistingArtifacts) {
		super(world, random, chunkI, chunkK, triesPerChunk, chunkTryProb, logger, chestConfigs);
        this.chatHandler = chatHandler;
        this.cityDataManager = cityDataManager;
        this.cityStyles = cityStyles;
        this.rejectOnPreexistingArtifacts = rejectOnPreexistingArtifacts;

		cityType = world.provider.dimensionId;
	}

	@Override
	public boolean generate(int i0, int j0, int k0) {
        logger.debug("Attempting to generate WalledCity near ({},{},{})", i0, j0, k0);
        ows = TemplateWall.pickBiomeWeightedWallStyle(cityStyles, world, i0, k0, world.rand, false);
		if (ows == null)
			return false;
		sws = TemplateWall.pickBiomeWeightedWallStyle(ows.streets, world, i0, k0, world.rand, false);
		if (sws == null)
			return false;
        if (!cityDataManager.isCitySeparated(world, i0, k0, cityType)) {
            logger.debug("Too close to another WalledCity");
			return false;
        }
		int ID = (random.nextInt(9000) + 1000) * 100;
        int minJ = ows.LevelInterior ? SEA_LEVEL - 1 : BuildingWall.NO_MIN_J;
		//boolean circular=random.nextFloat() < ows.CircularProb;
		chooseDirection(i0 >> 4, k0 >> 4);
		//==========================      outer walls    ====================================================
        if (ows.MinL < MIN_CITY_LENGTH)
            ows.MinL = MIN_CITY_LENGTH;
		walls = new BuildingWall[4];
		ows.setFixedRules(world.rand);
		//plan walls[0]
		walls[0] = new BuildingWall(ID, this, ows, dir[0], axXHand, ows.MinL + random.nextInt(ows.MaxL - ows.MinL), false, i0, j0, k0).setMinJ(minJ);
		walls[0].plan(1, 0, BuildingWall.DEFAULT_LOOKAHEAD, true);
        if (walls[0].bLength < ows.MinL) {
            logger.debug(
                    "Abandoning because wall[0]: {} planned length {} is less than targeted length {}. Reason: {}",
                    walls[0].IDString(), walls[0].bLength, ows.MinL, walls[0].failString());
			return false;
        }
		//plan walls[1]
		walls[0].setCursor(walls[0].bLength - 1);
		walls[1] = new BuildingWall(ID + 1, this, ows, dir[1], axXHand, ows.MinL + random.nextInt(ows.MaxL - ows.MinL), false, walls[0].getIJKPt(-1 - ows.TowerXOffset, 0, 1 + ows.TowerXOffset))
				.setTowers(walls[0]).setMinJ(minJ);
		if (!cityDataManager.isCitySeparated(world, walls[1].i1, walls[1].k1, cityType)) {
            logger.debug("wall[1] is too close to another WalledCity");
			return false;
        }
		walls[1].plan(1, 0, BuildingWall.DEFAULT_LOOKAHEAD, false);
        if (walls[1].bLength < ows.MinL) {
            logger.debug(
                    "Abandoning because wall[1]: {} planned length {} is less than targeted length {}. Reason: {}",
                    walls[1].IDString(), walls[1].bLength, ows.MinL, walls[1].failString());
			return false;
        }
		//plan walls[2]
		walls[1].setCursor(walls[1].bLength - 1);
		int distToTarget = walls[0].bLength + walls[1].xArray[walls[1].bLength - 1];
        if (distToTarget < MIN_SIDE_LENGTH) {
            logger.debug("Rejecting because distToTarget: {} for wall[2] is less than ",
                    distToTarget, MIN_SIDE_LENGTH);
			return false;
        }
		walls[2] = new BuildingWall(ID + 2, this, ows, dir[2], axXHand, distToTarget + 2, false, walls[1].getIJKPt(-1 - ows.TowerXOffset, 0, 1 + ows.TowerXOffset)).setTowers(walls[0]).setMinJ(minJ);
        if (!cityDataManager.isCitySeparated(world, walls[2].i1, walls[2].k1, cityType)) {
            logger.debug("wall[2] is too close to another WalledCity");
			return false;
        }
		walls[2].setCursor(0);
		walls[2].setTarget(walls[2].getIJKPt(0, 0, distToTarget));
		walls[2].plan(1, 0, BuildingWall.DEFAULT_LOOKAHEAD, false);
		if (walls[2].bLength < walls[2].y_targ) {
            logger.debug(
                    "Abandoning because wall[2]: {} planned length {} is less than targeted length {}. Reason: {}",
                    walls[2].IDString(), walls[2].bLength, walls[2].y_targ, walls[2].failString());
			return false;
		}
		//plan walls[3]
		walls[2].setCursor(walls[2].bLength - 1);
		distToTarget = walls[1].bLength - walls[0].xArray[walls[0].bLength - 1] + walls[1].xArray[walls[1].bLength - 1];
        if (distToTarget < MIN_SIDE_LENGTH) {
            logger.debug("Rejecting because distToTarget: {} for wall[3] is less than {}",
                    distToTarget, MIN_SIDE_LENGTH);
			return false;
        }
		walls[3] = new BuildingWall(ID + 3, this, ows, dir[3], axXHand, distToTarget + 2, false, walls[2].getIJKPt(-1 - ows.TowerXOffset, 0, 1 + ows.TowerXOffset)).setTowers(walls[0]).setMinJ(minJ);
        if (!cityDataManager.isCitySeparated(world, walls[3].i1, walls[3].k1, cityType)) {
            logger.debug("wall[3] is too close to another WalledCity");
			return false;
        }
		walls[0].setCursor(0);
		walls[3].setCursor(0);
		walls[3].setTarget(walls[0].getIJKPt(-1 - ows.TowerXOffset, 0, -1 - ows.TowerXOffset));
		walls[3].plan(1, 0, BuildingWall.DEFAULT_LOOKAHEAD, false);
		if (walls[3].bLength < walls[3].y_targ) {
            logger.debug(
                    "Abandoning because wall[3]: {} planned length {} is less than targeted length {}. Reason: {}",
                    walls[3].IDString(), walls[3].bLength, walls[3].y_targ, walls[3].failString());
			return false;
		}
		//smoothing
        for (BuildingWall w : walls) {
            logger.debug("Smoothing wall {}", w.IDString());
			w.smooth(ows.ConcaveDownSmoothingScale, ows.ConcaveUpSmoothingScale, true);
        }
		//======================= Additional site checks =======================================
		//calculate the corners
		int[] xmax = new int[4];
		for (int w = 0; w < 4; w++) {
			xmax[w] = 0;
			for (int n = 0; n < walls[w].bLength; n++)
				if (walls[w].xArray[n] > xmax[w])
					xmax[w] = walls[w].xArray[n];
		}
		for (BuildingWall w : walls)
			w.setCursor(0);
		corner1 = walls[1].getIJKPt(xmax[1] + walls[1].bWidth + 1, 0, walls[0].xArray[walls[0].bLength - 1] - xmax[0] - walls[0].bWidth - 2);
		corner2 = walls[3].getIJKPt(xmax[3] + walls[3].bWidth + 1, 0, walls[2].xArray[walls[2].bLength - 1] - xmax[2] - walls[2].bWidth - 2);
		mincorner = new int[] { Math.min(corner1[0], corner2[0]), 0, Math.min(corner1[2], corner2[2]) };
		//reject cities if too z-displaced at corners
		Lmean = (walls[0].bLength + walls[1].bLength + walls[2].bLength + walls[3].bLength) / 4;
		jmean = 0;
		for (BuildingWall w : walls)
			for (int n = 0; n < w.bLength; n++)
                jmean += w.yArray[n] + w.j1;
		jmean /= (Lmean * 4);
		for (BuildingWall w : walls) {
			if (Math.abs(w.j1 - jmean) > w.bLength / JMEAN_DEVIATION_SLOPE) {
                logger.debug(
                        "Rejected city {}, height at corner differed from mean by {}, which is greater than {}.",
                        ID, (Math.abs(w.j1 - jmean)), w.bLength / JMEAN_DEVIATION_SLOPE);
				return false;
			}
		}
		int cityArea = 0, waterArea = 0;
        int incI = Integer.signum(corner2[0] - corner1[0]);
        int incK = Integer.signum(corner2[2] - corner1[2]);
		for (int i2 = corner1[0]; (corner2[0] - i2) * incI > 0; i2 += incI) {
			for (int k2 = corner1[2]; (corner2[2] - k2) * incK > 0; k2 += incK) {
				boolean enclosed = true;
				for (BuildingWall w : walls)
					if (w.ptIsToXHand(new int[] { i2, 0, k2 }, 1))
						enclosed = false;
				if (enclosed) {
                    int j2 = findSurfaceJ(world, i2, k2, WORLD_MAX_Y, true, 3);
					cityArea++;
					if (j2 == HIT_WATER)
						waterArea++;
                    if (rejectOnPreexistingArtifacts && ows.LevelInterior && BlockProperties.get(world.getBlock(i2, j2, k2)).isArtificial) {
                        logger.debug(
                                "Rejected: {}, city: {}, found previous construction in city zone!",
                                ows.name, ID);
						return false;
					}
				}
			}
		}
		if (!ows.LevelInterior && (float) waterArea / (float) cityArea > MAX_WATER_PERCENTAGE) {
            logger.debug("Rejected: {}, city: {}, too much water! City area was {}% water!",
                    ows.name, ID, (100.0f * waterArea / cityArea));
			return false;
		}
		//query the exploration handler again to see if we've built nearby cities in the meanwhile
		for (BuildingWall w : walls) {
            if (!cityDataManager.isCitySeparated(world, w.i1, w.k1, cityType)) {
                logger.debug("Rejected city: {} nearby city was built during planning!", ID);
				return false;
			}
		}
		//We've passed all checks, register this city site
		walls[0].setCursor(0);
		int[] cityCenter = new int[] { (walls[0].i1 + walls[1].i1 + walls[2].i1 + walls[3].i1) / 4, 0, (walls[0].k1 + walls[1].k1 + walls[2].k1 + walls[3].k1) / 4 };
        cityCenter[1] = findSurfaceJ(world, cityCenter[0], cityCenter[1], WORLD_MAX_Y, false, 3);
        cityDataManager.addCity(world, cityCenter[0], cityCenter[2], cityType);
        cityDataManager.saveCityLocations(world);
		//=================================== Build it! =========================================
        logger.info("Building: {}, city: {}, in biome: {}, between: {} and {}", ows.name, ID,
                world.getBiomeGenForCoordsBody(walls[0].i1, walls[0].k1).biomeName,
                walls[0].localCoordString(0, 0, 0), walls[2].localCoordString(0, 0, 0));
        chatHandler.tellAllPlayers( "Building city...");
		if (ows.LevelInterior)
			levelCity();
		TemplateWall avenueWS = TemplateWall.pickBiomeWeightedWallStyle(ows.streets, world, i0, k0, world.rand, false);
		LinkedList<BuildingWall> radialAvenues = new LinkedList<BuildingWall>();
		//layout
        layout = new LayoutCode[Math.abs(corner1[0] - corner2[0])][Math.abs(corner1[2] - corner2[2])];
		for (int x = 0; x < layout.length; x++)
			for (int y = 0; y < layout[0].length; y++)
                layout[x][y] = LayoutCode.EMPTY;
		for (BuildingWall w : walls)
            w.setLayoutCode(LayoutCode.WALL);
		int gateFlankingTowers = 0;
		for (BuildingWall w : walls) {
			//build city walls
			w.endBLength = 0;
			w.buildFromTML();
            Handedness radialAvenueHand =
                    w.bDir == dir[0] || w.bDir == dir[1] ? Handedness.L_HAND : Handedness.R_HAND;
            int startScan =
                    w.getZ(cityCenter) + (radialAvenueHand == w.bHand ? (avenueWS.WWidth - 1) : 0);
            BuildingWall[] avenues =
                    w.buildGateway(new int[] {w.bLength / 4, 3 * w.bLength / 4}, startScan,
                            GATE_HEIGHT, avenueWS.WWidth, avenueWS,
                            random.nextInt(6) < gateFlankingTowers ? null : axXHand, 500, null,
                            axXHand.opposite(), 150, cityCenter, radialAvenueHand);
            w.makeBuildings(axXHand == Handedness.L_HAND, axXHand == Handedness.R_HAND, true,
                    false, false);
			if (w.gatewayStart != BuildingWall.NO_GATEWAY)
				gateFlankingTowers++;
			//build avenues
			if (avenues != null) {
				avenues[0].buildFromTML();
				radialAvenues.add(avenues[1]);
			} else {
				//no gateway on this city side, try just building an interior avenue from midpoint
				w.setCursor(startScan);
                BuildingWall radialAvenue =
                        new BuildingWall(0, this, sws, w.bDir.rotate(axXHand.opposite()),
                                radialAvenueHand, ows.MaxL, false, w.getSurfaceIJKPt(-1, 0,
                                        WORLD_MAX_Y, false, IGNORE_WATER));
				radialAvenue.setTarget(cityCenter);
				radialAvenue.plan(1, 0, BuildingWall.DEFAULT_LOOKAHEAD, true);
				if (radialAvenue.bLength > 20) {
					radialAvenue.smooth(10, 10, true);
					radialAvenues.add(radialAvenue);
				}
			}
		}
		//corner towers
		for (BuildingWall w : walls)
			w.setCursor(0);
		if (ows.MakeEndTowers) { //allow MakeEndTowers to control corner towers so we can have an "invisible wall"...
			for (int w = 0; w < 4; w++) {
				if (walls[(w + 3) % 4].bLength > 2) {
                    int zmean =
                            (walls[w].yArray[2] - walls[w].j1
                                    + walls[(w + 3) % 4].yArray[walls[(w + 3) % 4].bLength - 3] + walls[(w + 3) % 4].j1) / 2;
					int minCornerWidth = ows.WWidth + 2 + (ows.TowerXOffset < 0 ? 2 * ows.TowerXOffset : 0);
					int TWidth = ows.getTMaxWidth(walls[w].circular) < minCornerWidth ? minCornerWidth : ows.getTMaxWidth(walls[w].circular);
                    BuildingTower tower =
                            new BuildingTower(ID + 10 + w, walls[w], dir[(w + 2) % 4],
                                    axXHand.opposite(), false, TWidth,
                                    ows.getTMaxHeight(walls[w].circular), TWidth,
                                    walls[w].getIJKPt(-2
                                            - (ows.TowerXOffset < 0 ? ows.TowerXOffset : 0), zmean,
                                            2));
                    setLayoutCode(tower.getIJKPt(0, 0, 0), tower.getIJKPt(TWidth - 1, 0, TWidth - 1), LayoutCode.TOWER);
					tower.build(0, 0, true);
				}
			}
		}
		//===============================================      streets   ===============================================
		//Plan/Build Order:
		//1)Plan radial avenues as part of wall building
		//2)Plan cross avenues
		//3)Build radial avenues
		//4)Plan Streets
		//5)Build cross avenues
		//6)Build streets
		//7)Build radial avenue buildings
		//8)Build cross avenue buildings
		//9)Build street buildings
		//build avenues and cross avenues
		boolean cityIsDense = ows.StreetDensity >= 3 * TemplateWall.MAX_STREET_DENSITY / 4;
		LinkedList<BuildingDoubleWall> crossAvenues = new LinkedList<BuildingDoubleWall>();
		int avInterval = cityIsDense ? 60 : Lmean > 110 ? 35 : 20;
		//maxStreetCount scales linearly with LMean since we fill 2-D city quadrants with 1-D objects.
		int maxStreetCount = Lmean * ows.StreetDensity / 20;
		for (BuildingWall radialAvenue : radialAvenues) {
			for (int n = radialAvenue.bLength - avInterval; n >= 20; n -= avInterval) {
				radialAvenue.setCursor(n);
                BuildingDoubleWall crossAvenue =
                        new BuildingDoubleWall(ID, this, sws,
                                radialAvenue.bDir.rotate(1), Handedness.R_HAND,
                                radialAvenue.getIJKPt(0, 0, 0));
				if (crossAvenue.plan())
					crossAvenues.add(crossAvenue);
			}
            radialAvenue.setLayoutCode(LayoutCode.AVENUE);
		}
		for (BuildingWall avenue : radialAvenues)
			avenue.buildFromTML();
		LinkedList<BuildingDoubleWall> plannedStreets = new LinkedList<BuildingDoubleWall>();
		for (int tries = 0; tries < maxStreetCount; tries++) {
			int[] pt = randInteriorPoint();
			if (pt != null) {
				pt[1]++;//want block above surface block
				sws = TemplateWall.pickBiomeWeightedWallStyle(ows.streets, world, i0, k0, world.rand, true);
				if (pt[1] != -1) {
					//streets
                    BuildingDoubleWall street =
                            new BuildingDoubleWall(ID + tries, this, sws, Dir.randomDir(random),
                                    Handedness.R_HAND, pt);
					if (street.plan()) {
						plannedStreets.add(street);
					}
				}
			}
		}
		for (BuildingDoubleWall avenue : crossAvenues)
            avenue.build(LayoutCode.AVENUE);
		for (BuildingDoubleWall street : plannedStreets)
            street.build(LayoutCode.STREET);
		//build towers
		for (BuildingWall avenue : radialAvenues)
			avenue.makeBuildings(true, true, false, cityIsDense, true);
		for (BuildingDoubleWall avenue : crossAvenues)
			avenue.buildTowers(true, true, false, cityIsDense, true);
		for (BuildingDoubleWall street : plannedStreets) {
			street.buildTowers(true, true, sws.MakeGatehouseTowers, cityIsDense, false);
		}

        logger.info("Built {} at {}", (isUnderground() ? "underground city" : "city"),
                (new ChunkCoordinates(i0, j0, k0)));
		chatHandler.chatCityBuilt(new int[] { i0, j0, k0, cityType, Lmean / 2 + 40 }, isUnderground());

        cityDataManager.addCityToVillages(world, ID);
		//printLayout(new File("layout.txt"));
		//guard against memory leaks
		layout = null;
		walls = null;
		return true;
	}

	@Override
    public boolean layoutIsClear(Building building, boolean[][] templateLayout, LayoutCode layoutCode) {
		for (int y = 0; y < templateLayout.length; y++) {
			for (int x = 0; x < templateLayout[0].length; x++) {
				if (templateLayout[y][x]) {
                    int i = building.getI(x, y);
                    int k = building.getK(x, y);
					if (i >= mincorner[0] && k >= mincorner[2] && i - mincorner[0] < layout.length && k - mincorner[2] < layout[0].length)
                        if (!layoutCode.canOverride(layout[i - mincorner[0]][k - mincorner[2]]))
							return false;
				}
			}
		}
		return true;
	}

	@Override
    public boolean layoutIsClear(int[] pt1, int[] pt2, LayoutCode layoutCode) {
		for (int i = Math.min(pt1[0], pt2[0]); i <= Math.max(pt1[0], pt2[0]); i++)
			for (int k = Math.min(pt1[2], pt2[2]); k <= Math.max(pt1[2], pt2[2]); k++)
				if (i >= mincorner[0] && k >= mincorner[2] && i - mincorner[0] < layout.length && k - mincorner[2] < layout[0].length)
                    if (!layoutCode.canOverride(layout[i - mincorner[0]][k - mincorner[2]]))
						return false;
		return true;
	}

	@Override
    public void setLayoutCode(Building building, boolean[][] templateLayout, LayoutCode layoutCode) {
		for (int y = 0; y < templateLayout.length; y++) {
			for (int x = 0; x < templateLayout[0].length; x++) {
				if (templateLayout[y][x]) {
					int i = building.getI(x, y), k = building.getK(x, y);
					if (i >= mincorner[0] && k >= mincorner[2] && i - mincorner[0] < layout.length && k - mincorner[2] < layout[0].length)
						layout[i - mincorner[0]][k - mincorner[2]] = layoutCode;
				}
			}
		}
	}

	@Override
    public void setLayoutCode(int[] pt1, int[] pt2, LayoutCode layoutCode) {
		for (int i = Math.min(pt1[0], pt2[0]); i <= Math.max(pt1[0], pt2[0]); i++)
			for (int k = Math.min(pt1[2], pt2[2]); k <= Math.max(pt1[2], pt2[2]); k++)
				if (i >= mincorner[0] && k >= mincorner[2] && i - mincorner[0] < layout.length && k - mincorner[2] < layout[0].length)
					layout[i - mincorner[0]][k - mincorner[2]] = layoutCode;
	}

	private void chooseDirection(int chunkI, int chunkK) {
        Map<Dir, Boolean> exploredChunk = new HashMap<Dir, Boolean>();
        exploredChunk.put(Dir.NORTH, world.blockExists(chunkI << 4, 0, (chunkK - 1) << 4));
        exploredChunk.put(Dir.EAST,  world.blockExists((chunkI + 1) << 4, 0, chunkK << 4));
        exploredChunk.put(Dir.SOUTH, world.blockExists(chunkI << 4, 0, (chunkK + 1) << 4));
        exploredChunk.put(Dir.WEST,  world.blockExists((chunkI - 1) << 4, 0, chunkK << 4));
		//pick an explored direction if it exists
        dir = new Dir[4];
        Dir randDir = Dir.randomDir(random);
        for (dir[0] = randDir.rotate(1); dir[0] != randDir; dir[0] = dir[0].rotate(1))
            if (exploredChunk.get(dir[0]))
				break; //this chunk has been explored so we want to go in this direction
		//Choose axXHand (careful it is opposite the turn direction of the square).
		//if RH direction explored, then turn RH; else turn LH;
		//axXHand=2*random.nextInt(2)-1;
        axXHand = exploredChunk.get((dir[0].rotate(1))) ? Handedness.L_HAND : Handedness.R_HAND;
        dir[1] = dir[0].rotate(axXHand.opposite());
        dir[2] = dir[1].rotate(axXHand.opposite());
        dir[3] = dir[2].rotate(axXHand.opposite());
	}

	private void levelCity() {
		for (BuildingWall w : walls)
			w.setCursor(0);
		int incI = Integer.signum(corner2[0] - corner1[0]);
        int incK = Integer.signum(corner2[2] - corner1[2]);
		int[] pt = new int[3];
        int jmin = world.provider.isHellWorld ? jmean : Math.max(jmean, SEA_LEVEL);
		for (BuildingWall w : walls) {
			for (int n = 0; n < w.bLength; n++)
                if (w.yArray[n] + w.j1 + w.WalkHeight - 1 < jmin
                        && (world.provider.isHellWorld || jmin >= SEA_LEVEL))
                    jmin = w.yArray[n] + w.j1 + w.WalkHeight - 1;
		}
		int jmax = Math.max(jmean + Lmean / LEVELLING_DEVIATION_SLOPE, jmin);
		for (pt[0] = corner1[0]; (corner2[0] - pt[0]) * incI > 0; pt[0] += incI) {
			for (pt[2] = corner1[2]; (corner2[2] - pt[2]) * incK > 0; pt[2] += incK) {
				boolean enclosed = true;
				for (BuildingWall w : walls)
					if (w.ptIsToXHand(pt, 1))
						enclosed = false;
				if (enclosed) {
                    pt[1] = findSurfaceJ(world, pt[0], pt[2], WORLD_MAX_Y, false, IGNORE_WATER);
					Block oldSurfaceBlockId = world.getBlock(pt[0], pt[1], pt[2]);
					if (pt[1] > jmax) {
						while (!world.isAirBlock(pt[0], pt[1] + 1, pt[2]))
							pt[1]++; //go back up to grab any trees or whatnot
						pt[1] += 10; //just to try to catch any overhanging blocks
						for (; pt[1] > jmax; pt[1]--)
							if (!world.isAirBlock(pt[0], pt[1], pt[2]))
                                WorldUtil.setBlockAndMetaNoLighting(world, pt[0], pt[1], pt[2],
                                        Blocks.air, 0, 2);
						if (!world.isAirBlock(pt[0], jmax - 1, pt[2]))
                            WorldUtil.setBlockAndMetaNoLighting(world, pt[0], jmax, pt[2],
                                    oldSurfaceBlockId, 0, 2);
					}
					if (pt[1] < jmin)
                        WorldUtil.fillDown(pt, jmin, world);
				}
			}
		}
		//update heightmap
		for (int chunkI = corner1[0] >> 4; ((corner2[0] >> 4) - chunkI) * incI > 0; chunkI += incI)
			for (int chunkK = corner1[2] >> 4; ((corner2[2] >> 4) - chunkK) * incK > 0; chunkK += incK)
				world.getChunkFromChunkCoords(chunkI, chunkK).generateSkylightMap();
	}

	private void printLayout(File f) {
		try {
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(f)));
			pw.println("  +y   ");
			pw.println("   ^   ");
			pw.println("+x<.>-x");
			pw.println("   v   ");
			pw.println("  -y   ");
			pw.println();
			for (int y = layout[0].length - 1; y >= 0; y--) {
				for (int x = layout.length - 1; x >= 0; x--) {
                    pw.print(layout[x][y].symbol);
				}
				pw.println();
			}
			pw.close();
		} catch (Exception e) {
		}
	}

	/**
	 * @return Coordinates (i,j,k) of interior surface point, j will be -1 if
	 *         point was water
	 */
	private int[] randInteriorPoint() {
		int tries = 0;
		int[] pt = new int[3];
        logger.debug(
                "Finding random interior point for city seeded at corner ({},{},{}) walls[0]: {}",
                walls[0].i1, walls[0].j1, walls[0].k1, walls[0].IDString());
		while (tries < 20) {
			pt[0] = mincorner[0] + random.nextInt(Math.abs(corner1[0] - corner2[0]));
			pt[2] = mincorner[2] + random.nextInt(Math.abs(corner1[2] - corner2[2]));
            pt[1] = findSurfaceJ(world, pt[0], pt[2], WORLD_MAX_Y, true, 3);
			boolean enclosed = true;
			for (BuildingWall w : walls)
				if (w.ptIsToXHand(pt, -sws.WWidth))
					enclosed = false;
			if (enclosed)
				return pt;
			tries++;
		}
		logger.warn("Could not find point within bounds!");
		return null;
	}

    protected boolean isUnderground() {
        return false;
    }

    @Override
    public CityDataManager getCityDataManager() {
        return cityDataManager;
    }

    @Override
    public ILayoutGenerator getLayoutGenerator() {
        return this;
    }
}
