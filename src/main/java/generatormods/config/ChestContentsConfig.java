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

import java.util.ArrayList;
import java.util.List;

public class ChestContentsConfig {
    private ChestType chestType;
    private int tries;
    private List<ChestItemSpec> chestItems;

    public ChestContentsConfig(ChestType type, int tries, List<ChestItemSpec> chestItems) {
        this.chestType = type;
        this.tries = tries;
        this.chestItems = chestItems;
    }

    public ChestContentsConfig(ChestType type, int tries, String[] chestItemStrings) {
        this.chestType = type;
        this.tries = tries;
        this.chestItems = new ArrayList<ChestItemSpec>();
        for (String chestItemString : chestItemStrings) {
            chestItems.add(new ChestItemSpec(chestItemString));
        }
    }

    public ChestType getChestType() {
        return chestType;
    }

    public int getTries() {
        return tries;
    }

    public List<ChestItemSpec> getChestItems() {
        return chestItems;
    }

    public Object[][] getChestItemsObjectArray() {
        Object[][] chestItemArray = new Object[chestItems.size()][];
        for (int i = 0; i < chestItems.size(); i++) {
            chestItemArray[i] = chestItems.get(i).toObjectArray();
            chestItemArray[i][0] = i;
        }
        return chestItemArray;
    }

    public String toString() {
        String str = "ChestContentsConfig(" + chestType + ", " + tries + ", {";
        for (ChestItemSpec chestItem : chestItems) {
            str += "{" + chestItem.toSpecString() + "}, ";
        }
        str += "})";
        return str;
    }
}
