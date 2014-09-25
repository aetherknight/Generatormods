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
package generatormods.config;

import generatormods.Building;

import java.util.ArrayList;
import java.util.List;

public enum ChestType {
    EASY   (4, Building.DEFAULT_CHEST_ITEMS[0]),
    MEDIUM (6, Building.DEFAULT_CHEST_ITEMS[1]),
    HARD   (6, Building.DEFAULT_CHEST_ITEMS[2]),
    TOWER  (6, Building.DEFAULT_CHEST_ITEMS[3]);

    private int defaultChestTries;
    private List<ChestItemSpec> defaultChestItems;

    ChestType(int defaultChestTries, Object[][] defaultChestItems) {
        this.defaultChestTries = defaultChestTries;
        this.defaultChestItems = makeChestItemsFromArray(defaultChestItems);
    }

    public int getDefaultChestTries() {
        return defaultChestTries;
    }

    public List<ChestItemSpec> getDefaultChestItems() {
        return defaultChestItems;
    }

    public ChestContentsConfig getDefaultChestContentsConfig() {
        return new ChestContentsConfig(this, getDefaultChestTries(), getDefaultChestItems());
    }

    public static List<ChestItemSpec> makeChestItemsFromArray(Object[][] rawChestItems) {
        List<ChestItemSpec> chestItems = new ArrayList<ChestItemSpec>();
        for (Object[] rawChestItem : rawChestItems) {
            chestItems
                    .add(new ChestItemSpec(rawChestItem[1], (Integer) rawChestItem[2],
                            (Integer) rawChestItem[3], (Integer) rawChestItem[4],
                            (Integer) rawChestItem[5]));
        }
        return chestItems;
    }
}
