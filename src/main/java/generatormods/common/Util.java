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

import java.io.File;

import net.minecraft.world.World;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.storage.ISaveHandler;

public class Util {
    public static File getWorldSaveDir(World world) {
        ISaveHandler worldSaver = world.getSaveHandler();
        if (worldSaver.getChunkLoader(world.provider) instanceof AnvilChunkLoader) {
            return ((AnvilChunkLoader) worldSaver.getChunkLoader(world.provider)).chunkSaveLocation;
        }
        return null;
    }
}
