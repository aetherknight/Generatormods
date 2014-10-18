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

import cpw.mods.fml.common.registry.GameData;

/**
 * Class for representing a possible item in a chest.
 */
public class ChestItemSpec {
    private Object blockOrItem;
    private int metadata = 0;
    private int selectionWeight;
    private int minStackSize;
    private int maxStackSize;

    /**
     * Creates a ChestItemSpec out of a string form of the spec.
     *
     * The spec format is:
     *
     * "{block/item id or name}[-{metadata}],{selection weight},{min stack size},{max stack size}"
     */
    public ChestItemSpec(String specString) {
        Object[] parsedSpec = parseStringSpec(specString);

        blockOrItem = lookupBlockOrItem((String) parsedSpec[0]);
        metadata = (Integer) parsedSpec[1];
        selectionWeight = (Integer) parsedSpec[2];
        minStackSize = (Integer) parsedSpec[3];
        maxStackSize = (Integer) parsedSpec[4];

        validate();
    }

    public ChestItemSpec(String blockOrItemId, int metadata, int selectionWeight, int minStackSize,
            int maxStackSize) {
        this(lookupBlockOrItem(blockOrItemId), metadata, selectionWeight, minStackSize,
                maxStackSize);
    }

    public ChestItemSpec(Object blockOrItem, int metadata, int selectionWeight, int minStackSize,
            int maxStackSize) {
        this.blockOrItem = blockOrItem;
        this.metadata = metadata;
        this.selectionWeight = selectionWeight;
        this.minStackSize = minStackSize;
        this.maxStackSize = maxStackSize;

        validate();
    }

    /**
     * Returns an array of the fields from a Chest Item spec string.
     *
     * Where a spec string looks like:
     *
     * {block id or name}[-{metadata}],{weight},{min},{max}
     *
     * It returns:
     *
     * { "block id or name", Integer(metadata), Integer(weight), Integer(min), Integer(max) }
     */
    public static Object[] parseStringSpec(String spec) {
        String[] intStrs = spec.trim().split(",");
        String[] idAndMeta = intStrs[0].split("-");
        Object[] parsedSpec = new Object[5];

        parsedSpec[0] = idAndMeta[0];
        parsedSpec[1] = idAndMeta.length > 1 ? Integer.parseInt(idAndMeta[1]) : 0;
        parsedSpec[2] = Integer.parseInt(intStrs[1]);
        parsedSpec[3] = Integer.parseInt(intStrs[2]);
        parsedSpec[4] = Integer.parseInt(intStrs[3]);

        return parsedSpec;
    }

    public static Object lookupBlockOrItem(String possibleBlockOrItemId) {
        Object possibleBlockOrItem;
        try {
            // Try and parse it as a block ID
            int id = Integer.parseInt(possibleBlockOrItemId);
            possibleBlockOrItem = GameData.getItemRegistry().getObjectById(id);
            if (possibleBlockOrItem == null) {
                possibleBlockOrItem = GameData.getBlockRegistry().getObjectById(id);
            }
        } catch (Exception e) {
            // Not a valid block id, try and get the block by name (1.7 and newer)
            possibleBlockOrItem = GameData.getItemRegistry().getObject(possibleBlockOrItemId);
            if (possibleBlockOrItem == null) {
                possibleBlockOrItem = GameData.getBlockRegistry().getObject(possibleBlockOrItemId);
            }
        }
        return possibleBlockOrItem;
    }

    private void validate() {
        if (selectionWeight < 0)
            selectionWeight = 0;
        if (maxStackSize > 64)
            maxStackSize = 64;
        if (maxStackSize < 1)
            maxStackSize = 1;
        if (maxStackSize < minStackSize)
            minStackSize = maxStackSize;
    }

    public Object getBlockOrItem() {
        return blockOrItem;
    }

    public int getMetadata() {
        return metadata;
    }

    public int getSelectionWeight() {
        return selectionWeight;
    }

    public int getMinStackSize() {
        return minStackSize;
    }

    public int getMaxStackSize() {
        return maxStackSize;
    }

    public String toSpecString() {
        String blockName = GameData.getItemRegistry().getNameForObject(blockOrItem);
        if (blockName == null)
            blockName = GameData.getBlockRegistry().getNameForObject(blockOrItem);
        return blockName + "-" + metadata + "," + selectionWeight + "," + minStackSize + ","
                + maxStackSize;
    }
}
