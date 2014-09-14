/* Source code for the The Great Wall Mod and Walled City Generator Mods for the game Minecraft
 * Copyright (C) 2011 by formivore
 * Copyright (C) 2013-2014 by GotoLink
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package assets.generator;

import net.minecraft.block.Block;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class PlacedBlock extends BlockAndMeta{
    public final int x, y, z;
    public PlacedBlock(Block block, int[] data) {
        super(block, data[3]);
        this.x = data[0];
        this.y = data[1];
        this.z = data[2];
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
        }else if(obj instanceof PlacedBlock){
            return this.x == ((PlacedBlock) obj).x && this.y == ((PlacedBlock) obj).y && this.z == ((PlacedBlock) obj).z;
        }
        return false;
    }

    @Override
    public int hashCode(){
        return new HashCodeBuilder().append(getMeta()).append(get()).append(x).append(y).append(z).toHashCode();
    }
}
