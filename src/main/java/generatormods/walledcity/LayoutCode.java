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
package generatormods.walledcity;

/**
 * LayoutCode specifies the character used when generating a map of a city for
 * debugging purposes.
 * <p>
 * Use null when you want to specify that a building has no code.
 */
public enum LayoutCode {
    EMPTY(' '), WALL('#'), AVENUE('='), STREET('-'), TOWER('@'), TEMPLATE('&');

    public char symbol;

    private LayoutCode(char symbol) {
        this.symbol = symbol;
    }

    public boolean canOverride(LayoutCode oldCode) {
        return LAYOUT_CODE_OVERRIDE_MATRIX[oldCode.ordinal()][this.ordinal()] == 1;
    }

	private final static int[][] LAYOUT_CODE_OVERRIDE_MATRIX = new int[][] { //present code=rows, attempted overriding code=columns
		{ 0, 1, 1, 1, 1, 1 }, //present empty
		{ 0, 0, 0, 0, 0, 0 }, //present wall
		{ 0, 0, 1, 1, 0, 0 }, //present avenue
		{ 0, 0, 1, 1, 1, 0 }, //present street
		{ 0, 0, 0, 0, 0, 0 }, //present tower
		{ 0, 0, 0, 0, 0, 0 } }; //present template
}
