package generatormods.common;

import java.util.HashMap;
import java.util.Map;

public class DirMeta {
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
}
