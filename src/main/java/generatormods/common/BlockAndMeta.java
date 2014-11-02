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
package generatormods.common;

import generatormods.codex.BlockProperties;
import generatormods.config.chests.ChestType;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import static generatormods.codex.DirToMetaMappings.BUTTON_DIR_TO_META;
import static generatormods.codex.DirToMetaMappings.LADDER_DIR_TO_META;

/**
 * Stores a block type, plus a metadata value for that block.
 */
public class BlockAndMeta {
    // some prebuilt directional blocks
    public final static BlockAndMeta WEST_FACING_TORCH = new BlockAndMeta(Blocks.torch, BUTTON_DIR_TO_META.get(Dir.WEST));
    public final static BlockAndMeta EAST_FACING_TORCH = new BlockAndMeta(Blocks.torch, BUTTON_DIR_TO_META.get(Dir.EAST));
    public final static BlockAndMeta NORTH_FACING_TORCH = new BlockAndMeta(Blocks.torch, BUTTON_DIR_TO_META.get(Dir.NORTH));
    public final static BlockAndMeta SOUTH_FACING_TORCH = new BlockAndMeta(Blocks.torch, BUTTON_DIR_TO_META.get(Dir.SOUTH));
    public final static BlockAndMeta EAST_FACING_LADDER = new BlockAndMeta(Blocks.ladder, LADDER_DIR_TO_META.get(Dir.EAST));
    public final static BlockAndMeta AIR_WITH_LIGHTING = new BlockAndMeta(Blocks.air, 0);
    public final static BlockAndMeta AIR_WITH_NO_LIGHTING = new BlockAndMeta(Blocks.air, 1);
    public final static BlockAndMeta PRESERVE_BLOCK = new BlockExtended(Blocks.air, 0, TemplateRule.SPECIAL_AIR);
    public final static BlockAndMeta TOWER_CHEST = new BlockExtended(Blocks.chest, 0, ChestType.TOWER.toString());
    public final static BlockAndMeta HARD_CHEST = new BlockExtended(Blocks.chest, 0, ChestType.HARD.toString());
    public final static BlockAndMeta GHAST_SPAWNER = new BlockExtended(Blocks.mob_spawner, 0, "Ghast");

    private final Block block;
    private final int meta;

    public BlockAndMeta(Block block, int meta) {
        this.block = block;
        this.meta = meta;
    }

    public Block getBlock() {
        return block;
    }

    public int getMeta() {
        return meta;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj == this)
            return true;
        if (!(obj instanceof BlockAndMeta))
            return false;
        BlockAndMeta other = (BlockAndMeta) obj;
        return obj instanceof BlockAndMeta && this.meta == other.getMeta()
                && this.block == other.getBlock();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(meta).append(block).toHashCode();
    }

    public Block toStair() {
        if (block == Blocks.cobblestone || block == Blocks.mossy_cobblestone) {
            return Blocks.stone_stairs;
        } else if (block == Blocks.nether_brick) {
            return Blocks.nether_brick_stairs;
        } else if (block == Blocks.stonebrick || block == Blocks.stone) {
            return Blocks.stone_brick_stairs;
        } else if (block == Blocks.brick_block) {
            return Blocks.brick_stairs;
        } else if (block == Blocks.sandstone) {
            return Blocks.sandstone_stairs;
        } else if (block == Blocks.quartz_block) {
            return Blocks.quartz_stairs;
        } else if (block == Blocks.planks) {
            switch (meta) {
                case 0:
                    return Blocks.oak_stairs;
                case 1:
                    return Blocks.spruce_stairs;
                case 2:
                    return Blocks.birch_stairs;
                case 3:
                    return Blocks.jungle_stairs;
                case 4:
                    return Blocks.acacia_stairs;
                case 5:
                    return Blocks.dark_oak_stairs;
            }
        }
        return block;
    }

    public BlockAndMeta toStep() {
        if (!BlockProperties.get(block).isArtificial)
            return this;
        if (block == Blocks.sandstone) {
            return new BlockAndMeta(Blocks.stone_slab, 1);
        } else if (block == Blocks.planks) {
            return new BlockAndMeta(Blocks.stone_slab, 2);
        } else if (block == Blocks.cobblestone) {
            return new BlockAndMeta(Blocks.stone_slab, 3);
        } else if (block == Blocks.brick_block) {
            return new BlockAndMeta(Blocks.stone_slab, 4);
        } else if (block == Blocks.stonebrick) {
            return new BlockAndMeta(Blocks.stone_slab, 5);
        } else if (block == Blocks.nether_brick) {
            return new BlockAndMeta(Blocks.stone_slab, 6);
        } else if (block == Blocks.quartz_block) {
            return new BlockAndMeta(Blocks.stone_slab, 7);
        } else if (block == Blocks.stone_slab || block == Blocks.double_stone_slab) {
            return new BlockAndMeta(Blocks.stone_slab, getMeta());
        } else if (block == Blocks.double_wooden_slab || block == Blocks.wooden_slab) {
            return new BlockAndMeta(Blocks.wooden_slab, getMeta());
        } else {
            return new BlockAndMeta(block, 0);
        }
    }

    public BlockAndMeta stairToSolid() {
        Block block = this.block;
        int meta = 0;
        if (block == Blocks.stone_stairs) {
            block = Blocks.cobblestone;
        } else if (block == Blocks.oak_stairs) {
            block = Blocks.planks;
        } else if (block == Blocks.spruce_stairs) {
            block = Blocks.planks;
            meta = 1;
        } else if (block == Blocks.birch_stairs) {
            block = Blocks.planks;
            meta = 2;
        } else if (block == Blocks.jungle_stairs) {
            block = Blocks.planks;
            meta = 3;
        } else if (block == Blocks.acacia_stairs) {
            block = Blocks.planks;
            meta = 4;
        } else if (block == Blocks.dark_oak_stairs) {
            block = Blocks.planks;
            meta = 5;
        } else if (block == Blocks.brick_stairs) {
            block = Blocks.brick_block;
        } else if (block == Blocks.stone_brick_stairs) {
            block = Blocks.stonebrick;
        } else if (block == Blocks.nether_brick_stairs) {
            block = Blocks.nether_brick;
        } else if (block == Blocks.sandstone_stairs) {
            block = Blocks.sandstone;
        } else if (block == Blocks.quartz_stairs) {
            block = Blocks.quartz_block;
        }
        return new BlockAndMeta(block, meta);
    }

    /**
     * Sanity checks the meta value for a given block.
     *
     * @param blockID The block type being checked.
     * @param metadata The metadata value being checked.
     *
     * @return null if valid, otherwise a string describing what is wrong with the metadata value.
     */
    public static String metaValueCheck(Block blockID, int metadata) {
        if (metadata < 0 || metadata >= 16)
            return "All Minecraft meta values should be between 0 and 15";
        String fail = blockID.getUnlocalizedName() + " meta value should be between";
        if (BlockProperties.get(blockID).isStair)
            return metadata < 8 ? null : fail + " 0 and 7";
        // orientation metas
        if (blockID == Blocks.rail) {
            return metadata < 10 ? null : fail + " 0 and 9";
        } else if (blockID == Blocks.stone_button || blockID == Blocks.wooden_button) {
            return metadata % 8 > 0 && metadata % 8 < 5 ? null : fail + " 1 and 4 or 9 and 12";
        } else if (blockID == Blocks.ladder || blockID == Blocks.dispenser
                || blockID == Blocks.furnace || blockID == Blocks.lit_furnace
                || blockID == Blocks.wall_sign || blockID == Blocks.piston
                || blockID == Blocks.piston_extension || blockID == Blocks.chest
                || blockID == Blocks.hopper || blockID == Blocks.dropper
                || blockID == Blocks.golden_rail || blockID == Blocks.detector_rail
                || blockID == Blocks.activator_rail) {
            return metadata % 8 < 6 ? null : fail + " 0 and 5 or 8 and 13";
        } else if (blockID == Blocks.pumpkin || blockID == Blocks.lit_pumpkin) {
            return metadata < 5 ? null : fail + " 0 and 4";
        } else if (blockID == Blocks.fence_gate) {
            return metadata < 8 ? null : fail + " 0 and 7";
        } else if (blockID == Blocks.wooden_slab || blockID == Blocks.bed) {
            return metadata % 8 < 4 ? null : fail + " 0 and 3 or 8 and 11";
        } else if (blockID == Blocks.torch || blockID == Blocks.redstone_torch
                || blockID == Blocks.unlit_redstone_torch) {
            return metadata > 0 && metadata < 7 ? null : fail + " 1 and 6";
        }
        return null;
    }
}
