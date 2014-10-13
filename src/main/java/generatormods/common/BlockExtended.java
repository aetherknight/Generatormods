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
 * Extends BlockAndMeta further to represent a block with metadata, and an
 * internal store associating a given block to its BlockProperties.
 */
public class BlockExtended extends BlockAndMeta{
    public final String info;
    public BlockExtended(Block block, int meta, String extra) {
        super(block, meta);
        info = extra;
    }

    @Override
    public boolean equals(Object obj){
        if(obj==null){
            return false;
        }
        if(obj==this){
            return true;
        }
        if(!super.equals(obj)){
            return false;
        }else if(obj instanceof BlockExtended){
            return this.info.equals(((BlockExtended) obj).info);
        }
        return false;
    }

    @Override
    public int hashCode(){
        return new HashCodeBuilder().append(getMeta()).append(get()).append(info).toHashCode();
    }
}
