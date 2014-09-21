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

import generatormods.BuildingCellularAutomaton;

/**
 * CARule is used by the CARuins mod and can be used to generate towers for
 * great walls.
 */
public class CARule {

    private String ruleStr;
    private byte[][] rule;

    /**
     * Create a CARule. A configuration line contains a Birth number and a
     * survival number, like so:
     * 
     *     B3/S23
     *
     */
    public CARule(String ruleStr) throws ParseError {
        this.ruleStr = ruleStr;
        this.rule = parseRule(ruleStr);
    }

    public String toString() {
        return ruleStr;
    }

    private static byte[][] parseRule(String str) throws ParseError {
        try {
            byte[][] rule = new byte[][] { {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}};
            String birthStr = str.split("/")[0].trim();
            String surviveStr = str.split("/")[1].trim();
            for (int n = 1; n < birthStr.length(); n++) {
                int digit = Integer.parseInt(birthStr.substring(n, n + 1));
                rule[0][digit] = BuildingCellularAutomaton.ALIVE;
            }
            for (int n = 1; n < surviveStr.length(); n++) {
                int digit = Integer.parseInt(surviveStr.substring(n, n + 1));
                rule[1][digit] = BuildingCellularAutomaton.ALIVE;
            }
            return rule;
        } catch (Throwable e) {
            throw new ParseError("Error parsing \""+str+"\"", e);
        }
    }

    /**
     * Return the rule as 2 arrays of bytes to represent the birth rule and the
     * survival rule.
     */
    public byte[][] toBytes() {
        return rule;
    }
}
