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
package generatormods;

import generatormods.common.Dir;
import generatormods.common.PickWeighted;
import generatormods.common.config.ChestType;
import generatormods.walledcity.CityDataManager;

import java.util.Arrays;
import java.util.HashMap;
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

/**
 * Building is a general class for buildings. Classes can inherit from Building
 * to build from a local frame of reference.
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
    public final static int HIT_WATER = -666;
    public final static int ROT_R = 1;
    public final static int R_HAND = 1;
    public final static int L_HAND = -1;
    public final static int SEA_LEVEL = 63;
    public final static int WORLD_MAX_Y = 255;

	// **** WORKING VARIABLES ****
	protected final World world;
	protected final Random random;
	protected TemplateRule bRule; // main structural blocktype
	public int bWidth, bHeight, bLength;
	public final int bID; // Building ID number
    private final LinkedList<PlacedBlock> delayedBuildQueue;
	protected final WorldGeneratorThread wgt;
    protected final Logger logger;

	protected boolean centerAligned; // if true, alignPt x is the central axis of the building if false, alignPt is the origin
	protected int i0, j0, k0; // origin coordinates (x=0,z=0,y=0). The child class may want to move the origin as it progress to use as a "cursor" position.
	private int xI, yI, xK, yK; //
	protected int bHand; // hand of secondary axis. Takes values of 1 for right-handed, -1 for left-handed.

    /**
     * Direction code of the building's primary axis.
     */
    protected Dir bDir;

    /**
     * Special value to ignore water depth when looking for surface blocks.
     */
	public final static int IGNORE_WATER = -1;

	// Special Blocks
	public final static int PAINTING_BLOCK_OFFSET = 3;
	public final static String[] SPAWNERS = new String[]{
            "Zombie", "Skeleton", "Spider", "Creeper", "PigZombie", "Ghast", "Enderman", "CaveSpider", "Blaze", "Slime",
            "LavaSlime", "Villager", "SnowMan", "MushroomCow", "Sheep", "Cow", "Chicken", "Squid", "Wolf", "Giant",
            "Silverfish", "EnderDragon", "Ozelot", "VillagerGolem", "WitherBoss", "Bat", "Witch"
    };
	// maps block metadata to a dir
    public final static Dir[] BED_META_TO_DIR = new Dir[] { Dir.SOUTH, Dir.WEST, Dir.NORTH, Dir.EAST };
    public final static Dir[] STAIRS_META_TO_DIR = new Dir[] { Dir.EAST, Dir.WEST, Dir.SOUTH, Dir.NORTH };
    public final static Dir[] LADDER_META_TO_DIR = new Dir[] { Dir.NORTH, Dir.SOUTH, Dir.WEST, Dir.EAST };
    public final static Dir[] TRAPDOOR_META_TO_DIR = new Dir[] { Dir.SOUTH, Dir.NORTH, Dir.EAST, Dir.WEST };
    // the bit is the direction, so metadatas 1, 2, 4, and 8 are the directions.
    public final static Dir[] VINES_META_TO_DIR = new Dir[] { (Dir.NORTH), Dir.SOUTH, Dir.WEST, (Dir.NORTH), Dir.NORTH, (Dir.NORTH), (Dir.NORTH), (Dir.NORTH), Dir.EAST };
    public final static Dir[] DOOR_META_TO_DIR = new Dir[] { Dir.WEST, Dir.NORTH, Dir.EAST, Dir.SOUTH };

    // Minecraft Directions
	public final static Map<Dir, Integer> BED_DIR_TO_META = new HashMap<Dir, Integer>();
    static {
        BED_DIR_TO_META.put(Dir.SOUTH, 0);
        BED_DIR_TO_META.put(Dir.WEST, 1);
        BED_DIR_TO_META.put(Dir.NORTH, 2);
        BED_DIR_TO_META.put(Dir.EAST, 3);
    }

    /**
     * Maps Directions to the button metadata. The "direction" here refers to
     * which facing/side of the block the button is attached to. Button
     * metadata meanings (from <a
     * href="http://minecraft.gamepedia.com/Data_values#Buttons">Minecraft
     * Wiki</a>):
     *
     * <table>
     * <tr><th>Value</th><th>Meaning</th>                               </tr>
     * <tr><td>0x0</td>  <td>Facing down (attached to ceiling)</td>     </tr>
     * <tr><td>0x1</td>  <td>Facing East (attached to west block)</td>  </tr>
     * <tr><td>0x2</td>  <td>Facing West (attached to east block)</td>  </tr>
     * <tr><td>0x3</td>  <td>Facing South (attached to north block)</td></tr>
     * <tr><td>0x4</td>  <td>Facing North (attached to south block)</td></tr>
     * <tr><td>0x5</td>  <td>Facing up (attached to block below)</td>   </tr>
     * <tr><td>+0x8 </td><td>Button is pressed</td>                     </tr>
     * </table>
     */
    public final static Map<Dir, Integer> BUTTON_DIR_TO_META = new HashMap<Dir, Integer>();
    static {
        // facing down is 0
        BUTTON_DIR_TO_META.put(Dir.EAST, 1);
        BUTTON_DIR_TO_META.put(Dir.WEST, 2);
        BUTTON_DIR_TO_META.put(Dir.SOUTH, 3);
        BUTTON_DIR_TO_META.put(Dir.NORTH, 4);
        // facing up is 5
    }

    /**
     * Maps directions to Stair metadata. Stair metadata meanings (from <a
     * href="http://minecraft.gamepedia.com/Data_values#Buttons">Minecraft
     * Wiki</a>):
     *
     * <table>
     * <tr><th>Value</th><th>Meaning</th>                           </tr>
     * <tr><td>0x0</td>  <td>Full block side facing East</td>       </tr>
     * <tr><td>0x1</td>  <td>Full block side facing West</td>       </tr>
     * <tr><td>0x2</td>  <td>Full block side facing South</td>      </tr>
     * <tr><td>0x3</td>  <td>Full block side facing North</td>      </tr>
     * <tr><td>+0x4</td> <td>Whether the stairs are upside down</td></tr>
     * </table>
     */
    public final static Map<Dir, Integer> STAIRS_DIR_TO_META = new HashMap<Dir, Integer>();
    static {
        STAIRS_DIR_TO_META.put(Dir.NORTH, 3);
        STAIRS_DIR_TO_META.put(Dir.EAST, 0);
        STAIRS_DIR_TO_META.put(Dir.SOUTH, 2);
        STAIRS_DIR_TO_META.put(Dir.WEST, 1);
    }

    /**
     * Maps directions to Ladder metadata. Ladder metadata values are based
     * upon Facing values, instead of Direction values. The "direction" here
     * specifies which side of the block it is attached to. Ladder metadata
     * meanings (from <a
     * href="http://minecraft.gamepedia.com/Data_values#Ladders.2C_Wall_Signs.2C_Furnaces.2C_Chests.2C_and_Wall_Banners">Minecraft
     * Wiki</a>):
     * 
     * <table>
     * <tr><th>Value</th><th>Meaning</th>                              </tr>
     * <tr><td>0x2</td>  <td>Attached to the North side of a block</td></tr>
     * <tr><td>0x3</td>  <td>Attached to the South side of a block</td></tr>
     * <tr><td>0x4</td>  <td>Attached to the West side of a block</td> </tr>
     * <tr><td>0x5</td>  <td>Attached to the East side of a block</td> </tr>
     * </table>
     */
    public final static Map<Dir, Integer> LADDER_DIR_TO_META = new HashMap<Dir, Integer>();
    static {
        LADDER_DIR_TO_META.put(Dir.NORTH, 2);
        LADDER_DIR_TO_META.put(Dir.SOUTH, 3);
        LADDER_DIR_TO_META.put(Dir.WEST, 4);
        LADDER_DIR_TO_META.put(Dir.EAST, 5);
    }

    /**
     * Maps Directions to trapdoor metadata. Trapdoor metadata determines which
     * wall the trapdoor is attached to. Trapdoor metadata meanings (from <a
     * href="http://minecraft.gamepedia.com/Data_values#Trapdoors">Minecraft
     * Wiki</a>):
     *
     * <table>
     * <tr><th>Value</th><th>Meaning</th>                   </tr>
     * <tr><td>0x0</td>  <td>Attached to the South wall</td></tr>
     * <tr><td>0x1</td>  <td>Attached to the North wall</td></tr>
     * <tr><td>0x2</td>  <td>Attached to the East wall</td> </tr>
     * <tr><td>0x3</td>  <td>Attached to the West wall</td> </tr>
     * <tr><td>+0x4</td> <td>Whether the trapdoor is swung open</td> </tr>
     * <tr><td>+0x8</td> <td>Whether it is attached to the bottom or top of the block</td> </tr>
     * </table>
     */
    public final static Map<Dir, Integer> TRAPDOOR_DIR_TO_META = new HashMap<Dir, Integer>();
    static {
        TRAPDOOR_DIR_TO_META.put(Dir.SOUTH, 0);
        TRAPDOOR_DIR_TO_META.put(Dir.NORTH, 1);
        TRAPDOOR_DIR_TO_META.put(Dir.EAST, 2);
        TRAPDOOR_DIR_TO_META.put(Dir.WEST, 3);
    }

    /**
     * Maps directions to vine metadata. The directions refer to which face the
     * vine is anchored. Possible values (from <a
     * href="http://minecraft.gamepedia.com/Data_values#Vines">Minecraft
     * Wiki</a>):
     *
     * <table>
     * <tr><th>Value</th><th>Meaning</th>              </tr>
     * <tr><td>0x1</td>  <td>Attached to the south</td></tr>
     * <tr><td>0x2</td>  <td>Attached to the west</td> </tr>
     * <tr><td>0x4</td>  <td>Attached to the north</td></tr>
     * <tr><td>0x8</td>  <td>Attached to the east</td> </tr>
     * </table>
     */
    public final static Map<Dir, Integer> VINES_DIR_TO_META = new HashMap<Dir, Integer>();
    static {
        VINES_DIR_TO_META.put(Dir.NORTH, 4);
        VINES_DIR_TO_META.put(Dir.EAST, 8);
        VINES_DIR_TO_META.put(Dir.SOUTH, 1);
        VINES_DIR_TO_META.put(Dir.WEST, 2);
    }

    /**
     * Maps directions to the possible directions a door could face while
     * closed. Note that the directions here are interpreted as the opposite to
     * how the <a
     * href="http://minecraft.gamepedia.com/Data_values#Door">Minecraft
     * Wiki</a> interpretes which direction a door faces.
     */
    public final static Map<Dir, Integer> DOOR_DIR_TO_META = new HashMap<Dir, Integer>();
    static {
        DOOR_DIR_TO_META.put(Dir.EAST, 0);
        DOOR_DIR_TO_META.put(Dir.SOUTH, 1);
        DOOR_DIR_TO_META.put(Dir.WEST, 2);
        DOOR_DIR_TO_META.put(Dir.NORTH, 3);
    }

    /**
     * Maps directions to the direction a painting is facing. Paintings are
     * tile entities and not blocks.
     */
	public final static Map<Dir, Integer> PAINTING_DIR_TO_FACEDIR = new HashMap<Dir, Integer>();
    static {
        PAINTING_DIR_TO_FACEDIR.put(Dir.NORTH, 0);
        PAINTING_DIR_TO_FACEDIR.put(Dir.WEST, 1);
        PAINTING_DIR_TO_FACEDIR.put(Dir.SOUTH, 2);
        PAINTING_DIR_TO_FACEDIR.put(Dir.EAST, 3);
    }

	// some prebuilt directional blocks
    public final static BlockAndMeta WEST_FACE_TORCH_BLOCK = new BlockAndMeta(Blocks.torch, BUTTON_DIR_TO_META.get(Dir.WEST));
    public final static BlockAndMeta EAST_FACE_TORCH_BLOCK = new BlockAndMeta(Blocks.torch, BUTTON_DIR_TO_META.get(Dir.EAST));
    public final static BlockAndMeta NORTH_FACE_TORCH_BLOCK = new BlockAndMeta(Blocks.torch, BUTTON_DIR_TO_META.get(Dir.NORTH));
    public final static BlockAndMeta SOUTH_FACE_TORCH_BLOCK = new BlockAndMeta(Blocks.torch, BUTTON_DIR_TO_META.get(Dir.SOUTH));
    public final static BlockAndMeta EAST_FACE_LADDER_BLOCK = new BlockAndMeta(Blocks.ladder, LADDER_DIR_TO_META.get(Dir.EAST));
    public final static BlockAndMeta HOLE_BLOCK_LIGHTING = new BlockAndMeta(Blocks.air, 0);
    public final static BlockAndMeta HOLE_BLOCK_NO_LIGHTING = new BlockAndMeta(Blocks.air, 1);
    public final static BlockAndMeta PRESERVE_BLOCK = new BlockExtended(Blocks.air, 0, "PRESERVE");
    public final static BlockAndMeta TOWER_CHEST_BLOCK = new BlockExtended(Blocks.chest, 0, ChestType.TOWER.toString());
    public final static BlockAndMeta HARD_CHEST_BLOCK = new BlockExtended(Blocks.chest, 0, ChestType.HARD.toString());
    public final static BlockAndMeta GHAST_SPAWNER = new BlockExtended(Blocks.mob_spawner, 0, "Ghast");

	private final static int LIGHTING_INVERSE_DENSITY = 10;
	private final static boolean[] randLightingHash = new boolean[512];
	static {
		Random rand = new Random();
		for (int m = 0; m < randLightingHash.length; m++)
			randLightingHash[m] = rand.nextInt(LIGHTING_INVERSE_DENSITY) == 0;
	}

    public Building(int ID_, WorldGeneratorThread wgt_, TemplateRule buildingRule_, Dir dir_, int axXHand_, boolean centerAligned_, int[] dim, int[] alignPt) {
        bID = ID_;
        wgt = wgt_;
        logger = wgt_.logger;
        world = wgt.world;
        bRule = buildingRule_;
        if (bRule == null)
            bRule = TemplateRule.STONE_RULE;
        bWidth = dim[0];
        bHeight = dim[1];
        bLength = dim[2];
        random = wgt.random;
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

	// ******************** LOCAL COORDINATE FUNCTIONS - ACCESSORS
	// *************************************************************************************************************//
	// Use these instead of World.java functions when to build from a local
	// reference frame
	// when i0,j0,k0 are set to working values.
	public final int getI(int x, int y) {
		return i0 + yI * y + xI * x;
	}

	public final int[] getIJKPt(int x, int z, int y) {
		int[] pt = new int[3];
		pt[0] = i0 + yI * y + xI * x;
		pt[1] = j0 + z;
		pt[2] = k0 + yK * y + xK * x;
		return pt;
	}

	public final int getJ(int z) {
		return j0 + z;
	}

	public final int getK(int x, int y) {
		return k0 + yK * y + xK * x;
	}

	public final int[] getSurfaceIJKPt(int x, int y, int j, boolean wallIsSurface, int waterSurfaceBuffer) {
		int[] pt = getIJKPt(x, 0, y);
		pt[1] = findSurfaceJ(world, pt[0], pt[2], j, wallIsSurface, waterSurfaceBuffer);
		return pt;
	}

	public final int getX(int[] pt) {
		return xI * (pt[0] - i0) + xK * (pt[2] - k0);
	}

	public final int getY(int[] pt) {
		return yI * (pt[0] - i0) + yK * (pt[2] - k0);
	}

	public final int getZ(int[] pt) {
		return pt[1] - j0;
	}

	public final String localCoordString(int x, int z, int y) {
		int[] pt = getIJKPt(x, z, y);
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
        return bDir.reorient(bHand > 0, dir);
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

	// ******************** ORIENTATION FUNCTIONS
	// *************************************************************************************************************//
    public void setPrimaryAx(Dir dir_) {
		bDir = dir_;
		// changes of basis
        switch (bDir) {
        case NORTH:
			xI = bHand;
			yI = 0;
			xK = 0;
			yK = -1;
			break;
        case EAST:
			xI = 0;
			yI = 1;
			xK = bHand;
			yK = 0;
			break;
        case SOUTH:
			xI = -bHand;
			yI = 0;
			xK = 0;
			yK = 1;
			break;
        case WEST:
			xI = 0;
			yI = -1;
			xK = -bHand;
			yK = 0;
			break;
		}
	}

	// &&&&&&&&&&&&&&&&& SPECIAL BLOCK FUNCTION - setSignOrPost
	// &&&&&&&&&&&&&&&&&&&&&&&&&&&&&//
	public void setSignOrPost(int x2, int z2, int y2, boolean post, int sDir, String[] lines) {
		int[] pt = getIJKPt(x2, z2, y2);
		world.setBlock(pt[0], pt[1], pt[2], post ? Blocks.standing_sign : Blocks.wall_sign, sDir, 2);
		TileEntitySign tileentitysign = (TileEntitySign) world.getTileEntity(pt[0], pt[1], pt[2]);
		if (tileentitysign == null)
			return;
        System.arraycopy(lines, 0, tileentitysign.signText, 0, Math.min(lines.length, 4));
	}

	// call with z=start of builDown, will buildDown a maximum of maxDepth
	// blocks + foundationDepth.
	// if buildDown column is completely air, instead buildDown reserveDepth
	// blocks.
	public void buildDown(int x, int z, int y, TemplateRule buildRule, int maxDepth, int foundationDepth, int reserveDepth) {
		int stopZ;
		for (stopZ = z; stopZ > z - maxDepth; stopZ--) {
			if (!isWallable(x, stopZ, y))
				break; // find ground height
		}
		if (stopZ == z - maxDepth && isWallable(x, z - maxDepth, y)) // if we never hit ground
			stopZ = z - reserveDepth;
		else
			stopZ -= foundationDepth;
		for (int z1 = z; z1 > stopZ; z1--) {
			setBlockWithLightingLocal(x, z1, y, buildRule, false);
		}
	}

	protected final Block getBlockIdLocal(int x, int z, int y) {
		return world.getBlock(i0 + yI * y + xI * x, j0 + z, k0 + yK * y + xK * x);
	}

	protected final int getBlockMetadataLocal(int x, int z, int y) {
		return world.getBlockMetadata(i0 + yI * y + xI * x, j0 + z, k0 + yK * y + xK * x);
	}

    /**
     * Describes the building in terms of its building ID and orientation.
     *
     * @return A string describing the building. The ID is the building ID, and
     * the axes describe the building's local "North" and handedness.
     */
	protected final String IDString() {
        String str = this.getClass().toString() + "<ID="+ bID + " axes(Y,X)=";
		switch (bDir) {
            case SOUTH:
                return str + "(S," + (bHand > 0 ? "W" : "E") + ")>";
            case NORTH:
                return str + "(N," + (bHand > 0 ? "E" : "W") + ")>";
            case WEST:
                return str + "(W," + (bHand > 0 ? "N" : "S") + ")>";
            case EAST:
                return str + "(E," + (bHand > 0 ? "S" : "N") + ")>";
            default:
                return str + "(bad dir value: " + bDir + ")>";
		}
	}

	protected final boolean isArtificialWallBlock(int x, int z, int y) {
		Block blockId = getBlockIdLocal(x, z, y);
		return BlockProperties.get(blockId).isArtificial && !(blockId == Blocks.sandstone && (getBlockIdLocal(x, z + 1, y) == Blocks.sand || getBlockIdLocal(x, z + 2, y) == Blocks.sand));
	}

	protected final boolean isDoorway(int x, int z, int y) {
		return isFloor(x, z, y) && (isWallBlock(x + 1, z, y) && isWallBlock(x - 1, z, y) || isWallBlock(x, z, y + 1) && isWallBlock(x - 1, z, y - 1));
	}

	protected final boolean hasNoDoorway(int x, int z, int y) {
		return !(isDoorway(x - 1, z, y) || isDoorway(x + 1, z, y) || isDoorway(x, z, y - 1) || isDoorway(x - 1, z, y + 1));
	}

	protected boolean isObstructedFrame(int zstart, int ybuffer) {
		for (int z1 = zstart; z1 < bHeight; z1++) {
			// for(int x1=0; x1<length; x1++) for(int y1=ybuffer;
			// y1<width-1;y1++)
			// if(isWallBlock(x1,z1,y1))
			// return true;
			for (int x1 = 0; x1 < bWidth; x1++) {
				if (isArtificialWallBlock(x1, z1, bLength - 1)) {
					return true;
				}
			}
			for (int y1 = ybuffer; y1 < bLength - 1; y1++) {
				if (isArtificialWallBlock(0, z1, y1)) {
					return true;
				}
				if (isArtificialWallBlock(bWidth - 1, z1, y1)) {
					return true;
				}
			}
		}
		return false;
	}

	protected boolean isObstructedSolid(int pt1[], int pt2[]) {
		for (int x1 = pt1[0]; x1 <= pt2[0]; x1++) {
			for (int z1 = pt1[1]; z1 <= pt2[1]; z1++) {
				for (int y1 = pt1[2]; y1 <= pt2[2]; y1++) {
					if (!isWallable(x1, z1, y1)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	// ******************** LOCAL COORDINATE FUNCTIONS - BLOCK TEST FUNCTIONS
	// *************************************************************************************************************//
    // true if block is air, block below is wall block
    protected final boolean isFloor(int x, int z, int y) {
        Block blkId1 = getBlockIdLocal(x, z, y), blkId2 = getBlockIdLocal(x, z - 1, y);
        // return ((blkId1==0 || blkId1==STEP_ID) && IS_WALL_BLOCK[blkId2] &&
        // blkId2!=LADDER_ID);
        return blkId1 == Blocks.air && BlockProperties.get(blkId2).isArtificial && blkId2 != Blocks.ladder;
    }

    protected final boolean isStairBlock(int x, int z, int y) {
        Block blkId = getBlockIdLocal(x, z, y);
        return blkId == Blocks.stone_slab || BlockProperties.get(blkId).isStair;
    }

	protected final boolean isWallable(int x, int z, int y) {
		return BlockProperties.get(world.getBlock(i0 + yI * y + xI * x, j0 + z, k0 + yK * y + xK * x)).isWallable;
	}

	protected final boolean isWallableIJK(int pt[]) {
		return pt!=null && BlockProperties.get(world.getBlock(pt[0], pt[1], pt[2])).isWallable;
	}

	protected final boolean isWallBlock(int x, int z, int y) {
		return BlockProperties.get(world.getBlock(i0 + yI * y + xI * x, j0 + z, k0 + yK * y + xK * x)).isArtificial;
	}

    public final void flushDelayed(){
        while(delayedBuildQueue.size()>0){
            PlacedBlock block = delayedBuildQueue.poll();
            setDelayed(block.get(), block.x, block.y, block.z, block.getMeta());
        }
    }

	protected void setDelayed(Block blc, int...block) {
		if (BlockProperties.get(blc).isStair) {
            BlockAndMeta temp = getDelayedStair(blc, block);
            blc = temp.get();
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
                setBlockAndMetaNoLighting(world, block[0], block[1], block[2], blc, block[3]);
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

	// The origin of this building was placed to match a centerline.
	// The building previously had bWidth=oldWidth, now it has the current
	// value of bWidth and needs to have origin updated.
	protected final void recenterFromOldWidth(int oldWidth) {
		i0 += xI * (oldWidth - bWidth) / 2;
		k0 += xK * (oldWidth - bWidth) / 2;
	}

	// ******************** LOCAL COORDINATE FUNCTIONS - SET BLOCK FUNCTIONS
	// *************************************************************************************************************//
	protected final void setBlockLocal(int x, int z, int y, Block blockID) {
		setBlockLocal(x, z, y, blockID, 0);
	}

	protected final void setBlockLocal(int x, int z, int y, Block blockID, int metadata) {
        int[] pt = getIJKPt(x, z, y);
        if (blockID == Blocks.air && world.isAirBlock(pt[0], pt[1], pt[2]))
            return;
        if (!(blockID instanceof BlockChest))
            emptyIfChest(pt);
        if (BlockProperties.get(blockID).isDelayed){
            delayedBuildQueue.offer(new PlacedBlock(blockID, new int[]{pt[0], pt[1], pt[2], rotateMetadata(blockID, metadata)}));
        }else if (randLightingHash[(x & 0x7) | (y & 0x38) | (z & 0x1c0)]) {
            world.setBlock(pt[0], pt[1], pt[2], blockID, rotateMetadata(blockID, metadata), 2);
        } else {
            setBlockAndMetaNoLighting(world, pt[0], pt[1], pt[2], blockID, rotateMetadata(blockID, metadata));
        }
        if (BlockProperties.get(blockID).isDoor) {
            addDoorToNewListIfAppropriate(pt[0], pt[1], pt[2]);
        }
	}

	protected final void setBlockLocal(int x, int z, int y, BlockAndMeta block) {
        if(block instanceof BlockExtended){
            setSpecialBlockLocal(x, z, y, block.get(), block.getMeta(), ((BlockExtended) block).info);
        }else{
            setBlockLocal(x, z, y, block.get(), block.getMeta());
        }
	}

	protected final void setBlockLocal(int x, int z, int y, TemplateRule rule) {
		setBlockLocal(x, z, y, rule.getBlockOrHole(random));
	}

    protected final void setBlockWithLightingLocal(int x, int z, int y, TemplateRule rule, boolean lighting) {
        setBlockWithLightingLocal(x, z, y, rule.getBlockOrHole(random), lighting);
    }

    protected final void removeBlockWithLighting(int x, int z, int y){
        setBlockWithLightingLocal(x, z, y, TemplateRule.AIR_RULE, true);
    }

    protected final void setBlockWithLightingLocal(int x, int z, int y, BlockAndMeta block, boolean lighting) {
        if(block instanceof BlockExtended){
            setSpecialBlockLocal(x, z, y, block.get(), block.getMeta(), ((BlockExtended) block).info);
        }else{
            setBlockWithLightingLocal(x, z, y, block.get(), block.getMeta(), lighting);
        }
    }

	// allows control of lighting. Also will build even if replacing air with air.
	protected final void setBlockWithLightingLocal(int x, int z, int y, Block blockID, int metadata, boolean lighting) {
		int[] pt = getIJKPt(x, z, y);
        if (blockID == Blocks.air && world.isAirBlock(pt[0], pt[1], pt[2]))
            return;
		if (!(blockID instanceof BlockChest))
			emptyIfChest(pt);
		if (BlockProperties.get(blockID).isDelayed)
            delayedBuildQueue.offer(new PlacedBlock(blockID, new int[]{pt[0], pt[1], pt[2], rotateMetadata(blockID, metadata)}));
		else if (lighting)
			world.setBlock(pt[0], pt[1], pt[2], blockID, rotateMetadata(blockID, metadata), 3);
		else
			setBlockAndMetaNoLighting(world, pt[0], pt[1], pt[2], blockID, rotateMetadata(blockID, metadata));
		if (BlockProperties.get(blockID).isDoor) {
			addDoorToNewListIfAppropriate(pt[0], pt[1], pt[2]);
		}
	}

	protected final void setOrigin(int i0_, int j0_, int k0_) {
		i0 = i0_;
		j0 = j0_;
		k0 = k0_;
	}

	protected final void setOriginLocal(int i1, int j1, int k1, int x, int z, int y) {
		i0 = i1 + yI * y + xI * x;
		j0 = j1 + z;
		k0 = k1 + yK * y + xK * x;
	}

	// ******************** LOCAL COORDINATE FUNCTIONS - SPECIAL BLOCK FUNCTIONS
	// *************************************************************************************************************//
	// &&&&&&&&&&&&&&&&& SPECIAL BLOCK FUNCTION - setSpecialBlockLocal &&&&&&&&&&&&&&&&&&&&&&&&&&&&&//
	protected final void setSpecialBlockLocal(int x, int z, int y, Block blockID, int metadata, String extra) {
		if (extra.equals(TemplateRule.SPECIAL_AIR))
			return; // preserve existing world block
		int[] pt = getIJKPt(x, z, y);
		if (blockID instanceof BlockAir) {
            if(extra.equals(TemplateRule.SPECIAL_STAIR) && metadata<=0){
                world.setBlock(pt[0], pt[1], pt[2], Blocks.stone_slab, rotateMetadata(Blocks.stone_slab, -metadata), 2);
                return;
            }
            if (extra.equals(TemplateRule.SPECIAL_PAINT) && metadata>=PAINTING_BLOCK_OFFSET) {//Remember:Paintings are not blocks
                delayedBuildQueue.offer(new PlacedBlock(blockID, new int[]{pt[0], pt[1], pt[2], metadata}));
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
		if (!(this.wgt instanceof WorldGenWalledCity)) {
			return;
		}
        CityDataManager cityDataManager = ((WorldGenWalledCity) this.wgt).cityDataManager;
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
			return new ItemStack(bRule.primaryBlock.get(), random.nextInt(10), bRule.primaryBlock.getMeta());
		}
		Object[][] itempool = wgt.chestItems.get(chestType).getChestItemsObjectArray();
		int idx = PickWeighted.pickWeightedOption(world.rand, Arrays.asList(itempool[3]), Arrays.asList(itempool[0]));
        Object obj = itempool[1][idx];
        if(obj == null){
            return null;
        }
        if(obj instanceof Block){
            if(obj == Blocks.air)
                return null;
            obj = Item.getItemFromBlock((Block)obj);
        }
		return new ItemStack((Item)obj, Integer.class.cast(itempool[4][idx]) + random.nextInt(Integer.class.cast(itempool[5][idx]) - Integer.class.cast(itempool[4][idx]) + 1), Integer.class.cast(itempool[2][idx]));
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
					return bHand == 1 ? 4 : 5;
				}
				if (metadata == 5) {
					return bHand == 1 ? 5 : 4;
				}
				// curves
				if (metadata == 6) {
					return bHand == 1 ? 6 : 9;
				}
				if (metadata == 7) {
					return bHand == 1 ? 7 : 8;
				}
				if (metadata == 8) {
					return bHand == 1 ? 8 : 7;
				}
				if (metadata == 9) {
					return bHand == 1 ? 9 : 6;
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
					return bHand == 1 ? 2 : 3;
				}
				if (metadata == 5) {
					return bHand == 1 ? 3 : 2;
				}
				// curves
				if (metadata == 6) {
					return bHand == 1 ? 7 : 6;
				}
				if (metadata == 7) {
					return bHand == 1 ? 8 : 9;
				}
				if (metadata == 8) {
					return bHand == 1 ? 9 : 8;
				}
				if (metadata == 9) {
					return bHand == 1 ? 6 : 7;
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
					return bHand == 1 ? 5 : 4;
				}
				if (metadata == 5) {
					return bHand == 1 ? 4 : 5;
				}
				// curves
				if (metadata == 6) {
					return bHand == 1 ? 8 : 7;
				}
				if (metadata == 7) {
					return bHand == 1 ? 9 : 6;
				}
				if (metadata == 8) {
					return bHand == 1 ? 6 : 9;
				}
				if (metadata == 9) {
					return bHand == 1 ? 7 : 8;
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
					return bHand == 1 ? 3 : 2;
				}
				if (metadata == 5) {
					return bHand == 1 ? 2 : 3;
				}
				// curves
				if (metadata == 6) {
					return bHand == 1 ? 9 : 8;
				}
				if (metadata == 7) {
					return bHand == 1 ? 6 : 7;
				}
				if (metadata == 8) {
					return bHand == 1 ? 7 : 6;
				}
				if (metadata == 9) {
					return bHand == 1 ? 8 : 9;
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
                return VINES_DIR_TO_META.get(bDir.reorient(true, VINES_META_TO_DIR[metadata]));
			else
				return 1; // default case since vine do not have to have correct
			// metadata
        }else if(blockID==Blocks.standing_sign){
			// sign posts
            switch (bDir) {
            case NORTH:
				if (metadata == 0) {
					return bHand == 1 ? 0 : 8;
				}
				if (metadata == 1) {
					return bHand == 1 ? 1 : 7;
				}
				if (metadata == 2) {
					return bHand == 1 ? 2 : 6;
				}
				if (metadata == 3) {
					return bHand == 1 ? 3 : 5;
				}
				if (metadata == 4) {
					return 4;
				}
				if (metadata == 5) {
					return bHand == 1 ? 5 : 3;
				}
				if (metadata == 6) {
					return bHand == 1 ? 6 : 2;
				}
				if (metadata == 7) {
					return bHand == 1 ? 7 : 1;
				}
				if (metadata == 8) {
					return bHand == 1 ? 8 : 0;
				}
				if (metadata == 9) {
					return bHand == 1 ? 9 : 15;
				}
				if (metadata == 10) {
					return bHand == 1 ? 10 : 14;
				}
				if (metadata == 11) {
					return bHand == 1 ? 11 : 13;
				}
				if (metadata == 12) {
					return 12;
				}
				if (metadata == 13) {
					return bHand == 1 ? 13 : 11;
				}
				if (metadata == 14) {
					return bHand == 1 ? 14 : 10;
				}
				if (metadata == 15) {
					return bHand == 1 ? 15 : 9;
				}
            case EAST:
				if (metadata == 0) {
					return bHand == 1 ? 4 : 12;
				}
				if (metadata == 1) {
					return bHand == 1 ? 5 : 11;
				}
				if (metadata == 2) {
					return bHand == 1 ? 6 : 10;
				}
				if (metadata == 3) {
					return bHand == 1 ? 7 : 9;
				}
				if (metadata == 4) {
					return 8;
				}
				if (metadata == 5) {
					return bHand == 1 ? 9 : 7;
				}
				if (metadata == 6) {
					return bHand == 1 ? 10 : 6;
				}
				if (metadata == 7) {
					return bHand == 1 ? 11 : 5;
				}
				if (metadata == 8) {
					return bHand == 1 ? 12 : 4;
				}
				if (metadata == 9) {
					return bHand == 1 ? 13 : 3;
				}
				if (metadata == 10) {
					return bHand == 1 ? 14 : 2;
				}
				if (metadata == 11) {
					return bHand == 1 ? 15 : 1;
				}
				if (metadata == 12) {
					return 0;
				}
				if (metadata == 13) {
					return bHand == 1 ? 1 : 15;
				}
				if (metadata == 14) {
					return bHand == 1 ? 2 : 14;
				}
				if (metadata == 15) {
					return bHand == 1 ? 3 : 13;
				}
            case SOUTH:
				if (metadata == 0) {
					return bHand == 1 ? 8 : 0;
				}
				if (metadata == 1) {
					return bHand == 1 ? 9 : 15;
				}
				if (metadata == 2) {
					return bHand == 1 ? 10 : 14;
				}
				if (metadata == 3) {
					return bHand == 1 ? 11 : 13;
				}
				if (metadata == 4) {
					return 12;
				}
				if (metadata == 5) {
					return bHand == 1 ? 13 : 11;
				}
				if (metadata == 6) {
					return bHand == 1 ? 14 : 10;
				}
				if (metadata == 7) {
					return bHand == 1 ? 15 : 9;
				}
				if (metadata == 8) {
					return bHand == 1 ? 0 : 8;
				}
				if (metadata == 9) {
					return bHand == 1 ? 1 : 7;
				}
				if (metadata == 10) {
					return bHand == 1 ? 2 : 6;
				}
				if (metadata == 11) {
					return bHand == 1 ? 3 : 5;
				}
				if (metadata == 12) {
					return 4;
				}
				if (metadata == 13) {
					return bHand == 1 ? 5 : 3;
				}
				if (metadata == 14) {
					return bHand == 1 ? 6 : 2;
				}
				if (metadata == 15) {
					return bHand == 1 ? 7 : 1;
				}
            case WEST:
				if (metadata == 0) {
					return bHand == 1 ? 12 : 4;
				}
				if (metadata == 1) {
					return bHand == 1 ? 13 : 3;
				}
				if (metadata == 2) {
					return bHand == 1 ? 14 : 2;
				}
				if (metadata == 3) {
					return bHand == 1 ? 15 : 1;
				}
				if (metadata == 4) {
					return 0;
				}
				if (metadata == 5) {
					return bHand == 1 ? 1 : 15;
				}
				if (metadata == 6) {
					return bHand == 1 ? 2 : 14;
				}
				if (metadata == 7) {
					return bHand == 1 ? 3 : 13;
				}
				if (metadata == 8) {
					return bHand == 1 ? 4 : 12;
				}
				if (metadata == 9) {
					return bHand == 1 ? 5 : 11;
				}
				if (metadata == 10) {
					return bHand == 1 ? 6 : 10;
				}
				if (metadata == 11) {
					return bHand == 1 ? 7 : 9;
				}
				if (metadata == 12) {
					return 8;
				}
				if (metadata == 13) {
					return bHand == 1 ? 9 : 7;
				}
				if (metadata == 14) {
					return bHand == 1 ? 10 : 6;
				}
				if (metadata == 15) {
					return bHand == 1 ? 11 : 5;
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
			if (wgt.chestItems != null && wgt.chestItems.containsKey(chestType)) {
				for (int m = 0; m < wgt.chestItems.get(chestType).getTries(); m++) {
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

	public static int distance(int[] pt1, int[] pt2) {
		return (int) Math.sqrt((pt1[0] - pt2[0]) * (pt1[0] - pt2[0]) + (pt1[1] - pt2[1]) * (pt1[1] - pt2[1]) + (pt1[2] - pt2[2]) * (pt1[2] - pt2[2]));
	}

    /**
     * Finds a surface block. Depending on the value of waterIsSurface and
     * wallIsSurface will treat liquid and wall blocks as either solid or air.
     *
     * @param world The world we are checking
     * @param i X-coordinate of world to check
     * @param k Z-coordinate of world to check
     * @param jinit Initial Y-coordinate to begin checking down from to find the surface Y value.
     * @param wallIsSurface ???
     * @param waterSurfaceBuffer The number of vertical blocks of water we permit.
     * @return The Y coordinate of the surface. However, if there is more water
     * than waterSurfaceBuffer, then it will return HIT_WATER instead.
     */
    public static int findSurfaceJ(World world, int i, int k, int jinit, boolean wallIsSurface, int waterSurfaceBuffer) {
		Block blockId;
		//if(world.getChunkProvider().chunkExists(i>>4, k>>4))
		{
			if (world.provider.isHellWorld) {// the Nether
				if ((i % 2 == 1) ^ (k % 2 == 1)) {
					for (int j = (int) (WORLD_MAX_Y * 0.5); j > -1; j--) {
						if (world.isAirBlock(i, j, k))
							for (; j > -1; j--)
								if (!BlockProperties.get(world.getBlock(i, j, k)).isWallable)
									return j;
					}
				} else {
					for (int j = 0; j <= (int) (WORLD_MAX_Y * 0.5); j++)
						if (world.isAirBlock(i, j, k))
							return j;
				}
				return -1;
			} else { // other dimensions
				int minecraftHeight = world.getChunkFromBlockCoords(i, k).getHeightValue(i & 0xf, k & 0xf);
				if (minecraftHeight < jinit)
					jinit = minecraftHeight;
				for (int j = jinit; j >= 0; j--) {
					blockId = world.getBlock(i, j, k);
					if (!BlockProperties.get(blockId).isWallable && (wallIsSurface || !BlockProperties.get(blockId).isArtificial))
						return j;
					if (waterSurfaceBuffer != IGNORE_WATER && BlockProperties.get(blockId).isWater)
						return BlockProperties.get(world.getBlock(i, j - waterSurfaceBuffer, k)).isWater ? HIT_WATER : j;
					// so we can still build in swamps...
				}
			}
		}
		return -1;
	}

	public static String metaValueCheck(Block blockID, int metadata) {
		if (metadata < 0 || metadata >= 16)
			return "All Minecraft meta values should be between 0 and 15";
		String fail = blockID.getUnlocalizedName() + " meta value should be between";
		if (BlockProperties.get(blockID).isStair)
			return metadata < 8 ? null : fail + " 0 and 7";
		// orientation metas
		if(blockID==Blocks.rail){
			return metadata < 10 ? null : fail + " 0 and 9";
        }else if(blockID==Blocks.stone_button || blockID== Blocks.wooden_button){
			return metadata % 8 > 0 && metadata % 8 < 5 ? null : fail + " 1 and 4 or 9 and 12";
        }else if(blockID==Blocks.ladder||blockID==Blocks.dispenser||blockID==Blocks.furnace||blockID==Blocks.lit_furnace||blockID==Blocks.wall_sign
		||blockID==Blocks.piston||blockID==Blocks.piston_extension||blockID==Blocks.chest||blockID==Blocks.hopper||blockID==Blocks.dropper||blockID==Blocks.golden_rail||blockID==Blocks.detector_rail||blockID==Blocks.activator_rail){
			return metadata % 8 < 6 ? null : fail + " 0 and 5 or 8 and 13";
        }else if(blockID==Blocks.pumpkin||blockID==Blocks.lit_pumpkin){
			return metadata < 5 ? null : fail + " 0 and 4";
        }else if(blockID==Blocks.fence_gate){
			return metadata < 8 ? null : fail + " 0 and 7";
        }else if(blockID==Blocks.wooden_slab ||blockID==Blocks.bed){
			return metadata % 8 < 4 ? null : fail + " 0 and 3 or 8 and 11";
        }else if(blockID==Blocks.torch||blockID==Blocks.redstone_torch||blockID==Blocks.unlit_redstone_torch){
			return metadata > 0 && metadata < 7 ? null : fail + " 1 and 6";
		}
		return null;
	}

	public static int rotDir(int dir, int rotation) {
		return (dir + rotation + 4) % 4;
	}

	public static void setBlockAndMetaNoLighting(World world, int i, int j, int k, Block blockId, int meta) {
		if (i < 0xfe363c80 || k < 0xfe363c80 || i >= 0x1c9c380 || k >= 0x1c9c380 || j < 0 || j > Building.WORLD_MAX_Y)
			return;
		world.getChunkFromChunkCoords(i >> 4, k >> 4).func_150807_a(i & 0xf, j, k & 0xf, blockId, meta);
	}

	// ******************** STATIC FUNCTIONS
	// ******************************************************************************************************************************************//
	protected static void fillDown(int[] lowPt, int jtop, World world) {
		while (BlockProperties.get(world.getBlock(lowPt[0], lowPt[1], lowPt[2])).isArtificial)
			lowPt[1]--;
		Block oldSurfaceBlockId = world.getBlock(lowPt[0], lowPt[1], lowPt[2]);
		if (BlockProperties.get(oldSurfaceBlockId).isOre)
			oldSurfaceBlockId = Blocks.stone;
		if (oldSurfaceBlockId == Blocks.dirt || (lowPt[1] <= SEA_LEVEL && oldSurfaceBlockId == Blocks.sand))
			oldSurfaceBlockId = Blocks.grass;
		if (oldSurfaceBlockId == Blocks.air)
			oldSurfaceBlockId = world.provider.isHellWorld ? Blocks.netherrack : Blocks.grass;
		Block fillBlockId = oldSurfaceBlockId == Blocks.grass ? Blocks.dirt : oldSurfaceBlockId;
		for (; lowPt[1] <= jtop; lowPt[1]++)
			setBlockAndMetaNoLighting(world, lowPt[0], lowPt[1], lowPt[2], lowPt[1] == jtop ? oldSurfaceBlockId : fillBlockId, 0);
	}

	protected static int minOrMax(int[] a, boolean isMin) {
		if (isMin) {
			int min = Integer.MAX_VALUE;
			for (int i : a)
				min = Math.min(min, i);
			return min;
		} else {
			int max = Integer.MIN_VALUE;
			for (int i : a)
				max = Math.max(max, i);
			return max;
		}
	}

	// wiggle allows for some leeway before nonzero is detected
	protected static int signum(int n, int wiggle) {
		if (n <= wiggle && -n <= wiggle)
			return 0;
		return n < 0 ? -1 : 1;
	}

	private static boolean isSolidBlock(Block blockID) {
		return blockID.getMaterial().isSolid();
	}
}
