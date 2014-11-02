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
