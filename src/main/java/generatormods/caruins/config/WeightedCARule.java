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

import generatormods.common.config.ParseError;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.util.WeightedRandom;

public class WeightedCARule extends WeightedRandom.Item {
    public final static List<WeightedCARule> DEFAULT_CA_RULES;
    static {
        try {
            DEFAULT_CA_RULES = new ArrayList<WeightedCARule>();
            // 3-rule
            DEFAULT_CA_RULES.add(new WeightedCARule("B3/S23", 5, "Life - good for weird temples"));
            DEFAULT_CA_RULES.add(new WeightedCARule("B36/S013468", 3, "pillars and hands"));
            DEFAULT_CA_RULES.add(new WeightedCARule("B367/S02347", 2,
                    "towers with interiors and chasms"));
            DEFAULT_CA_RULES.add(new WeightedCARule("B34/S2356", 3,
                    "towers with hetrogenous shapes"));
            DEFAULT_CA_RULES.add(new WeightedCARule("B368/S245", 8, "Morley - good hanging bits"));
            DEFAULT_CA_RULES.add(new WeightedCARule("B36/S125", 4,
                    "2x2 - pillar & arch temple/tower/statue"));
            DEFAULT_CA_RULES.add(new WeightedCARule("B36/S23", 4,
                    "High Life - space invaders, hanging arms."));
            DEFAULT_CA_RULES.add(new WeightedCARule("B3568/S148", 4, "fuzzy stilts"));
            DEFAULT_CA_RULES.add(new WeightedCARule("B3/S1245", 8, "complex"));
            DEFAULT_CA_RULES.add(new WeightedCARule("B3567/S13468", 5, "fat fuzzy"));
            DEFAULT_CA_RULES.add(new WeightedCARule("B356/S16", 5, "fuzzy with spurs"));
            DEFAULT_CA_RULES.add(new WeightedCARule("B3468/S123", 3, "towers with arches"));
            DEFAULT_CA_RULES.add(new WeightedCARule("B35678/S015678", 2, "checkerboard"));
            DEFAULT_CA_RULES.add(new WeightedCARule("B35678/S0156", 15, "spermatazoa"));
            // 2-rule
            DEFAULT_CA_RULES.add(new WeightedCARule("B26/S12368", 1, "mayan pyramid"));
            DEFAULT_CA_RULES.add(new WeightedCARule("B248/S45", 1, "gaudi pyramid"));
            DEFAULT_CA_RULES
                    .add(new WeightedCARule("B2457/S013458", 1, "complex interior pyramid"));
            // 4-rule
            DEFAULT_CA_RULES.add(new WeightedCARule("B45/S2345", 6, "45-rule - square towers"));
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private CARule rule;
    private int weight;
    private String comment;

    /**
     * Create a WeightedCARule from a line of configuration.
     *
     * A configuration line is a comma-separated list that looks like:
     *
     * B3/S23, 5, Life - good for weird temples
     *
     * The first value is the CARule, with Birth and Survival rule numbers. The second value is the
     * weight of the rule for randomly choosing a rule. The third value is an optional comment, and
     * it and may contain commas.
     */
    public static WeightedCARule fromString(String ruleLine) throws ParseError {
        try {
            String[] sections = ruleLine.trim().split(",", 3);
            CARule rule = new CARule(sections[0].trim());
            int weight = Integer.parseInt(sections[1].trim());
            String comment = sections.length > 2 ? sections[2].trim() : null;

            return new WeightedCARule(rule, weight, comment);
        } catch (NumberFormatException e) {
            throw new ParseError("Error parsing \"" + ruleLine + "\"", e);
        }
    }

    public WeightedCARule(String ruleStr, int weight, String comment) throws ParseError {
        this(new CARule(ruleStr), weight, comment);
    }

    public WeightedCARule(CARule rule, int weight, String comment) {
        super(weight);
        this.rule = rule;
        this.weight = weight;
        this.comment = comment;
    }

    public String toString() {
        return rule.toString() + ", " + weight
                + ((comment != null && comment.length() > 0) ? ", " + comment : "");
    }

    public CARule getRule() {
        return rule;
    }

    public int getWeight() {
        return weight;
    }
}
