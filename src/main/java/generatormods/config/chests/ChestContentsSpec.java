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
package generatormods.config.chests;

import java.util.ArrayList;
import java.util.List;

/**
 * Specifies the contents of a particular type of chest.
 */
public class ChestContentsSpec {
    private ChestType chestType;
    private int tries;
    private List<ChestItemSpec> chestItems;

    public ChestContentsSpec(ChestType type, int tries, List<ChestItemSpec> chestItems) {
        this.chestType = type;
        this.tries = tries;
        this.chestItems = chestItems;
    }

    public ChestContentsSpec(ChestType type, int tries, String[] chestItemStrings) {
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

    public String toString() {
        String str = "ChestContentsSpec(" + chestType + ", " + tries + ", {";
        for (ChestItemSpec chestItem : chestItems) {
            str += "{" + chestItem.toSpecString() + "}, ";
        }
        str += "})";
        return str;
    }
}
