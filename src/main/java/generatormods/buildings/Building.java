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

import generatormods.config.chests.ChestContentsSpec;
import generatormods.config.chests.ChestItemSpec;
import generatormods.config.chests.ChestType;
import generatormods.config.templates.TemplateRule;
import generatormods.util.PickWeighted;
import generatormods.util.WorldUtil;
import generatormods.util.blocks.BlockAndMeta;
import generatormods.util.blocks.BlockExtended;
import generatormods.util.blocks.PlacedBlock;
import generatormods.util.build.BlockProperties;
import generatormods.util.build.Dir;
import generatormods.util.build.Handedness;
import generatormods.walledcity.CityDataManager;
import generatormods.walledcity.ILayoutGenerator;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.minecraft.block.*;
import net.minecraft.entity.item.EntityPainting;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.world.World;

import org.apache.logging.log4j.Logger;

import static generatormods.util.WorldUtil.findSurfaceJ;
import static generatormods.util.build.DirToMetaMappings.BED_DIR_TO_META;
import static generatormods.util.build.DirToMetaMappings.BED_META_TO_DIR;
import static generatormods.util.build.DirToMetaMappings.BUTTON_DIR_TO_META;
import static generatormods.util.build.DirToMetaMappings.DOOR_DIR_TO_META;
import static generatormods.util.build.DirToMetaMappings.DOOR_META_TO_DIR;
import static generatormods.util.build.DirToMetaMappings.LADDER_DIR_TO_META;
import static generatormods.util.build.DirToMetaMappings.LADDER_META_TO_DIR;
import static generatormods.util.build.DirToMetaMappings.PAINTING_DIR_TO_FACEDIR;
import static generatormods.util.build.DirToMetaMappings.STAIRS_DIR_TO_META;
import static generatormods.util.build.DirToMetaMappings.STAIRS_META_TO_DIR;
import static generatormods.util.build.DirToMetaMappings.TRAPDOOR_DIR_TO_META;
import static generatormods.util.build.DirToMetaMappings.TRAPDOOR_META_TO_DIR;
import static generatormods.util.build.DirToMetaMappings.VINES_DIR_TO_META;
import static generatormods.util.build.DirToMetaMappings.VINES_META_TO_DIR;

/**
 * Building is a general class for buildings. Classes can inherit from Building
 * to build from a local frame of reference.
 * <p>
 * INFLIGHT: renaming x, y, and z to match minecrat concepts. Replacing X with
 * localX, Z with localY, and Y with localZ.
 * <p>
 * Note: Building currently swaps the terminology of Y and Z as Minecraft
 * uses them. In minecraft, y is up(+)-down(-), and z is south(+)-north(-).
 * Building and subclasses use z to refer to the Y axis, and y to refer to the
 * Z axis. So Minecraft's coordinate tuple is <code>(X, Y, Z)</code>, while
 * Generatormods uses <code>(X, Z, Y)</code>. Note that the order of the axes
 * is correct in Generatormods' Arrays and method arguments, just the
 * terminology is swapped for variable and method names.
 * <p>
 * Local frame of reference variables:
 * <ul>
 *   <li>i,j,k are coordinate inputs for global frame of reference functions.</li>
 *   <li>x,z,y are coordinate inputs for local frame of reference functions.</li>
 *   <li>bHand =-1,1 determines whether X-axis points left or right respectively when facing along Y-axis.</li>
 * </ul>
 *
 * <pre>
 *               (dir=0)
 *                (-k)
 *                 n
 *                 n
 *  (dir=3) (-i)www*eee(+i)  (dir=1)
 *                 s
 *                 s
 *                (+k)
 *               (dir=2)
 * </pre>
 */
public class Building {
    protected final IBuildingConfig config;
	protected final World world;
	protected final Random random;
    protected final Logger logger;
    protected final Map<ChestType, ChestContentsSpec> chestItems;
    protected final ILayoutGenerator layoutGenerator;
    protected final CityDataManager cityDataManager;
	protected TemplateRule bRule; // main structural blocktype
	public int bWidth, bHeight, bLength;
	public final int bID; // Building ID number
    private final LinkedList<PlacedBlock> delayedBuildQueue;

	protected boolean centerAligned; // if true, alignPt x is the central axis of the building if false, alignPt is the origin

    /**
     * The origin coordinates of the building (the world's (X, Y, Z)). A child
     * class might move the origin around as it progresses to use it as a
     * "cursor" position.
     */
    protected int i0, j0, k0;

    /* Used to for computations related to building orientation. */
    private int xI;
    /* Used to for computations related to building orientation. */
    private int zI;
    /* Used to for computations related to building orientation. */
    private int xK;
    /* Used to for computations related to building orientation. */
    private int zK;

    /**
     * Handedness of the secondary axis. R_HAND means the building's own X-axis
     * matches the world, while L_HAND means it is "mirrored".
     */
    public Handedness bHand;

    /**
     * Direction code of the building's primary axis.
     */
    public Dir bDir;

	// Special Blocks
	public final static int PAINTING_BLOCK_OFFSET = 3;
	public final static String[] SPAWNERS = new String[]{
            "Zombie", "Skeleton", "Spider", "Creeper", "PigZombie", "Ghast", "Enderman", "CaveSpider", "Blaze", "Slime",
            "LavaSlime", "Villager", "SnowMan", "MushroomCow", "Sheep", "Cow", "Chicken", "Squid", "Wolf", "Giant",
            "Silverfish", "EnderDragon", "Ozelot", "VillagerGolem", "WitherBoss", "Bat", "Witch"
    };

	private final static int LIGHTING_INVERSE_DENSITY = 10;
	private final static boolean[] randLightingHash = new boolean[512];
	static {
		Random rand = new Random();
		for (int m = 0; m < randLightingHash.length; m++)
			randLightingHash[m] = rand.nextInt(LIGHTING_INVERSE_DENSITY) == 0;
	}

    public Building(int ID_, IBuildingConfig config, TemplateRule buildingRule_, Dir dir_,
            Handedness axXHand_, boolean centerAligned_, int[] dim, int[] alignPt) {
        bID = ID_;
        this.config = config;
        this.world = config.getWorld();
        this.random = config.getRandom();
        this.logger = config.getLogger();
        this.cityDataManager = config.getCityDataManager();
        this.chestItems = config.getChestConfigs();
        this.layoutGenerator = config.getLayoutGenerator();
        bRule = buildingRule_;
        if (bRule == null)
            bRule = TemplateRule.STONE_RULE;
        bWidth = dim[0];
        bHeight = dim[1];
        bLength = dim[2];
        bHand = axXHand_;
        centerAligned = centerAligned_;
        setPrimaryAx(dir_);
        if (alignPt != null && alignPt.length == 3) {
            if (centerAligned)
                setOrigin(alignPt[0] - xI * bWidth / 2, alignPt[1], alignPt[2] - xK * bWidth / 2);
            else
                setOrigin(alignPt[0], alignPt[1], alignPt[2]);
        }
        delayedBuildQueue = new LinkedList<PlacedBlock>();
    }

    /**
     * Converts the given local building coordinates into the world's
     * coordinate system, for just the X (I) axis.
     */
    public final int getI(int x, int z) {
        return i0 + (zI * z) + (xI * x);
    }

    /**
     * Converts the given local building coordinates (relative to the cursor
     * position) into the world's coordinate system, for just the Y (J) axis.
     */
    public final int getJ(int y) {
        return j0 + y;
    }

    /**
     * Converts the given local building coordinates (relative to the cursor
     * position) into the world's coordinate system, for just the Z (K) axis.
     */
    public final int getK(int x, int z) {
        return k0 + zK * z + xK * x;
    }

    /**
     * Converts the given local building coordinates (relative to the cursor
     * position) into a world coordinate, based upon the building's
     * orientation.
     */
    public final int[] getIJKPt(int x, int y, int z) {
        int[] pt = new int[3];
        pt[0] = getI(x, z);
        pt[1] = getJ(y);
        pt[2] = getK(x, z);
        return pt;
    }

    /**
     * Get the surface point (in world coordinates) based on the local building
     * coordinates (relative to the cursor position).
     */
    public final int[] getSurfaceIJKPt(int x, int z, int j, boolean wallIsSurface, int waterSurfaceBuffer) {
        int[] pt = getIJKPt(x, 0, z);
        pt[1] = findSurfaceJ(world, pt[0], pt[2], j, wallIsSurface, waterSurfaceBuffer);
        return pt;
    }

    /**
     * Get the local X coordinate of the building based on the global pt.
     */
    public final int getX(int[] pt) {
        return xI * (pt[0] - i0) + xK * (pt[2] - k0);
    }

    /**
     * Get the local Y coordinate of the building based on the global pt.
     */
    public final int getY(int[] pt) {
        return pt[1] - j0;
    }

    /**
     * Get the local Z coordinate of the building based on the global pt.
     */
    public final int getZ(int[] pt) {
        return zI * (pt[0] - i0) + zK * (pt[2] - k0);
    }

    /**
     * Return a string gdescribing the global coordinate of a given local
     * coordinate.
     */
    public final String localCoordString(int x, int y, int z) {
        int[] pt = getIJKPt(x, y, z);
        return "(" + pt[0] + "," + pt[1] + "," + pt[2] + ")";
    }

    /**
     * Reorients dir in relation to this Building's orientation and handedness.
     *
     * @param dir The direction to reorient, in relation to the building's bDir and bHand.
     * @return The newly computed direction. If bHand were R_HAND, then it
     * essentially treats bDir as if it were the relative north and adjusts dir
     * accordingly to get a new absolute direction. If bHand is set to L_HAND,
     * then it would mirror dir the other way.
     */
    public Dir orientDirToBDir(Dir dir) {
        return bDir.reorient(bHand, dir);
    }

	// &&&&&&&&&&&&&&&&& SPECIAL BLOCK FUNCTION - setPainting
	// &&&&&&&&&&&&&&&&&&&&&&&&&&&&&//
	public void setPainting(int[] pt, int metadata) {
		// painting uses same orientation meta as ladders.
		// Have to adjust ijk since unlike ladders the entity exists at the
		// block it is hung on.
        Dir dir = orientDirToBDir(LADDER_META_TO_DIR[metadata]);
        pt[0] -= dir.i;
        pt[2] -= dir.k;
        switch (dir) {
            case NORTH:
                pt[2]++;
                break;
            case SOUTH:
                pt[2]--;
                break;
            case WEST:
                pt[0]++;
                break;
            default:
                pt[0]--;
        }
        EntityPainting entitypainting = new EntityPainting(world, pt[0], pt[1], pt[2], PAINTING_DIR_TO_FACEDIR.get(dir));
		if (!world.isRemote && entitypainting.onValidSurface())
			world.spawnEntityInWorld(entitypainting);
	}

    /**
     * Updates the building's orientation.
     * <p>
     * It computes several internal variables that are derived form the
     * building's orientation and handedness. If the building is NORTH oriented
     * and R_HAND, then the building's local X and Z axes match up. However, if
     * the building is L_HAND instead, then its X axis would be
     * inverted/mirrored compared to the world's axes.
     */
    public void setPrimaryAx(Dir dir_) {
		bDir = dir_;
		// changes of basis
        switch (bDir) {
        case NORTH:
            xI = bHand.num;
            zI = 0;
			xK = 0;
            zK = -1;
			break;
        case EAST:
			xI = 0;
            zI = 1;
            xK = bHand.num;
            zK = 0;
			break;
        case SOUTH:
            xI = -(bHand.num);
            zI = 0;
			xK = 0;
            zK = 1;
			break;
        case WEST:
			xI = 0;
            zI = -1;
            xK = -(bHand.num);
            zK = 0;
			break;
        }
    }

    public void setSignOrPost(int x2, int y2, int z2, boolean post, int sDir, String[] lines) {
        int[] pt = getIJKPt(x2, y2, z2);
		world.setBlock(pt[0], pt[1], pt[2], post ? Blocks.standing_sign : Blocks.wall_sign, sDir, 2);
		TileEntitySign tileentitysign = (TileEntitySign) world.getTileEntity(pt[0], pt[1], pt[2]);
		if (tileentitysign == null)
			return;
        System.arraycopy(lines, 0, tileentitysign.signText, 0, Math.min(lines.length, 4));
	}

    /**
     * Builds down to a maximum of maxDepth blocks + foundationDepth, starting
     * at local y. If buildDown column is completely air, it will instead build
     * down reserveDepth blocks.
     */
    public void buildDown(int x, int y, int z, TemplateRule buildRule, int maxDepth, int foundationDepth, int reserveDepth) {
        int stopY;
        for (stopY = y; stopY > y - maxDepth; stopY--) {
            if (!isWallable(x, stopY, z))
				break; // find ground height
		}
        if (stopY == y - maxDepth && isWallable(x, y - maxDepth, z)) // if we never hit ground
            stopY = y - reserveDepth;
		else
            stopY -= foundationDepth;
        for (int y1 = y; y1 > stopY; y1--) {
            setBlockWithLightingLocal(x, y1, z, buildRule, false);
		}
	}

    protected final Block getBlockIdLocal(int x, int y, int z) {
        return world.getBlock(getI(x, z), getJ(y), getK(x, z));
	}

    protected final int getBlockMetadataLocal(int x, int y, int z) {
        return world.getBlockMetadata(getI(x, z), getJ(y), getK(x, z));
	}

    /**
     * Describes the building in terms of its building ID and orientation.
     *
     * @return A string describing the building. The ID is the building ID, and
     * the axes describe the building's local "North" and handedness.
     */
	public final String IDString() {
        String str = this.getClass().toString() + "<ID="+ bID + " axes(Y,X)=";
		switch (bDir) {
            case SOUTH:
                return str + "(S," + (bHand == Handedness.R_HAND ? "W" : "E") + ")>";
            case NORTH:
                return str + "(N," + (bHand == Handedness.R_HAND ? "E" : "W") + ")>";
            case WEST:
                return str + "(W," + (bHand == Handedness.R_HAND ? "N" : "S") + ")>";
            case EAST:
                return str + "(E," + (bHand == Handedness.R_HAND ? "S" : "N") + ")>";
            default:
                return str + "(bad dir value: " + bDir + ")>";
		}
	}

    protected final boolean isArtificialWallBlock(int x, int y, int z) {
        Block blockId = getBlockIdLocal(x, y, z);
        return BlockProperties.get(blockId).isArtificial
                && !(blockId == Blocks.sandstone && (getBlockIdLocal(x, y + 1, z) == Blocks.sand || getBlockIdLocal(
                        x, y + 2, z) == Blocks.sand));
	}

    protected final boolean isDoorway(int x, int y, int z) {
        return isFloor(x, y, z)
                && (isWallBlock(x + 1, y, z) && isWallBlock(x - 1, y, z) || isWallBlock(x, y, z + 1)
                        && isWallBlock(x - 1, y, z - 1));
	}

    protected final boolean hasNoDoorway(int x, int y, int z) {
        return !(isDoorway(x - 1, y, z) || isDoorway(x + 1, y, z) || isDoorway(x, y, z - 1) || isDoorway(
                x - 1, y, z + 1));
	}

    protected boolean isObstructedFrame(int ystart, int zbuffer) {
        for (int y1 = ystart; y1 < bHeight; y1++) {
            // for(int x1=0; x1<length; x1++) for(int z1=zbuffer;
            // z1<width-1;z1++)
            // if(isWallBlock(x1,y1,z1))
			// return true;
            for (int x1 = 0; x1 < bWidth; x1++) {
                if (isArtificialWallBlock(x1, y1, bLength - 1)) {
					return true;
				}
			}
            for (int z1 = zbuffer; z1 < bLength - 1; z1++) {
                if (isArtificialWallBlock(0, y1, z1)) {
					return true;
				}
                if (isArtificialWallBlock(bWidth - 1, y1, z1)) {
					return true;
				}
			}
		}
		return false;
	}

	protected boolean isObstructedSolid(int pt1[], int pt2[]) {
		for (int x1 = pt1[0]; x1 <= pt2[0]; x1++) {
            for (int y1 = pt1[1]; y1 <= pt2[1]; y1++) {
                for (int z1 = pt1[2]; z1 <= pt2[2]; z1++) {
                    if (!isWallable(x1, y1, z1)) {
						return true;
					}
				}
			}
		}
		return false;
	}

    /**
     * True if block is air, andthe block below is a wall block.
     */
    protected final boolean isFloor(int x, int y, int z) {
        Block blkId1 = getBlockIdLocal(x, y, z), blkId2 = getBlockIdLocal(x, y - 1, z);
		// return ((blkId1==0 || blkId1==STEP_ID) && IS_WALL_BLOCK[blkId2] &&
		// blkId2!=LADDER_ID);
		return blkId1 == Blocks.air && BlockProperties.get(blkId2).isArtificial && blkId2 != Blocks.ladder;
	}

    protected final boolean isStairBlock(int x, int y, int z) {
        Block blkId = getBlockIdLocal(x, y, z);
		return blkId == Blocks.stone_slab || BlockProperties.get(blkId).isStair;
	}

    protected final boolean isWallable(int x, int y, int z) {
        return BlockProperties.get(world.getBlock(getI(x, z), getJ(y), getK(x, z))).isWallable;
	}

	protected final boolean isWallableIJK(int pt[]) {
		return pt!=null && BlockProperties.get(world.getBlock(pt[0], pt[1], pt[2])).isWallable;
	}

    protected final boolean isWallBlock(int x, int y, int z) {
        return BlockProperties.get(world.getBlock(getI(x, z), getJ(y), getK(x, z))).isArtificial;
	}

    public final void flushDelayed(){
        while(delayedBuildQueue.size()>0){
            PlacedBlock block = delayedBuildQueue.poll();
            setDelayed(block.getBlock(), block.x, block.y, block.z, block.getMeta());
        }
    }

	protected void setDelayed(Block blc, int...block) {
		if (BlockProperties.get(blc).isStair) {
            BlockAndMeta temp = getDelayedStair(blc, block);
            blc = temp.getBlock();
            block[3] = temp.getMeta();
		} else if (blc instanceof BlockVine) {
			if (block[3] == 0 && !isSolidBlock(world.getBlock(block[0], block[1] + 1, block[2])))
				block[3] = 1;
			if (block[3] != 0) {
                Dir dir = VINES_META_TO_DIR[block[3]];
				while (true) {
                    if (isSolidBlock(world.getBlock(block[0] + dir.i, block[1], block[2] + dir.k)))
						break;
                    dir = dir.rotate(1);
                    if (dir == VINES_META_TO_DIR[block[3]]) { // we've looped through everything
						if (isSolidBlock(world.getBlock(block[0], block[1] + 1, block[2]))) {
                            dir = null;
							break;
						}
						return; // did not find a surface we can attach to
					}
				}
                block[4] = dir == null ? 0 : VINES_DIR_TO_META.get(dir);
			}
		}
		// It seems Minecraft orients torches automatically, so I shouldn't have to do anything...
		// else if(block[3]==TORCH_ID || block[3]==REDSTONE_TORCH_ON_ID ||
		// block[3]==REDSTONE_TORCH_OFF_ID){
		// block[4]=1;
		// }
		if (blc == Blocks.air && block[3]>=PAINTING_BLOCK_OFFSET)//Remember:Paintings are not blocks
			setPainting(block, block[3]-PAINTING_BLOCK_OFFSET);
		else if (blc == Blocks.torch) {
			if (Blocks.torch.canPlaceBlockAt(world, block[0], block[1], block[2]))
				world.setBlock(block[0], block[1], block[2], blc, block[3], 3);// force lighting update
		} else if (blc == Blocks.glowstone)
			world.setBlock(block[0], block[1], block[2], blc, block[3], 3);// force lighting update
		else if(blc!=null) {
            if((randLightingHash[(block[0] & 0x7) | (block[1] & 0x38) | (block[2] & 0x1c0)]))
                world.setBlock(block[0], block[1], block[2], blc, block[3], 3);
            else
                WorldUtil.setBlockAndMetaNoLighting(world, block[0], block[1], block[2], blc,
                        block[3]);
        }
	}

    protected BlockAndMeta getDelayedStair(Block blc, int...block){
        // if stairs are running into ground. replace them with a solid block
        int dirX = block[0] - STAIRS_META_TO_DIR[block[3] % 4].i;
        int dirZ = block[2] - STAIRS_META_TO_DIR[block[3] % 4].k;
        if(world.getHeightValue(dirX, dirZ)>block[1]) {
            Block adjId = world.getBlock(dirX, block[1], dirZ);
            Block aboveID = world.getBlock(block[0], block[1] + 1, block[2]);
            if (BlockProperties.get(aboveID).isGround && BlockProperties.get(adjId).isGround) {
                return new BlockAndMeta(blc, block[3]).stairToSolid();
            } else if (!BlockProperties.get(adjId).isWallable || !BlockProperties.get(aboveID).isWallable) {
                return new BlockAndMeta(null, 0); // solid or liquid non-wall block. In this case, just don't build the stair (aka preserve block).
            }
        }
        return new BlockAndMeta(blc, block[3]);
    }

    /**
     * Recenters the building. The origin of this building was placed to match
     * a centerline. The building previously had bWidth=oldWidth, now it has
     * the current value of bWidth and needs to have origin updated.
     */
	protected final void recenterFromOldWidth(int oldWidth) {
        i0 += xI * (oldWidth - bWidth) / 2;
        k0 += xK * (oldWidth - bWidth) / 2;
	}

    protected final void setBlockLocal(int x, int y, int z, Block blockID) {
        setBlockLocal(x, y, z, blockID, 0);
	}

    protected final void setBlockLocal(int x, int y, int z, Block blockID, int metadata) {
        int[] pt = getIJKPt(x, y, z);
        if (blockID == Blocks.air && world.isAirBlock(pt[0], pt[1], pt[2]))
            return;
        if (!(blockID instanceof BlockChest))
            emptyIfChest(pt);
        if (BlockProperties.get(blockID).isDelayed){
            delayedBuildQueue.offer(new PlacedBlock(blockID, rotateMetadata(blockID, metadata),
                    new int[] {pt[0], pt[1], pt[2]}));
        } else if (randLightingHash[(x & 0x7) | (z & 0x38) | (y & 0x1c0)]) {
            world.setBlock(pt[0], pt[1], pt[2], blockID, rotateMetadata(blockID, metadata), 2);
        } else {
            WorldUtil.setBlockAndMetaNoLighting(world, pt[0], pt[1], pt[2], blockID,
                    rotateMetadata(blockID, metadata));
        }
        if (BlockProperties.get(blockID).isDoor) {
            addDoorToNewListIfAppropriate(pt[0], pt[1], pt[2]);
        }
	}

    protected final void setBlockLocal(int x, int y, int z, BlockAndMeta block) {
        if(block instanceof BlockExtended){
            setSpecialBlockLocal(x, y, z, block.getBlock(), block.getMeta(),
                    ((BlockExtended) block).info);
        }else{
            setBlockLocal(x, y, z, block.getBlock(), block.getMeta());
        }
	}

    protected final void setBlockLocal(int x, int y, int z, TemplateRule rule) {
        setBlockLocal(x, y, z, rule.getBlockOrHole(random));
	}

    protected final void setBlockWithLightingLocal(int x, int y, int z, TemplateRule rule,
            boolean lighting) {
        setBlockWithLightingLocal(x, y, z, rule.getBlockOrHole(random), lighting);
	}

    protected final void removeBlockWithLighting(int x, int y, int z) {
        setBlockWithLightingLocal(x, y, z, TemplateRule.AIR_RULE, true);
    }

    protected final void setBlockWithLightingLocal(int x, int z, int y, BlockAndMeta block, boolean lighting) {
        if(block instanceof BlockExtended){
            setSpecialBlockLocal(x, z, y, block.getBlock(), block.getMeta(),
                    ((BlockExtended) block).info);
        }else{
            setBlockWithLightingLocal(x, z, y, block.getBlock(), block.getMeta(), lighting);
        }
    }

    /**
     * Allows control of lighting. Also will build even if replacing air with air.
     */
    protected final void setBlockWithLightingLocal(int x, int y, int z, Block blockID, int metadata, boolean lighting) {
        int[] pt = getIJKPt(x, y, z);
        if (blockID == Blocks.air && world.isAirBlock(pt[0], pt[1], pt[2]))
            return;
		if (!(blockID instanceof BlockChest))
			emptyIfChest(pt);
		if (BlockProperties.get(blockID).isDelayed)
            delayedBuildQueue.offer(new PlacedBlock(blockID, rotateMetadata(blockID, metadata),
                    new int[] {pt[0], pt[1], pt[2]}));
		else if (lighting)
			world.setBlock(pt[0], pt[1], pt[2], blockID, rotateMetadata(blockID, metadata), 3);
		else
            WorldUtil.setBlockAndMetaNoLighting(world, pt[0], pt[1], pt[2], blockID,
                    rotateMetadata(blockID, metadata));
		if (BlockProperties.get(blockID).isDoor) {
			addDoorToNewListIfAppropriate(pt[0], pt[1], pt[2]);
		}
	}

	protected final void setOrigin(int i0_, int j0_, int k0_) {
		i0 = i0_;
		j0 = j0_;
		k0 = k0_;
	}

    protected final void setOriginLocal(int i1, int j1, int k1, int x, int y, int z) {
        i0 = i1 + zI * z + xI * x;
        j0 = j1 + y;
        k0 = k1 + zK * z + xK * x;
	}

    protected final void setSpecialBlockLocal(int x, int y, int z, Block blockID, int metadata,
            String extra) {
		if (extra.equals(TemplateRule.SPECIAL_AIR))
			return; // preserve existing world block
        int[] pt = getIJKPt(x, y, z);
		if (blockID instanceof BlockAir) {
            if(extra.equals(TemplateRule.SPECIAL_STAIR) && metadata<=0){
                world.setBlock(pt[0], pt[1], pt[2], Blocks.stone_slab, rotateMetadata(Blocks.stone_slab, -metadata), 2);
                return;
            }
            if (extra.equals(TemplateRule.SPECIAL_PAINT) && metadata>=PAINTING_BLOCK_OFFSET) {//Remember:Paintings are not blocks
                delayedBuildQueue.offer(new PlacedBlock(blockID, metadata, new int[] {pt[0], pt[1],
                        pt[2]}));
                return;
            }
			Block presentBlock = world.getBlock(pt[0], pt[1], pt[2]);
			if (!presentBlock.isAir(world, pt[0], pt[1], pt[2]) && !BlockProperties.get(presentBlock).isWater) {
				if (!(BlockProperties.get(world.getBlock(pt[0] - 1, pt[1], pt[2])).isWater || BlockProperties.get(world.getBlock(pt[0], pt[1], pt[2] - 1)).isWater
						|| BlockProperties.get(world.getBlock(pt[0] + 1, pt[1], pt[2])).isWater || BlockProperties.get(world.getBlock(pt[0], pt[1], pt[2] + 1)).isWater || BlockProperties.get(world.getBlock(pt[0], pt[1] + 1,
						pt[2])).isWater)) {// don't adjacent to a water block
					world.setBlockToAir(pt[0], pt[1], pt[2]);
				}
			}
		}else if(blockID instanceof BlockMobSpawner){
            setMobSpawner(pt, blockID, metadata, extra);
        }else if(blockID instanceof BlockChest){
			setLootChest(pt, blockID, metadata, extra);
        }else{
			world.setBlock(pt[0], pt[1], pt[2], blockID, metadata, 2);
		}
	}

	private void addDoorToNewListIfAppropriate(int par1, int par2, int par3) {
        if (cityDataManager == null)
            return;
		int l = ((BlockDoor) Blocks.wooden_door).func_150013_e(this.world, par1, par2, par3);
		int i1;
		if (l != 0 && l != 2) {
			i1 = 0;
			for (int j1 = -5; j1 < 0; ++j1) {
				if (this.world.canBlockSeeTheSky(par1, par2, par3 + j1)) {
					--i1;
				}
			}
			for (int j1 = 1; j1 <= 5; ++j1) {
				if (this.world.canBlockSeeTheSky(par1, par2, par3 + j1)) {
					++i1;
				}
			}
			if (i1 != 0) {
                cityDataManager.addDoor(bID, par1, par2, par3, 0, i1 > 0 ? -2 : 2, 0);
			}
		} else {
			i1 = 0;
			for (int j1 = -5; j1 < 0; ++j1) {
				if (this.world.canBlockSeeTheSky(par1 + j1, par2, par3)) {
					--i1;
				}
			}
			for (int j1 = 1; j1 <= 5; ++j1) {
				if (this.world.canBlockSeeTheSky(par1 + j1, par2, par3)) {
					++i1;
				}
			}
			if (i1 != 0) {
                cityDataManager.addDoor(bID, par1, par2, par3, i1 > 0 ? -2 : 2, 0, 0);
			}
		}
	}

	// ******************** LOCAL COORDINATE FUNCTIONS - HELPER FUNCTIONS
	// *************************************************************************************************************//
	private void emptyIfChest(int[] pt) {
		// if block is a chest empty it
		if (pt != null && world.getBlock(pt[0], pt[1], pt[2]) instanceof BlockChest) {
			TileEntityChest tileentitychest = (TileEntityChest) world.getTileEntity(pt[0], pt[1], pt[2]);
			for (int m = 0; m < tileentitychest.getSizeInventory(); m++)
				tileentitychest.setInventorySlotContents(m, null);
		}
	}

	private ItemStack getChestItemstack(String chestType) {
		if (chestType.equals(ChestType.TOWER) && random.nextInt(4) == 0) { // for tower chests, chance of returning the tower block
            return new ItemStack(bRule.primaryBlock.getBlock(), random.nextInt(10),
                    bRule.primaryBlock.getMeta());
		}
        ChestContentsSpec chestContentsSpec = chestItems.get(chestType);

        List<Integer> weights = new ArrayList<Integer>();
        for (ChestItemSpec chestItemSpec : chestContentsSpec.getChestItems()) {
            weights.add(chestItemSpec.getSelectionWeight());
        }
        ChestItemSpec chosenItem =
                PickWeighted.pickWeightedOption(world.rand, weights,
                        chestContentsSpec.getChestItems());
        Object obj = chosenItem.getBlockOrItem();
        if(obj == null){
            return null;
        }
        if(obj instanceof Block){
            if(obj == Blocks.air)
                return null;
            obj = Item.getItemFromBlock((Block)obj);
        }
        return new ItemStack((Item) obj, chosenItem.getMinStackSize()
                + random.nextInt(chosenItem.getMaxStackSize() - chosenItem.getMinStackSize() + 1),
                chosenItem.getMetadata());
	}

	private int rotateMetadata(Block blockID, int metadata) {
		int tempdata = 0;
		if (BlockProperties.get(blockID).isStair) {
			if (metadata >= 4) {
				tempdata += 4;
				metadata -= 4;
			}
            return STAIRS_DIR_TO_META.get(orientDirToBDir(STAIRS_META_TO_DIR[metadata])) + tempdata;
		}
		if (BlockProperties.get(blockID).isDoor) {
			// think of door metas applying to doors with hinges on the left
			// that open in (when seen facing in)
			// in this case, door metas match the dir in which the door opens
			// e.g. a door on the south face of a wall, opening in to the north
			// has a meta value of 0 (or 4 if the door is opened).
			if (metadata >= 8)// >=8:the top half of the door
				return metadata;
			if (metadata >= 4) {
				// >=4:the door is open
				tempdata += 4;
			}
            return DOOR_DIR_TO_META.get(orientDirToBDir(DOOR_META_TO_DIR[metadata % 4])) + tempdata;
		}
		if(blockID==Blocks.lever||blockID==Blocks.stone_button||blockID==Blocks.wooden_button){
			// check to see if this is flagged as thrown
			if (metadata - 8 > 0) {
				tempdata += 8;
				metadata -= 8;
			}
			if (metadata == 0 || (blockID==Blocks.lever && metadata >= 5))
				return metadata + tempdata;
            return BUTTON_DIR_TO_META.get(orientDirToBDir(STAIRS_META_TO_DIR[metadata - 1])) + tempdata;
        }else if(blockID==Blocks.torch||blockID==Blocks.redstone_torch||blockID==Blocks.unlit_redstone_torch){
			if (metadata == 0 || metadata >= 5) {
				return metadata;
			}
            return BUTTON_DIR_TO_META.get(orientDirToBDir(STAIRS_META_TO_DIR[metadata - 1]));
        }else if(blockID==Blocks.ladder||blockID==Blocks.dispenser||blockID==Blocks.furnace||blockID==Blocks.lit_furnace||blockID==Blocks.wall_sign||blockID==Blocks.piston||blockID==Blocks.piston_extension||blockID==Blocks.chest||blockID==Blocks.hopper||blockID==Blocks.dropper){
			if (blockID==Blocks.piston|| blockID==Blocks.piston_extension) {
				if (metadata - 8 >= 0) {
					// pushed or not, sticky or not
					tempdata += 8;
					metadata -= 8;
				}
			}
			if (metadata <= 1)
				return metadata + tempdata;
            return LADDER_DIR_TO_META.get(orientDirToBDir(LADDER_META_TO_DIR[metadata - 2])) + tempdata;
        }else if(blockID==Blocks.rail||blockID==Blocks.golden_rail||blockID==Blocks.detector_rail||blockID==Blocks.activator_rail){
            switch (bDir) {
            case NORTH:
				// flat tracks
				if (metadata == 0) {
					return 0;
				}
				if (metadata == 1) {
					return 1;
				}
				// ascending tracks
				if (metadata == 2) {
					return 2;
				}
				if (metadata == 3) {
					return 3;
				}
				if (metadata == 4) {
                    return bHand == Handedness.R_HAND ? 4 : 5;
				}
				if (metadata == 5) {
                    return bHand == Handedness.R_HAND ? 5 : 4;
				}
				// curves
				if (metadata == 6) {
                    return bHand == Handedness.R_HAND ? 6 : 9;
				}
				if (metadata == 7) {
                    return bHand == Handedness.R_HAND ? 7 : 8;
				}
				if (metadata == 8) {
                    return bHand == Handedness.R_HAND ? 8 : 7;
				}
				if (metadata == 9) {
                    return bHand == Handedness.R_HAND ? 9 : 6;
				}
            case EAST:
				// flat tracks
				if (metadata == 0) {
					return 1;
				}
				if (metadata == 1) {
					return 0;
				}
				// ascending tracks
				if (metadata == 2) {
					return 5;
				}
				if (metadata == 3) {
					return 4;
				}
				if (metadata == 4) {
                    return bHand == Handedness.R_HAND ? 2 : 3;
				}
				if (metadata == 5) {
                    return bHand == Handedness.R_HAND ? 3 : 2;
				}
				// curves
				if (metadata == 6) {
                    return bHand == Handedness.R_HAND ? 7 : 6;
				}
				if (metadata == 7) {
                    return bHand == Handedness.R_HAND ? 8 : 9;
				}
				if (metadata == 8) {
                    return bHand == Handedness.R_HAND ? 9 : 8;
				}
				if (metadata == 9) {
                    return bHand == Handedness.R_HAND ? 6 : 7;
				}
            case SOUTH:
				// flat tracks
				if (metadata == 0) {
					return 0;
				}
				if (metadata == 1) {
					return 1;
				}
				// ascending tracks
				if (metadata == 2) {
					return 3;
				}
				if (metadata == 3) {
					return 2;
				}
				if (metadata == 4) {
                    return bHand == Handedness.R_HAND ? 5 : 4;
				}
				if (metadata == 5) {
                    return bHand == Handedness.R_HAND ? 4 : 5;
				}
				// curves
				if (metadata == 6) {
                    return bHand == Handedness.R_HAND ? 8 : 7;
				}
				if (metadata == 7) {
                    return bHand == Handedness.R_HAND ? 9 : 6;
				}
				if (metadata == 8) {
                    return bHand == Handedness.R_HAND ? 6 : 9;
                }
				if (metadata == 9) {
                    return bHand == Handedness.R_HAND ? 7 : 8;
				}
            case WEST:
				// flat tracks
				if (metadata == 0) {
					return 1;
				}
				if (metadata == 1) {
					return 0;
				}
				// ascending tracks
				if (metadata == 2) {
					return 4;
				}
				if (metadata == 3) {
					return 5;
				}
				if (metadata == 4) {
                    return bHand == Handedness.R_HAND ? 3 : 2;
				}
				if (metadata == 5) {
                    return bHand == Handedness.R_HAND ? 2 : 3;
				}
				// curves
				if (metadata == 6) {
                    return bHand == Handedness.R_HAND ? 9 : 8;
				}
				if (metadata == 7) {
                    return bHand == Handedness.R_HAND ? 6 : 7;
				}
				if (metadata == 8) {
                    return bHand == Handedness.R_HAND ? 7 : 6;
				}
				if (metadata == 9) {
                    return bHand == Handedness.R_HAND ? 8 : 9;
				}
			}
        }else if(blockID==Blocks.bed||blockID==Blocks.fence_gate||blockID==Blocks.tripwire_hook||blockID==Blocks.pumpkin||blockID==Blocks.lit_pumpkin||blockID==Blocks.powered_repeater||blockID==Blocks.unpowered_repeater){
			while (metadata >= 4) {
				tempdata += 4;
                metadata -= 4;
			}
			if (blockID==Blocks.trapdoor)
                return TRAPDOOR_DIR_TO_META.get(orientDirToBDir(TRAPDOOR_META_TO_DIR[metadata])) + tempdata;
			else
                return BED_DIR_TO_META.get(orientDirToBDir(BED_META_TO_DIR[metadata])) + tempdata;
        }else if(blockID==Blocks.vine){
			if (metadata == 0)
				return 0;
			else if (metadata == 1 || metadata == 2 || metadata == 4 || metadata == 8)
                return VINES_DIR_TO_META.get(bDir.reorient(Handedness.R_HAND,
                        VINES_META_TO_DIR[metadata]));
			else
				return 1; // default case since vine do not have to have correct
			// metadata
        }else if(blockID==Blocks.standing_sign){
			// sign posts
            switch (bDir) {
            case NORTH:
				if (metadata == 0) {
                    return bHand == Handedness.R_HAND ? 0 : 8;
				}
				if (metadata == 1) {
                    return bHand == Handedness.R_HAND ? 1 : 7;
				}
				if (metadata == 2) {
                    return bHand == Handedness.R_HAND ? 2 : 6;
				}
				if (metadata == 3) {
                    return bHand == Handedness.R_HAND ? 3 : 5;
				}
				if (metadata == 4) {
					return 4;
				}
				if (metadata == 5) {
                    return bHand == Handedness.R_HAND ? 5 : 3;
				}
				if (metadata == 6) {
                    return bHand == Handedness.R_HAND ? 6 : 2;
				}
				if (metadata == 7) {
                    return bHand == Handedness.R_HAND ? 7 : 1;
				}
				if (metadata == 8) {
                    return bHand == Handedness.R_HAND ? 8 : 0;
				}
				if (metadata == 9) {
                    return bHand == Handedness.R_HAND ? 9 : 15;
                }
				if (metadata == 10) {
                    return bHand == Handedness.R_HAND ? 10 : 14;
				}
				if (metadata == 11) {
                    return bHand == Handedness.R_HAND ? 11 : 13;
				}
				if (metadata == 12) {
					return 12;
				}
				if (metadata == 13) {
                    return bHand == Handedness.R_HAND ? 13 : 11;
				}
				if (metadata == 14) {
                    return bHand == Handedness.R_HAND ? 14 : 10;
				}
				if (metadata == 15) {
                    return bHand == Handedness.R_HAND ? 15 : 9;
				}
            case EAST:
				if (metadata == 0) {
                    return bHand == Handedness.R_HAND ? 4 : 12;
				}
				if (metadata == 1) {
                    return bHand == Handedness.R_HAND ? 5 : 11;
				}
				if (metadata == 2) {
                    return bHand == Handedness.R_HAND ? 6 : 10;
				}
				if (metadata == 3) {
                    return bHand == Handedness.R_HAND ? 7 : 9;
				}
				if (metadata == 4) {
					return 8;
				}
				if (metadata == 5) {
                    return bHand == Handedness.R_HAND ? 9 : 7;
				}
				if (metadata == 6) {
                    return bHand == Handedness.R_HAND ? 10 : 6;
				}
				if (metadata == 7) {
                    return bHand == Handedness.R_HAND ? 11 : 5;
				}
				if (metadata == 8) {
                    return bHand == Handedness.R_HAND ? 12 : 4;
				}
				if (metadata == 9) {
                    return bHand == Handedness.R_HAND ? 13 : 3;
				}
				if (metadata == 10) {
                    return bHand == Handedness.R_HAND ? 14 : 2;
				}
				if (metadata == 11) {
                    return bHand == Handedness.R_HAND ? 15 : 1;
				}
				if (metadata == 12) {
					return 0;
				}
				if (metadata == 13) {
                    return bHand == Handedness.R_HAND ? 1 : 15;
                }
				if (metadata == 14) {
                    return bHand == Handedness.R_HAND ? 2 : 14;
				}
				if (metadata == 15) {
                    return bHand == Handedness.R_HAND ? 3 : 13;
				}
            case SOUTH:
				if (metadata == 0) {
                    return bHand == Handedness.R_HAND ? 8 : 0;
				}
				if (metadata == 1) {
                    return bHand == Handedness.R_HAND ? 9 : 15;
				}
				if (metadata == 2) {
                    return bHand == Handedness.R_HAND ? 10 : 14;
				}
				if (metadata == 3) {
                    return bHand == Handedness.R_HAND ? 11 : 13;
				}
				if (metadata == 4) {
					return 12;
				}
				if (metadata == 5) {
                    return bHand == Handedness.R_HAND ? 13 : 11;
				}
				if (metadata == 6) {
                    return bHand == Handedness.R_HAND ? 14 : 10;
				}
				if (metadata == 7) {
                    return bHand == Handedness.R_HAND ? 15 : 9;
				}
				if (metadata == 8) {
                    return bHand == Handedness.R_HAND ? 0 : 8;
				}
				if (metadata == 9) {
                    return bHand == Handedness.R_HAND ? 1 : 7;
				}
				if (metadata == 10) {
                    return bHand == Handedness.R_HAND ? 2 : 6;
				}
				if (metadata == 11) {
                    return bHand == Handedness.R_HAND ? 3 : 5;
				}
				if (metadata == 12) {
					return 4;
				}
				if (metadata == 13) {
                    return bHand == Handedness.R_HAND ? 5 : 3;
				}
				if (metadata == 14) {
                    return bHand == Handedness.R_HAND ? 6 : 2;
				}
				if (metadata == 15) {
                    return bHand == Handedness.R_HAND ? 7 : 1;
				}
            case WEST:
				if (metadata == 0) {
                    return bHand == Handedness.R_HAND ? 12 : 4;
				}
				if (metadata == 1) {
                    return bHand == Handedness.R_HAND ? 13 : 3;
				}
				if (metadata == 2) {
                    return bHand == Handedness.R_HAND ? 14 : 2;
				}
				if (metadata == 3) {
                    return bHand == Handedness.R_HAND ? 15 : 1;
				}
				if (metadata == 4) {
					return 0;
				}
				if (metadata == 5) {
                    return bHand == Handedness.R_HAND ? 1 : 15;
				}
				if (metadata == 6) {
                    return bHand == Handedness.R_HAND ? 2 : 14;
				}
				if (metadata == 7) {
                    return bHand == Handedness.R_HAND ? 3 : 13;
				}
				if (metadata == 8) {
                    return bHand == Handedness.R_HAND ? 4 : 12;
				}
				if (metadata == 9) {
                    return bHand == Handedness.R_HAND ? 5 : 11;
				}
				if (metadata == 10) {
                    return bHand == Handedness.R_HAND ? 6 : 10;
				}
				if (metadata == 11) {
                    return bHand == Handedness.R_HAND ? 7 : 9;
				}
				if (metadata == 12) {
					return 8;
				}
				if (metadata == 13) {
                    return bHand == Handedness.R_HAND ? 9 : 7;
				}
				if (metadata == 14) {
                    return bHand == Handedness.R_HAND ? 10 : 6;
				}
				if (metadata == 15) {
                    return bHand == Handedness.R_HAND ? 11 : 5;
				}
			}
		}
		return metadata + tempdata;
	}

	// &&&&&&&&&&&&&&&&& SPECIAL BLOCK FUNCTION - setLootChest
	// &&&&&&&&&&&&&&&&&&&&&&&&&&&&&//
	private void setLootChest(int[] pt, Block chestBlock, int meta, String chestType) {
		if (world.setBlock(pt[0], pt[1], pt[2], chestBlock, meta, 2)) {
			TileEntityChest chest = (TileEntityChest) world.getTileEntity(pt[0], pt[1], pt[2]);
            if (chestItems != null && chestItems.containsKey(chestType)) {
                for (int m = 0; m < chestItems.get(chestType).getTries(); m++) {
					if (random.nextBoolean()) {
						ItemStack itemstack = getChestItemstack(chestType);
						if (itemstack != null && chest != null)
							chest.setInventorySlotContents(random.nextInt(chest.getSizeInventory()), itemstack);
					}
				}
			}
		}
	}

	// &&&&&&&&&&&&&&&&& SPECIAL BLOCK FUNCTION - setMobSpawner
	// &&&&&&&&&&&&&&&&&&&&&&&&&&&&&//
	private void setMobSpawner(int[] pt, Block spawner, int metadata, String info) {
		if(world.setBlock(pt[0], pt[1], pt[2], spawner, metadata, 2)){
            TileEntityMobSpawner tileentitymobspawner = (TileEntityMobSpawner) world.getTileEntity(pt[0], pt[1], pt[2]);
            if (tileentitymobspawner != null){
                if(info.equals("UPRIGHT")) {
                    if (random.nextInt(3) == 0)
                        setMobSpawner(tileentitymobspawner, 1, 3);
                    else
                        setMobSpawner(tileentitymobspawner, 2, 0);
                }else if(info.equals("EASY")){
                    setMobSpawner(tileentitymobspawner, 2, 0);
                }else if(info.equals("MEDIUM")){
                    setMobSpawner(tileentitymobspawner, 3, 0);
                }else if(info.equals("HARD")){
                    setMobSpawner(tileentitymobspawner, 4, 0);
                }else
                    tileentitymobspawner.func_145881_a().setEntityName(info);
            }
        }
	}

    private void setMobSpawner(TileEntityMobSpawner spawner, int nTypes, int offset) {
        String mob = "Pig";
        int n = random.nextInt(nTypes) + offset;
        if(n<SPAWNERS.length){
            mob = SPAWNERS[n];
        }
        spawner.func_145881_a().setEntityName(mob);
    }

    private boolean isSolidBlock(Block blockID) {
		return blockID.getMaterial().isSolid();
	}
}
