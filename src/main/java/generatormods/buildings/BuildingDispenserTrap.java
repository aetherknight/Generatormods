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
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityDispenser;
import static generatormods.codex.DirToMetaMappings.BUTTON_DIR_TO_META;
import static generatormods.codex.DirToMetaMappings.LADDER_DIR_TO_META;

/*
 * BuildingDispenserTrap generates a redstone activated dispenser trap
 */
public class BuildingDispenserTrap extends Building {
    public enum MissileType { ARROW, DAMAGE_POTION };

    private static BlockAndMeta[] CODE_TO_BLOCK = new BlockAndMeta[] {
            BlockAndMeta.PRESERVE_BLOCK,
            null,
            new BlockAndMeta(Blocks.air, 0),
            new BlockAndMeta(Blocks.redstone_wire, 0),
            new BlockAndMeta(Blocks.redstone_torch, BUTTON_DIR_TO_META.get(Dir.NORTH)),
            new BlockAndMeta(Blocks.unlit_redstone_torch, BUTTON_DIR_TO_META.get(Dir.SOUTH)),
            new BlockAndMeta(Blocks.redstone_torch, 5)};

    /* [z][y][x] */
    private static int[][][] MECHANISM = new int[][][] {
            {   { 0, 0, 0 },
                { 0, 0, 0 },
                { 0, 1, 0 },
                { 0, 0, 0 } },
            {   { 0, 0, 0 },
                { 1, 1, 1 },
                { 1, 4, 1 },
                { 1, 1, 1 } },
            {   { 0, 0, 0 },
                { 1, 1, 1 },
                { 1, 1, 1 },
                { 1, 5, 1 } },
            {   { 0, 1, 0 },
                { 1, 1, 1 },
                { 1, 3, 1 },
                { 1, 1, 1 } },
            {   { 0, 1, 0 },
                { 1, 6, 1 },
                { 1, 0, 1 },
                { 1, 2, 1 } },
            {   { 0, 1, 0 },
                { 1, 1, 1 },
                { 1, 1, 1 },
                { 1, 1, 1 } }, };

    public BuildingDispenserTrap(IBuildingConfig config, TemplateRule bRule_, Dir bDir_,
            int plateSeparation, int[] sourcePt) {
        super(0, config, bRule_, bDir_, Handedness.R_HAND, true, new int[] {3, 6, plateSeparation},
                sourcePt);
	}

    /**
     * <pre>
     *      ---   bLength+3 - end of mechanism
     *      | |
     *      | |
     *      ---   z=bLength - mechanism start,
     *       *    z==bLength-1 - end of redstone wire
     *       *
     *       0    z=0 - trigger plate
     * </pre>
     */
	public void build(MissileType missileType, boolean multipleTriggers) {
		if (bLength < 0)
			bLength = 0;
        logger.debug("Building dispenser trap at "+i0+","+j0+","+k0+", plateSeparation="+bLength);
		for (int x = 0; x < MECHANISM[0][0].length; x++) {
            for (int z = 0; z < MECHANISM[0].length; z++) {
                for (int y = 0; y < MECHANISM.length; y++) {
                    if (MECHANISM[y][3 - z][x] == 1)
                        setBlockLocal(x, y - 3, z + bLength, bRule);
					else
                        setBlockLocal(x, y - 3, z + bLength, CODE_TO_BLOCK[MECHANISM[y][3 - z][x]]);
				}
			}
		}
        for (int z = 0; z < bLength; z++) {
            setBlockLocal(1, -3, z, bRule);
            setBlockLocal(1, -2, z, Blocks.redstone_wire);
            setBlockLocal(0, -2, z, bRule);
            setBlockLocal(2, -2, z, bRule);
            setBlockLocal(1, -1, z, bRule);
            setBlockLocal(1, 0, z,
                    multipleTriggers && random.nextBoolean() ? Blocks.stone_pressure_plate
                            : Blocks.air);
            setBlockLocal(1, 1, z, Blocks.air);
		}
		setBlockLocal(1, 0, 0, Blocks.stone_pressure_plate);
        flushDelayed();
        ItemStack itemstack =
                missileType == MissileType.ARROW ? new ItemStack(Items.arrow,
                        30 + random.nextInt(10), 0) : new ItemStack(Items.potionitem,
                        30 + random.nextInt(10), 12 | 0x4000);
		setItemDispenser(1, 1, bLength + 1, Dir.SOUTH, itemstack);
	}

	public boolean queryCanBuild(int minLength) {
		if (!isFloor(1, 0, 0))
			return false;
		//search along floor for a height 2 wall. If we find it, reset bLength and return true.
        for (int z = 1; z < 9; z++) {
            if (isFloor(1, 0, z))
				continue;
            if (!isWallBlock(1, -1, z))
				return false;
            //now must have block at (1,0,z)...
            if (z < minLength)
				return false;
            if (!isWallable(1, 1, z)) {
                bLength = z;
				return true;
			}
			return false;
		}
		return false;
	}

    private void setItemDispenser(int x, int y, int z, Dir metaDir, ItemStack itemstack) {
        int[] pt = getIJKPt(x, y, z);
		world.setBlock(pt[0], pt[1], pt[2], Blocks.dispenser, 0, 2);
		world.setBlockMetadataWithNotify(pt[0], pt[1], pt[2], LADDER_DIR_TO_META.get(orientDirToBDir(metaDir)), 3);
		try {
			TileEntityDispenser tileentitychest = (TileEntityDispenser) world.getTileEntity(pt[0], pt[1], pt[2]);
			if (itemstack != null && tileentitychest != null)
				tileentitychest.setInventorySlotContents(random.nextInt(tileentitychest.getSizeInventory()), itemstack);
		} catch (Exception e) {
            logger.error("Error filling dispenser", e);
		}
	}
}
