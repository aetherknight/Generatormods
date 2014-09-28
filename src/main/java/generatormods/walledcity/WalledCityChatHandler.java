/* Source code for the The Great Wall Mod and Walled City Generator Mods for the game Minecraft
 * Copyright (C) 2011 by formivore
 * Copyright (C) 2013-2014 by GotoLink
 * Copyright (C) 2014 by William (B.J.) Snow Orvis (aetherknight)
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
package generatormods.walledcity;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;

public class WalledCityChatHandler {
    private boolean dontSuppressToAllPlayers;

    public WalledCityChatHandler(boolean dontSuppressToAllPlayers) {
        this.dontSuppressToAllPlayers = dontSuppressToAllPlayers;
    }

    private List<EntityPlayerMP> getCurrentPlayers() {
        List<EntityPlayerMP> playerList = new ArrayList<EntityPlayerMP>();
        List<?> playerListNoType =
                MinecraftServer.getServer().getConfigurationManager().playerEntityList;
        for (Object playerObject : playerListNoType)
            playerList.add((EntityPlayerMP) playerObject);
        return playerList;
    }

    private void tellPlayer(EntityPlayerMP player, String message) {
        player.addChatComponentMessage(new ChatComponentText(message));
    }

    public void tellAllPlayers(String message) {
        if (!dontSuppressToAllPlayers)
            return;
        List<EntityPlayerMP> playerList = getCurrentPlayers();
        if (playerList == null)
            return;
        for (EntityPlayerMP player : playerList) {
            tellPlayer(player, message);
        }
    }

    public void chatCityBuilt(int[] args, boolean underground) {
        if (!dontSuppressToAllPlayers)
            return;
        List<EntityPlayerMP> playerList = getCurrentPlayers();
        if (playerList == null)
            return;
        for (EntityPlayerMP player : playerList) {
            String cityType = underground ? "underground city" : "city";
            String dirStr;
            int dI = args[0] - (int) player.posX;
            int dK = args[2] - (int) player.posZ;
            if (dI * dI + dK * dK < args[4] * args[4]) {
                dirStr = "nearby";
            }
            dirStr = "to the ";
            if (Math.abs(dI) > 2 * Math.abs(dK))
                dirStr += dI > 0 ? "east" : "west";
            else if (Math.abs(dK) > 2 * Math.abs(dI))
                dirStr += dK > 0 ? "south" : "north";
            else
                dirStr +=
                        dI > 0 ? (dK > 0 ? "southeast" : "northeast") : (dK > 0 ? "southwest"
                                : "northwest");
            tellPlayer(player, "Built " + cityType + " " + dirStr + " (" + args[0] + "," + args[1]
                    + "," + args[2] + ")!");
        }
    }
}
