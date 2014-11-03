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
package generatormods.commands;

import generatormods.builders.CARuinsBuilder;
import generatormods.builders.GreatWallBuilder;
import generatormods.builders.UndergroundCityBuilder;
import generatormods.builders.WalledCityBuilder;
import generatormods.modules.CARuins;
import generatormods.modules.GreatWall;
import generatormods.modules.WalledCity;

import java.util.List;
import java.util.Random;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import static generatormods.util.WorldUtil.IGNORE_WATER;
import static generatormods.util.WorldUtil.WORLD_MAX_Y;
import static generatormods.util.WorldUtil.findSurfaceJ;

/**
 * Build command for players to try to build structures city/wall/ruin at the specified location
 */
public class CommandBuild extends CommandBase {
	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}

	@Override
	public String getCommandName() {
		return "build";
	}

	@Override
	public String getCommandUsage(ICommandSender commandSender) {
		return "/" + getCommandName() + " <ruin:wall:city:undcity> <dimensionID,default:0> <x> <z>";
	}

	@Override
    @SuppressWarnings("rawtypes")
	public List getCommandAliases() {
		return null;
	}

	@Override
	public void processCommand(ICommandSender var1, String[] coordinate) {
		if (coordinate.length == 4 || coordinate.length == 3) {
            func_152373_a(var1, this, "/build command used by " + var1.getCommandSenderName(), var1.getCommandSenderName(), coordinate);
			int posX = parseInt(var1, coordinate[coordinate.length - 2]);
			int posZ = parseInt(var1, coordinate[coordinate.length - 1]);
			World world = MinecraftServer.getServer().worldServers[coordinate.length == 3 ? 0 : Integer.parseInt(coordinate[1])];
			if ("ruin".equalsIgnoreCase(coordinate[0])) {
                (new CARuinsBuilder(world, new Random(), posX, posZ,
                        CARuins.instance.logger, CARuins.instance.config)).run();
			} else if ("wall".equalsIgnoreCase(coordinate[0])) {
                (new GreatWallBuilder(world, new Random(), posX, posZ, 1, 1.0,
                        GreatWall.instance.logger,
                        GreatWall.instance.config.getChestConfigs(),
                        GreatWall.instance.wallStyles,
                        GreatWall.instance.config.getCurveBias())).run();
			}
			else if ("city".equalsIgnoreCase(coordinate[0])) {
                WalledCity cityMod = WalledCity.instance;
                WalledCityBuilder wcb =
                        new WalledCityBuilder(world, new Random(), posX, posZ, 1, 1.0,
                                cityMod.logger, cityMod.config.getChestConfigs(),
                                cityMod.chatHandler, cityMod.cityDataManager,
                                cityMod.surfaceCityStyles,
                                cityMod.config.getRejectOnPreexistingArtifacts());
                wcb.run();
			} else if ("undcity".equalsIgnoreCase(coordinate[0])) {
                WalledCity cityMod = WalledCity.instance;
                UndergroundCityBuilder wgt =
                        new UndergroundCityBuilder(world, new Random(), posX, posZ, 1, 1.0,
                                cityMod.logger, cityMod.config.getChestConfigs(),
                                cityMod.chatHandler, cityMod.cityDataManager,
                                cityMod.undergroundCityStyles);
                int maxSpawnHeight =
                        findSurfaceJ(world, posX, posZ, WORLD_MAX_Y, false, IGNORE_WATER)
                                - UndergroundCityBuilder.MAX_DIAM / 2 - 5; // 44 at sea level
                int minSpawnHeight =
                        WalledCity.MAX_FOG_HEIGHT + UndergroundCityBuilder.MAX_DIAM / 2
                                - 8; // 34, a pretty thin margin. Too thin for underocean cities?
				if (minSpawnHeight <= maxSpawnHeight)
					wgt.setSpawnHeight(minSpawnHeight, maxSpawnHeight, false);
				(wgt).run();
			}
		} else {
			throw new WrongUsageException(getCommandUsage(var1));
		}
	}

	@Override
    @SuppressWarnings("rawtypes")
	public List addTabCompletionOptions(ICommandSender var1, String[] var2) {
		return var2.length == 1 ? getListOfStringsMatchingLastWord(var2, "ruin", "wall", "city", "undcity") : null;
	}

    @Override
    public int compareTo(Object par1Obj){
        return this.compareTo((ICommand)par1Obj);
    }
}
