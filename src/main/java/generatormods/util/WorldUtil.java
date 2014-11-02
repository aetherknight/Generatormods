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
package generatormods.util;

import generatormods.util.build.BlockProperties;

import java.io.File;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.storage.ISaveHandler;

/**
 * Static helper methods that interact with the World.
 */
public class WorldUtil {
    /* Return value for findSurfaceJ() that means we hit water. */
    public final static int HIT_WATER = -666;

    /**
     * Special value to ignore water depth when looking for surface blocks with findSurfaceJ().
     */
    public final static int IGNORE_WATER = -1;

    public final static int SEA_LEVEL = 63;
    public final static int WORLD_MAX_Y = 255;

    /* Compute the distnace (rounded down to nearest integer) between 2 points */
    public static int distance(int[] pt1, int[] pt2) {
        return (int) Math.sqrt((pt1[0] - pt2[0]) * (pt1[0] - pt2[0]) + (pt1[1] - pt2[1])
                * (pt1[1] - pt2[1]) + (pt1[2] - pt2[2]) * (pt1[2] - pt2[2]));
    }

    /**
     * Finds a surface block. Depending on the value of waterIsSurface and wallIsSurface it will
     * treat liquid and wall blocks as either solid or air.
     *
     * @param world The world we are checking
     * @param i X-coordinate of world to check
     * @param k Z-coordinate of world to check
     * @param jinit Initial Y-coordinate to begin checking down from to find the surface Y value.
     * @param wallIsSurface ???
     * @param waterSurfaceBuffer The number of vertical blocks of water we permit. Allows us to
     *        build in swamps and other shallow water. Set this to -1 to completely ignore water.
     * @return The Y coordinate of the surface. However, if there is more water than
     *         waterSurfaceBuffer, then it will return HIT_WATER instead.
     */
    public static int findSurfaceJ(World world, int i, int k, int jinit, boolean wallIsSurface,
            int waterSurfaceBuffer) {
        Block blockId;
        // if(world.getChunkProvider().chunkExists(i>>4, k>>4))
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
                int minecraftHeight =
                        world.getChunkFromBlockCoords(i, k).getHeightValue(i & 0xf, k & 0xf);
                if (minecraftHeight < jinit)
                    jinit = minecraftHeight;
                for (int j = jinit; j >= 0; j--) {
                    blockId = world.getBlock(i, j, k);
                    /*
                     * This will return the current coordinate when the the current block is not
                     * wallable (wallables are blocks that can be replaced with walls, like water,
                     * air, trees, plants) and it is not artificial (both of these together means
                     * the block is an ore or a naturally occurring ground block). It will also
                     * return when the current block is not wallable, but wallIsSurface is set to
                     * true.
                     */
                    if (!BlockProperties.get(blockId).isWallable
                            && (wallIsSurface || !BlockProperties.get(blockId).isArtificial))
                        return j;
                    /*
                     * If we aren't ignoring water entirely, and the current block is water, then
                     * return HIT_WATER if j - waterSurfaceBuffer is water, or return j (because
                     * there is less than waterSurfaceBuffer below our current point.
                     */
                    if (waterSurfaceBuffer != IGNORE_WATER && BlockProperties.get(blockId).isWater)
                        return BlockProperties.get(world.getBlock(i, j - waterSurfaceBuffer, k)).isWater ? HIT_WATER
                                : j;
                    /* Otherwise, continue on down. */
                }
            }
        }
        return -1;
    }

    /**
     * Appears to set a block (and its metadata) at a given coordinate, in a manner that appears to
     * set the value directly instead of using something like StructureComponent's
     * placeBlockAtCurrentPosition().
     * <p>
     * TODO: We should consider using something similar to StructureComponent's
     * placeBlockAtCurrentPosition()
     */
    public static void setBlockAndMetaNoLighting(World world, int i, int j, int k, Block blockId,
            int meta) {
        if (i < 0xfe363c80 || k < 0xfe363c80 || i >= 0x1c9c380 || k >= 0x1c9c380 || j < 0
                || j > WORLD_MAX_Y)
            return;
        world.getChunkFromChunkCoords(i >> 4, k >> 4).func_150807_a(i & 0xf, j, k & 0xf, blockId,
                meta);
    }

    /**
     * Fills blocks from lowPt up to y=jtop. In general, lowPt should be the old surface point. It
     * then fills blocks above it up to y=jtop as follows:
     *
     * <ul>
     * <li>If the old surface point is an ore, then use stone as the fill type.</li>
     * <li>If the old surface point is is dirt, or if it is below sea level and sand, then use fill
     * with dirt but then use a grass block for the new to-point.</li>
     * <li>If the old surface point is air, then use either fill with netherrack (for Nether), or
     * dirt and then grass.</li>
     * <li>Otherwise, fill with the same type of block as the existing starting point.</li>
     * </ul>
     */
    public static void fillDown(int[] lowPt, int jtop, World world) {
        while (BlockProperties.get(world.getBlock(lowPt[0], lowPt[1], lowPt[2])).isArtificial)
            lowPt[1]--;
        Block oldSurfaceBlockId = world.getBlock(lowPt[0], lowPt[1], lowPt[2]);
        if (BlockProperties.get(oldSurfaceBlockId).isOre)
            oldSurfaceBlockId = Blocks.stone;
        if (oldSurfaceBlockId == Blocks.dirt
                || (lowPt[1] <= SEA_LEVEL && oldSurfaceBlockId == Blocks.sand))
            oldSurfaceBlockId = Blocks.grass;
        if (oldSurfaceBlockId == Blocks.air)
            oldSurfaceBlockId = world.provider.isHellWorld ? Blocks.netherrack : Blocks.grass;
        Block fillBlockId = oldSurfaceBlockId == Blocks.grass ? Blocks.dirt : oldSurfaceBlockId;
        for (; lowPt[1] <= jtop; lowPt[1]++)
            setBlockAndMetaNoLighting(world, lowPt[0], lowPt[1], lowPt[2],
                    lowPt[1] == jtop ? oldSurfaceBlockId : fillBlockId, 0);
    }

    public static File getWorldSaveDir(World world) {
        ISaveHandler worldSaver = world.getSaveHandler();
        if (worldSaver.getChunkLoader(world.provider) instanceof AnvilChunkLoader) {
            return ((AnvilChunkLoader) worldSaver.getChunkLoader(world.provider)).chunkSaveLocation;
        }
        return null;
    }

}
