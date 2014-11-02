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
package generatormods;

import generatormods.commands.CommandBuild;
import generatormods.commands.CommandScan;
import generatormods.modules.CARuins;
import generatormods.modules.GreatWall;
import generatormods.modules.WalledCity;
import generatormods.util.ModUpdateDetectorWrapper;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.registry.GameRegistry;

@Mod(modid = GeneratorMods.modId, name = "Formivore's GeneratorMods", version = GeneratorMods.modVersion,
        dependencies = "after:ExtraBiomes,BiomesOPlenty", acceptableRemoteVersions = "*")
public class GeneratorMods {
    public final static String modId = "GeneratorMods";
    protected final static String modVersion = "0.1.6";

    @Instance(modId)
    public static GeneratorMods instance;


    protected CARuins caRuins;
    protected GreatWall greatWall;
    protected WalledCity walledCity;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {

        caRuins = new CARuins(modId);
        greatWall = new GreatWall(modId);
        walledCity = new WalledCity(modId);

        CARuins.instance = caRuins;
        GreatWall.instance = greatWall;
        WalledCity.instance = walledCity;

        ModUpdateDetectorWrapper.checkForUpdates(this, event);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        /*
         * Initialize the submods. We want to load templates after all mods have loaded so that we
         * can check whether any modded blockIDs are valid.
         */
        caRuins.loadConfiguration();
        greatWall.loadConfiguration();
        walledCity.loadConfiguration();

        // Register them as world generators
        if(!caRuins.isDisabled())
            GameRegistry.registerWorldGenerator(caRuins, 2);
        if (!greatWall.isDisabled())
            GameRegistry.registerWorldGenerator(greatWall, 1);
        if (!walledCity.isDisabled())
            GameRegistry.registerWorldGenerator(walledCity, 0);
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandBuild());
        event.registerServerCommand(new CommandScan());
    }
}
