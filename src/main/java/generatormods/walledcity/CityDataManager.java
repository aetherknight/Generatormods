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

import generatormods.PopulatorWalledCity;
import generatormods.common.Util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.village.Village;
import net.minecraft.village.VillageDoorInfo;
import net.minecraft.world.World;

import org.apache.logging.log4j.Logger;

/**
 * TODO: use 2 managers: 1 for surface cities and 1 for underground, unless the two have to interoperate.
 */
public class CityDataManager {
    private final static String CITY_FILE_SAVE = "WalledCities.txt";

    private Logger logger;
    private int undergroundMinCitySeparation;
    private int minCitySeparation;
    // Map of all village doors in a given structure/city
    private Map<Integer, List<VillageDoorInfo>> cityDoors;
    public Map<World, List<int[]>> cityLocations;
    private Map<World, File> cityFiles;

    public CityDataManager(Logger logger, int undergroundMinCitySeparation, int minCitySeparation) {
        this.undergroundMinCitySeparation = undergroundMinCitySeparation;
        this.minCitySeparation = minCitySeparation;
        cityDoors = new HashMap<Integer, List<VillageDoorInfo>>();
        cityLocations = new HashMap<World, List<int[]>>();
        cityFiles = new HashMap<World, File>();
    }

    /**
     * Add the village doors for the city identified by id to the world.
     *
     * Adds all village doors currently added for the city to a village object,
     * and then adds that village object to the world. It then cleans up the
     * cityDoors list afterwards.
     *
     * TODO: Make this part of a City object?
     */
    public void addCityToVillages(World world, int id) {
        if (world != null && world.provider.dimensionId != 1) {
            if (world.villageCollectionObj != null) {
                Village city = new Village(world);
                if (cityDoors.containsKey(id)) {
                    for (VillageDoorInfo door : cityDoors.get(id))
                        if (door != null)
                            city.addVillageDoorInfo(door);
                    world.villageCollectionObj.getVillageList().add(city);
                    cityDoors.remove(id);
                }
            }
        }
    }

    /**
     * Is the city at least the minimum distance away from all other cities?
     */
    public boolean isCitySeparated(World world, int i, int k, int cityType) {
        if (cityLocations.containsKey(world)) {
            for (int[] location : cityLocations.get(world)) {
                if (location[2] == cityType && Math.abs(location[0] - i) + Math.abs(location[1] - k) < (cityType == PopulatorWalledCity.CITY_TYPE_UNDERGROUND ? undergroundMinCitySeparation : minCitySeparation)) {
                    return false;
                }
            }
        }
        return true;
    }

    public void saveCityLocations(World world) {
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new BufferedWriter(new FileWriter(cityFiles.get(world), true)));
            BufferedReader br = new BufferedReader(new FileReader(cityFiles.get(world)));
            if (br.readLine() == null) {
                pw.println("City locations in " + world.provider.getDimensionName() + " of : " + world.getWorldInfo().getWorldName());
            }
            br.close();
            int[] location = cityLocations.get(world).get(cityLocations.get(world).size() - 1);
            pw.println(new StringBuilder(Integer.toString(location[0])).append(",").append(Integer.toString(location[1])).append(",").append(Integer.toString(location[2])));
        } catch (IOException e) {
            logger.warn(e);
        } finally {
            if (pw != null)
                pw.close();
        }
    }

    public void updateWorldExplored(World world) throws IOException {
        File cityFile = new File(Util.getWorldSaveDir(world), world.provider.getDimensionName() + CITY_FILE_SAVE);
        if (cityFiles.isEmpty() || !cityFiles.containsKey(world))
            cityFiles.put(world, cityFile);
        if (!cityFile.createNewFile() && !cityLocations.containsKey(world))
            cityLocations.put(world, getCityLocs(cityFile));
    }

    public List<int[]> getCityLocs(File city) {
        List<int[]> cityLocs = new ArrayList<int[]>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(city));
            for (String read = br.readLine(); read != null; read = br.readLine()) {
                String[] split = read.split(",");
                if (split.length == 3) {
                    cityLocs.add(new int[] { Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]) });
                }
            }
        } catch (IOException e) {
            logger.warn(e);
        } finally {
            try {
                if (br != null)
                    br.close();
            } catch (IOException e) {
            }
        }
        return cityLocs;
    }

    public void addCity(World world, int x, int z, int cityType) {
        cityLocations.get(world).add( new int[] { x, z, cityType });
    }

    public void addBuildingToDoorList(int buildingId) {
        cityDoors.put(buildingId, new ArrayList<VillageDoorInfo>());
    }

    public void addDoor(int buildingId, int par1, int par2, int par3, int par4, int par5, int par6) {
        cityDoors.get(buildingId).add(new VillageDoorInfo(par1, par2, par3, par4, par5, par6));
    }

    public Set<Integer> getDoorListKnownBuildingIds() {
        return cityDoors.keySet();
    }

}
