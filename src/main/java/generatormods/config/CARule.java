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

import generatormods.BuildingCellularAutomaton;

public class CARule {
    public final static List<CARule> DEFAULT_CA_RULES;
    static {
        try {
            DEFAULT_CA_RULES = new ArrayList<CARule>();
            // 3-rule
            DEFAULT_CA_RULES.add(new CARule("B3/S23", 5, "Life - good for weird temples"));
            DEFAULT_CA_RULES.add(new CARule("B36/S013468", 3, "pillars and hands"));
            DEFAULT_CA_RULES.add(new CARule("B367/S02347", 2, "towers with interiors and chasms"));
            DEFAULT_CA_RULES.add(new CARule("B34/S2356", 3, "towers with hetrogenous shapes"));
            DEFAULT_CA_RULES.add(new CARule("B368/S245", 8, "Morley - good hanging bits"));
            DEFAULT_CA_RULES.add(new CARule("B36/S125", 4, "2x2 - pillar & arch temple/tower/statue"));
            DEFAULT_CA_RULES.add(new CARule("B36/S23", 4, "High Life - space invaders, hanging arms."));
            DEFAULT_CA_RULES.add(new CARule("B3568/S148", 4, "fuzzy stilts"));
            DEFAULT_CA_RULES.add(new CARule("B3/S1245", 8, "complex"));
            DEFAULT_CA_RULES.add(new CARule("B3567/S13468", 5, "fat fuzzy"));
            DEFAULT_CA_RULES.add(new CARule("B356/S16", 5, "fuzzy with spurs"));
            DEFAULT_CA_RULES.add(new CARule("B3468/S123", 3, "towers with arches"));
            DEFAULT_CA_RULES.add(new CARule("B35678/S015678", 2, "checkerboard"));
            DEFAULT_CA_RULES.add(new CARule("B35678/S0156", 15, "spermatazoa"));
            // 2-rule
            DEFAULT_CA_RULES.add(new CARule("B26/S12368", 1, "mayan pyramid"));
            DEFAULT_CA_RULES.add(new CARule("B248/S45", 1, "gaudi pyramid"));
            DEFAULT_CA_RULES.add(new CARule("B2457/S013458", 1, "complex interior pyramid"));
            // 4-rule
            DEFAULT_CA_RULES.add(new CARule("B45/S2345", 6, "45-rule - square towers"));
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private String ruleStr;
    private int weight;
    private String comment;
    private byte[][] rule;

    /**
     * Create a CARule from a line of configuration.
     *
     * A configuration line is a comma-separated list that looks like:
     * 
     *     B3/S23, 5, Life - good for weird temples
     *
     * The first value is the Cellular Automata rule, with Birth and Survival
     * rule numbers. The second value is the weight of the rule for randomly
     * choosing a rule. The third value is an optional comment, and it and may
     * contain commas.
     */
    public static CARule fromString(String ruleLine) throws ParseError {
        try {
            String[] sections = ruleLine.trim().split(",", 3);
            String rule = sections[0].trim();
            int weight = Integer.parseInt(sections[1].trim());
            String comment = sections.length > 2 ? sections[2].trim() : null;

            return new CARule(rule, weight, comment);
        } catch (NumberFormatException e) {
            throw new ParseError("Error parsing a CARule", e);
        }
    }

    public CARule(String ruleStr, int weight, String comment) throws ParseError {
        this.ruleStr = ruleStr;
        this.weight = weight;
        this.comment = comment;
        this.rule = parseRule(ruleStr);
    }

    public String toString() {
        return ruleStr + ", " + weight
                + ((comment != null && comment.length() > 0) ? ", " + comment : "");
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
            throw new ParseError("Error parsing a CARule", e);
        }
    }

    /**
     * Return the rule as 2 arrays of bytes to represent the birth rule and the
     * survival rule.
     */
    public byte[][] toBytes() {
        return rule;
    }

    public int getWeight() {
        return weight;
    }
}
