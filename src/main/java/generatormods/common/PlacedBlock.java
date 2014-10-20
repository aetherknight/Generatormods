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

import net.minecraft.block.Block;

import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Sub-type of BlockAndMeta that represents a block placed in the world.
 */
public class PlacedBlock extends BlockAndMeta {
    public final int x, y, z;
    public PlacedBlock(Block block, int meta, int[] data) {
        super(block, meta);
        this.x = data[0];
        this.y = data[1];
        this.z = data[2];
    }

    @Override
    public boolean equals(Object obj){
        if(!super.equals(obj))
            return false;
        if(!(obj instanceof PlacedBlock))
            return false;
        PlacedBlock other = (PlacedBlock) obj;
        return this.x == other.x && this.y == other.y && this.z == other.z;
    }

    @Override
    public int hashCode(){
        return new HashCodeBuilder().append(getMeta()).append(getBlock()).append(x).append(y)
                .append(z).toHashCode();
    }
}
