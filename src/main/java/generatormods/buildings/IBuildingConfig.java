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
import generatormods.config.chests.ChestType;
import generatormods.walledcity.CityDataManager;
import generatormods.walledcity.ILayoutGenerator;

import java.util.Map;
import java.util.Random;

import net.minecraft.world.World;

import org.apache.logging.log4j.Logger;


public interface IBuildingConfig {
    /* Returns the backtrack length */
    public int getBacktrackLength();
    /* Returns the chest configurations */
    public Map<ChestType, ChestContentsSpec> getChestConfigs();
    /* Optional, returns either a CityDataManager or null */
    public CityDataManager getCityDataManager();
    /* Optional, returns either an ILayoutGenerator or null */
    public ILayoutGenerator getLayoutGenerator();
    /* Returns the mod's logger */
    public Logger getLogger();
    /* Returns the current Random object */
    public Random getRandom();
    /* Returns the Minecraft World object */
    public World getWorld();
}
