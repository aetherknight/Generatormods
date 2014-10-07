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
package generatormods.common.config;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;

public enum ChestType {
    EASY(4) {
        @Override
        protected List<ChestItemSpec> getDefaultItems() {
            List<ChestItemSpec> chestItems = new ArrayList<ChestItemSpec>();
            chestItems.add(new ChestItemSpec(Items.arrow, 0, 2, 1, 12));
            chestItems.add(new ChestItemSpec(Items.iron_sword, 0, 2, 1, 1));
            chestItems.add(new ChestItemSpec(Items.leather_leggings, 0, 1, 1, 1));
            chestItems.add(new ChestItemSpec(Items.iron_shovel, 0, 1, 1, 1));
            chestItems.add(new ChestItemSpec(Items.string, 0, 1, 1, 1));
            chestItems.add(new ChestItemSpec(Items.iron_pickaxe, 0, 2, 1, 1));
            chestItems.add(new ChestItemSpec(Items.leather_boots, 0, 1, 1, 1));
            chestItems.add(new ChestItemSpec(Items.bucket, 0, 1, 1, 1));
            chestItems.add(new ChestItemSpec(Items.leather_helmet, 0, 1, 1, 1));
            chestItems.add(new ChestItemSpec(Items.wheat_seeds, 0, 1, 10, 15));
            chestItems.add(new ChestItemSpec(Items.gold_nugget, 0, 2, 3, 8));
            chestItems.add(new ChestItemSpec(Items.potionitem, 5, 2, 1, 1)); // healing I
            chestItems.add(new ChestItemSpec(Items.potionitem, 4, 1, 1, 1)); // poison, hehe
            return chestItems;
        }
    },
    MEDIUM(6) {
        @Override
        protected List<ChestItemSpec> getDefaultItems() {
            List<ChestItemSpec> chestItems = new ArrayList<ChestItemSpec>();
            chestItems.add(new ChestItemSpec(Items.golden_sword, 0, 2, 1, 1));
            chestItems.add(new ChestItemSpec(Items.milk_bucket, 0, 2, 1, 1));
            chestItems.add(new ChestItemSpec(Blocks.web, 0, 1, 8, 16));
            chestItems.add(new ChestItemSpec(Items.golden_shovel, 0, 1, 1, 1));
            chestItems.add(new ChestItemSpec(Items.golden_hoe, 0, 1, 0, 1));
            chestItems.add(new ChestItemSpec(Items.clock, 0, 1, 1, 1));
            chestItems.add(new ChestItemSpec(Items.iron_axe, 0, 3, 1, 1));
            chestItems.add(new ChestItemSpec(Items.map, 0, 1, 1, 1));
            chestItems.add(new ChestItemSpec(Items.apple, 0, 2, 2, 3));
            chestItems.add(new ChestItemSpec(Items.compass, 0, 1, 1, 1));
            chestItems.add(new ChestItemSpec(Items.iron_ingot, 0, 1, 5, 8));
            chestItems.add(new ChestItemSpec(Items.slime_ball, 0, 1, 1, 3));
            chestItems.add(new ChestItemSpec(Blocks.obsidian, 0, 1, 1, 4));
            chestItems.add(new ChestItemSpec(Items.bread, 0, 2, 8, 15));
            chestItems.add(new ChestItemSpec(Items.potionitem, 2, 1, 1, 1));
            chestItems.add(new ChestItemSpec(Items.potionitem, 37, 3, 1, 1)); // healing II
            chestItems.add(new ChestItemSpec(Items.potionitem, 34, 1, 1, 1)); // swiftness II
            chestItems.add(new ChestItemSpec(Items.potionitem, 9, 1, 1, 1)); // strength
            return chestItems;
        }
    },
    HARD(6) {
        @Override
        protected List<ChestItemSpec> getDefaultItems() {
            List<ChestItemSpec> chestItems = new ArrayList<ChestItemSpec>();
            chestItems.add(new ChestItemSpec(Blocks.sticky_piston, 0, 2, 6, 12));
            chestItems.add(new ChestItemSpec(Blocks.web, 0, 1, 8, 24));
            chestItems.add(new ChestItemSpec(Items.cookie, 0, 2, 8, 18));
            chestItems.add(new ChestItemSpec(Items.diamond_axe, 0, 1, 1, 1));
            chestItems.add(new ChestItemSpec(Items.minecart, 0, 1, 12, 24));
            chestItems.add(new ChestItemSpec(Items.redstone, 0, 2, 12, 24));
            chestItems.add(new ChestItemSpec(Items.lava_bucket, 0, 2, 1, 1));
            chestItems.add(new ChestItemSpec(Items.ender_pearl, 0, 1, 1, 1));
            chestItems.add(new ChestItemSpec(Blocks.mob_spawner, 0, 1, 2, 4));
            chestItems.add(new ChestItemSpec(Items.record_13, 0, 1, 1, 1));
            chestItems.add(new ChestItemSpec(Items.golden_apple, 0, 1, 4, 8));
            chestItems.add(new ChestItemSpec(Blocks.tnt, 0, 2, 8, 20));
            chestItems.add(new ChestItemSpec(Items.diamond, 0, 2, 1, 4));
            chestItems.add(new ChestItemSpec(Items.gold_ingot, 0, 2, 30, 64));
            chestItems.add(new ChestItemSpec(Items.potionitem, 37, 3, 1, 1)); // healing II
            chestItems.add(new ChestItemSpec(Items.potionitem, 49, 2, 1, 1)); // regeneration II
            chestItems.add(new ChestItemSpec(Items.potionitem, 3, 2, 1, 1)); // fire resistance
            return chestItems;
        }
    },
    TOWER(6) { // Tower
        @Override
        protected List<ChestItemSpec> getDefaultItems() {
            List<ChestItemSpec> chestItems = new ArrayList<ChestItemSpec>();
            chestItems.add(new ChestItemSpec(Items.arrow, 0, 1, 1, 12));
            chestItems.add(new ChestItemSpec(Items.fish, 0, 2, 1, 1));
            chestItems.add(new ChestItemSpec(Items.golden_helmet, 0, 1, 1, 1));
            chestItems.add(new ChestItemSpec(Blocks.web, 0, 1, 1, 12));
            chestItems.add(new ChestItemSpec(Items.iron_ingot, 0, 1, 2, 3));
            chestItems.add(new ChestItemSpec(Items.stone_sword, 0, 1, 1, 1));
            chestItems.add(new ChestItemSpec(Items.iron_axe, 0, 1, 1, 1));
            chestItems.add(new ChestItemSpec(Items.egg, 0, 2, 8, 16));
            chestItems.add(new ChestItemSpec(Items.saddle, 0, 1, 1, 1));
            chestItems.add(new ChestItemSpec(Items.wheat, 0, 2, 3, 6));
            chestItems.add(new ChestItemSpec(Items.gunpowder, 0, 1, 2, 4));
            chestItems.add(new ChestItemSpec(Items.leather_chestplate, 0, 1, 1, 1));
            chestItems.add(new ChestItemSpec(Blocks.pumpkin, 0, 1, 1, 5));
            chestItems.add(new ChestItemSpec(Items.gold_nugget, 0, 2, 1, 3));
            return chestItems;
        }
    };

    private int defaultChestTries;
    private List<ChestItemSpec> defaultChestItems;

    protected abstract List<ChestItemSpec> getDefaultItems();

    private ChestType(int defaultChestTries) {
        this.defaultChestTries = defaultChestTries;
        this.defaultChestItems = getDefaultItems();
    }

    public int getDefaultChestTries() {
        return defaultChestTries;
    }

    public List<ChestItemSpec> getDefaultChestItems() {
        return defaultChestItems;
    }

    public ChestContentsSpec getDefaultChestContentsConfig() {
        return new ChestContentsSpec(this, getDefaultChestTries(), getDefaultChestItems());
    }

    public static List<ChestItemSpec> makeChestItemsFromArray(Object[][] rawChestItems) {
        try {
            List<ChestItemSpec> chestItems = new ArrayList<ChestItemSpec>();
            for (Object[] rawChestItem : rawChestItems) {
                chestItems.add(new ChestItemSpec(rawChestItem[0], (Integer) rawChestItem[1],
                        (Integer) rawChestItem[2], (Integer) rawChestItem[3],
                        (Integer) rawChestItem[4]));
            }
            return chestItems;
        } catch (Exception e) {
            System.out.println(e + ": " + e.getMessage());
            e.printStackTrace();
            throw new ExceptionInInitializerError(e);
        }
    }
}
