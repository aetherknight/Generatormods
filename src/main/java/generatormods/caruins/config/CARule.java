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
package generatormods.caruins.config;

import generatormods.caruins.CAState;
import generatormods.common.config.ParseError;

import static generatormods.caruins.CAState.ALIVE;
import static generatormods.caruins.CAState.DEAD;

/**
 * CARule is used by the CARuins mod and can be used to generate towers for
 * great walls.
 */
public class CARule {

    private String ruleStr;
    private CAState[] birthRule;
    private CAState[] survivalRule;

    /**
     * Create a CARule. A configuration line contains a Birth number and a
     * survival number, like so:
     * <pre>
     *     B3/S23
     * </pre>
     */
    public CARule(String ruleStr) throws ParseError {
        this.ruleStr = ruleStr;
        parseRule(ruleStr);
    }

    public String toString() {
        return ruleStr;
    }

    private void parseRule(String str) throws ParseError {
        try {
            birthRule = new CAState[] {DEAD, DEAD, DEAD, DEAD, DEAD, DEAD, DEAD, DEAD, DEAD};
            survivalRule = new CAState[] {DEAD, DEAD, DEAD, DEAD, DEAD, DEAD, DEAD, DEAD, DEAD};
            String birthStr = str.split("/")[0].trim();
            String surviveStr = str.split("/")[1].trim();
            for (int n = 1; n < birthStr.length(); n++) {
                int digit = Integer.parseInt(birthStr.substring(n, n + 1));
                birthRule[digit] = ALIVE;
            }
            for (int n = 1; n < surviveStr.length(); n++) {
                int digit = Integer.parseInt(surviveStr.substring(n, n + 1));
                survivalRule[digit] = ALIVE;
            }
        } catch (Throwable e) {
            throw new ParseError("Error parsing \""+str+"\"", e);
        }
    }

    public CAState[] getBirthRule() {
        return birthRule;
    }

    public CAState[] getSurvivalRule() {
        return survivalRule;
    }

    public boolean isFourRule() {
        return birthRule[0] == DEAD && birthRule[1] == DEAD && birthRule[2] == DEAD
                && birthRule[3] == DEAD;
    }
}
