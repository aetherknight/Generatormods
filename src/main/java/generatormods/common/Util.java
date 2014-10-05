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
package generatormods.common;

import generatormods.TemplateRule;
import generatormods.caruins.config.CARule;
import generatormods.config.ParseError;

import java.io.File;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.World;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.storage.ISaveHandler;

import org.apache.logging.log4j.Logger;

public class Util {
	public static List<byte[][]> readAutomataList(Logger logger, String splitString, String read) {
		List<byte[][]> rules = new ArrayList<byte[][]>();
		String[] ruleStrs = (read.split(splitString)[1]).split(",");
		for (String ruleStr : ruleStrs) {
            try {
                byte[][] rule = (new CARule(ruleStr.trim())).toBytes();
				rules.add(rule);
            } catch (ParseError e) {
                logger.error("Error parsing automaton rule " + ruleStr, e);
            }
		}
		if (rules.size() == 0)
			return null;
		return rules;
	}

	public static boolean readBooleanParam(Logger logger, boolean defaultVal, String splitString, String read) {
		try {
			defaultVal = Boolean.parseBoolean(read.split(splitString)[1].trim());
		} catch (NullPointerException e) {
			logger.error("Error parsing boolean, using default: " + defaultVal + " Line: " + read, e);
		}
		return defaultVal;
	}

	public static float readFloatParam(Logger logger, float defaultVal, String splitString, String read) {
		try {
			defaultVal = Float.parseFloat(read.split(splitString)[1].trim());
		} catch (Exception e) {
			logger.error("Error parsing double, using default: " + defaultVal + " Line: " + read, e);
		}
		return defaultVal;
	}

	public static Integer[] readIntList(Logger logger, Integer[] defaultVals, String splitString, String read) {
		try {
			String[] check = (read.split(splitString)[1]).split(",");
			Integer[] newVals = new Integer[check.length];
			for (int i = 0; i < check.length; i++) {
				newVals[i] = Integer.parseInt(check[i].trim());
			}
			return newVals;
		} catch (Exception e) {
			logger.error("Error parsing intlist, using defaults. Line: " + read, e);
		}
		return defaultVals;
	}

	public static int readIntParam(Logger logger, int defaultVal, String splitString, String read) {
		try {
			defaultVal = Integer.parseInt(read.split(splitString)[1].trim());
		} catch (NumberFormatException e) {
			logger.error("Error parsing int, using default: " + defaultVal + " Line: " + read, e);
		}
		return defaultVal;
	}

	public static int[] readNamedCheckList(Logger logger, int[] defaultVals, String splitString, String read, String[] names, String allStr) {
		if (defaultVals == null || names.length != defaultVals.length)
			defaultVals = new int[names.length];
		try {
			int[] newVals = new int[names.length];
			for (int i = 0; i < newVals.length; i++)
				newVals[i] = 0;
			if ((read.split(splitString)[1]).trim().equalsIgnoreCase(allStr)) {
				for (int i = 0; i < newVals.length; i++)
					newVals[i] = 1;
			} else {
				for (String check : (read.split(splitString)[1]).split(",")) {
					boolean found = false;
					for (int i = 0; i < names.length; i++) {
						if (names[i] != null && names[i].replaceAll("\\s", "").trim().equalsIgnoreCase(check.replaceAll("\\s", "").trim())) {
							found = true;
							newVals[i]++;
						}
					}
					if (!found)
						logger.warn("Warning, named checklist item not found:" + check + ". Line:" + read);
				}
			}
			return newVals;
		} catch (Exception e) {
			logger.error("Error parsing checklist, using defaults. Line: " + read, e);
		}
		return defaultVals;
	}

	//if an integer ruleId: try reading from rules and return.
	//If a rule: parse the rule, add it to rules, and return.
	public static TemplateRule readRuleIdOrRule(String splitString, String read, TemplateRule[] rules) throws Exception {
		String postSplit = read.split(splitString, 2)[1].trim();
		try {
			int ruleId = Integer.parseInt(postSplit);
			return rules[ruleId];
		} catch (NumberFormatException e) {
			return new TemplateRule(postSplit, false);
		} catch (Exception e) {
            throw new ParseError("Error reading block rule for variable. Line: " + read, e);
		}
	}

	public static File getWorldSaveDir(World world) {
		ISaveHandler worldSaver = world.getSaveHandler();
		if (worldSaver.getChunkLoader(world.provider) instanceof AnvilChunkLoader) {
			return ((AnvilChunkLoader) worldSaver.getChunkLoader(world.provider)).chunkSaveLocation;
		}
		return null;
	}
}
