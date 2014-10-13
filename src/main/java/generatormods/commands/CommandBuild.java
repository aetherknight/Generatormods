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

import generatormods.PopulatorCARuins;
import generatormods.PopulatorGreatWall;
import generatormods.PopulatorWalledCity;
import generatormods.buildings.Building;
import generatormods.gen.WorldGenCARuins;
import generatormods.gen.WorldGenGreatWall;
import generatormods.gen.WorldGenUndergroundCity;
import generatormods.gen.WorldGenWalledCity;
import generatormods.gen.WorldGeneratorThread;

import java.util.List;
import java.util.Random;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

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
                (new WorldGenCARuins(world, new Random(), posX, posZ,
                        PopulatorCARuins.instance.logger, PopulatorCARuins.instance.config)).run();
			} else if ("wall".equalsIgnoreCase(coordinate[0])) {
                (new WorldGenGreatWall(world, new Random(), posX, posZ, 1, 1.0,
                        PopulatorGreatWall.instance.logger,
                        PopulatorGreatWall.instance.config.chestConfigs,
                        PopulatorGreatWall.instance.wallStyles,
                        PopulatorGreatWall.instance.config.curveBias)).run();
			}
			/*
			 * else if ("monowall".equalsIgnoreCase(coordinate[0])) {
			 * wall=PopulatorGreatWall.instance; if(wall.placedCoords==null ||
			 * wall.placedWorld!=world){ wall.placedCoords=new
			 * int[]{posX,world.getHeightValue(posX, posZ),posZ};
			 * wall.placedWorld=world;
			 * var1.sendChatToPlayer("First set of wall coordinates stored."); }
			 * else{ wall.master.exploreThreads.add(new WorldGenSingleWall(
			 * wall, world, world.rand, new int[]{posX,
			 * world.getHeightValue(posX, posZ), posZ}));
			 * wall.placedCoords=null; wall.placedWorld=null; } }
			 */
			else if ("city".equalsIgnoreCase(coordinate[0])) {
                PopulatorWalledCity cityMod = PopulatorWalledCity.instance;
                (new WorldGenWalledCity(world, new Random(), posX, posZ, 1, 1.0, cityMod.logger,
                        cityMod.config.chestConfigs, cityMod.chatHandler, cityMod.cityDataManager,
                        cityMod.cityStyles, cityMod.config.rejectOnPreexistingArtifacts)).run();
			} else if ("undcity".equalsIgnoreCase(coordinate[0])) {
                PopulatorWalledCity cityMod = PopulatorWalledCity.instance;
                WorldGenUndergroundCity wgt =
                        new WorldGenUndergroundCity(world, new Random(), posX, posZ, 1, 1.0,
                                cityMod.logger, cityMod.config.chestConfigs, cityMod.chatHandler,
                                cityMod.cityDataManager, cityMod.undergroundCityStyles);
				int maxSpawnHeight = Building.findSurfaceJ(world, posX, posZ, Building.WORLD_MAX_Y, false, Building.IGNORE_WATER) - WorldGenUndergroundCity.MAX_DIAM / 2 - 5; //44 at sea level
				int minSpawnHeight = PopulatorWalledCity.MAX_FOG_HEIGHT + WorldGenUndergroundCity.MAX_DIAM / 2 - 8; //34, a pretty thin margin. Too thin for underocean cities?
				if (minSpawnHeight <= maxSpawnHeight)
					wgt.setSpawnHeight(minSpawnHeight, maxSpawnHeight, false);
				(wgt).run();
			}
		} else {
			throw new WrongUsageException(getCommandUsage(var1));
		}
	}

	@Override
	public List addTabCompletionOptions(ICommandSender var1, String[] var2) {
		return var2.length == 1 ? getListOfStringsMatchingLastWord(var2, "ruin", "wall", "city", "undcity") : null;
	}

    @Override
    public int compareTo(Object par1Obj){
        return this.compareTo((ICommand)par1Obj);
    }
}
