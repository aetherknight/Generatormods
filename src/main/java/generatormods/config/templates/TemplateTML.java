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
package generatormods.config.templates;

import generatormods.util.ParseUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.apache.logging.log4j.Logger;

/*
 * TemplateTML reads in a .tml file and defines a template.
 */
public class TemplateTML {
	public final static String NO_WATER_CHECK_STR = "NO_WATER_CHECK";
	public final static int NO_WATER_CHECK = -666;
	public final static int TML_CODE = 0, DEFAULT_TOWER_CODE = 1, CA_RUIN_CODE = 2;
	public final static int MAX_NO_BUILDOVER_LAYOUT_AREA = 120;
	public final static Exception ZERO_WEIGHT_EXCEPTION = new Exception("weight=0");
	public TemplateRule[] rules = null;
	public int[][][] template = null;
	public boolean[][] templateLayout = null;
	public boolean buildOverStreets = false;
	public HashMap<String, int[][]> namedLayers = null;
	public HashMap<String, String> extraOptions = null;
	public String name = "";
	protected boolean readInWaterHeight = false;
	public int templateTypeCode = TML_CODE;
	public int height = 0, length = 0, width = 0, weight = 1, embed = 1, leveling = 4, cutIn = 0, waterHeight = 3;
	//public int[] targets;
	//public int overhang = 0, primary=4, w_off=0, l_off=0, lbuffer =0;
	//public boolean preserveWater = false, preserveLava = false, preservePlants = false, unique = false;

    protected Logger logger;

    public TemplateTML(File file, Logger logger) throws Exception {
		// load in the given file as a template
		BufferedReader br;
		name = file.getName();
        this.logger = logger;
		ArrayList<String> lines = new ArrayList<String>();
		br = new BufferedReader(new FileReader(file));
		try {
			for (String read = br.readLine(); read != null; read = br.readLine())
				lines.add(read);
			br.close();
		} catch (IOException io) {
		}
		parseFile(lines);
        logger.debug("Successfully loaded template: {}, with weight: {}", name, weight);
	}

	//a dummy constructor for use by TemplateWall for default towers/ CARuins
	public TemplateTML(int code, int weight_) {
		templateTypeCode = code;
		weight = weight_;
	}

	//****************************  FUNCTION - setFixedRules *************************************************************************************//
	public void setFixedRules(Random random) {
		for (TemplateRule rule : rules)
			rule.setFixedRule(random);
	}

	//****************************  FUNCTION - parseFile *************************************************************************************//
	private void parseFile(ArrayList<String> lines) throws Exception {
		namedLayers = new HashMap<String, int[][]>();
		ArrayList<int[][]> layers = new ArrayList<int[][]>();
		ArrayList<TemplateRule> rulesArrayList = new ArrayList<TemplateRule>();
		extraOptions = new HashMap<String, String>();
		// the first rule added will always be the air block rule.
		rulesArrayList.add(TemplateRule.AIR_RULE);
		// now get the rest of the data
		Iterator<String> itr = lines.iterator();
		int layerN = 0;
		String line;
		while (itr.hasNext()) {
			String[] splitStr = itr.next().split("#");
			line = splitStr.length > 0 ? splitStr[0].trim() : "";
			if (line.startsWith("layer")) {
				//if layer has a label, put it in separate table. Otherwise add to main template.
				String[] layerData = line.split(":");
				if (layerData.length == 1) {
					layers.add(new int[length][width]);
					try {
						parseLayer(itr, layers.get(layers.size() - 1), true);
					} catch (Exception e) {
						throw new Exception("Error reading layer " + layerN, e);
					}
					layerN++;
				} else if (layerData.length == 2) {
					namedLayers.put(layerData[1], new int[length][width]);
					try {
						parseLayer(itr, namedLayers.get(layerData[1]), false);
					} catch (Exception e) {
						throw new Exception("Error reading layer \"" + layerData[1] + "\"", e);
					}
				}
			} else if (line.startsWith("rule")) {
				String[] parts = line.split("=");
				rulesArrayList.add(new TemplateRule(parts[1], true));
			} else if (line.startsWith("dimensions")) {
                Integer[] dim = ParseUtil.readIntList(logger, null, "=", line);
				if (dim == null || dim.length != 3)
					throw new Exception("Bad dimension input!" + line);
				height = dim[0];
				length = dim[1];
				width = dim[2];
			}
            //else if(line.startsWith("acceptable_target_blocks" )) targets=ParseUtil.readIntList(logger,targets,"=",line);
			else if (line.startsWith("weight")) {
                weight = ParseUtil.readIntParam(logger, weight, "=", line);
				if (weight <= 0)
					throw ZERO_WEIGHT_EXCEPTION;
			} else if (line.startsWith("embed_into_distance"))
                embed = ParseUtil.readIntParam(logger, embed, "=", line);
			else if (line.startsWith("max_cut_in"))
                cutIn = ParseUtil.readIntParam(logger, cutIn, "=", line);
			else if (line.startsWith("max_leveling"))
                leveling = ParseUtil.readIntParam(logger, leveling, "=", line);
			else if (line.startsWith("water_height")) {
				readInWaterHeight = true;
				if (line.contains(NO_WATER_CHECK_STR))
					waterHeight = NO_WATER_CHECK;
				else
                    waterHeight = ParseUtil.readIntParam(logger, waterHeight, "=", line);
			} else if (line.length() > 0) {
				String[] spl = line.split("=");
				if (spl.length == 2 && !spl[0].equals("") && !spl[1].equals(""))
					extraOptions.put(spl[0], line); //lazy - put line as value since we have a functions to parse
			}
		}
		waterHeight -= embed;
		if (layers.size() == 0)
			throw new Exception("No layers provided!");
		if (layers.size() != height) {
            logger.warn("Warning, number of layers provided: {} did not equal height: {}",
                    layers.size(), height);
			height = layers.size();
		}
		template = new int[height][length][width];
		template = layers.toArray(template);
		//convert rules to array and check that rules in template are OK
		rules = new TemplateRule[rulesArrayList.size()];
		rules = rulesArrayList.toArray(rules);
		for (int z = 0; z < height; z++)
			for (int x = 0; x < length; x++)
				for (int y = 0; y < width; y++)
					if (template[z][x][y] >= rules.length || template[z][x][y] < 0)
						throw new Exception("No rule provided for rule at (" + z + "," + x + "," + y + "): " + template[z][x][y] + " in template!");
	}

	//****************************  FUNCTION - parseLayer *************************************************************************************//
	private void parseLayer(Iterator<String> itr, int[][] layer, boolean isFixedLength) throws Exception {
		// add in data until we reach the end of the layer
		int lengthN = length - 1;
		for (String line = itr.next(); itr.hasNext() && !line.startsWith("endlayer"); line = itr.next()) {
			if (lengthN < 0)
				throw new Exception("# of lines in layer was > length=" + length + " specified in dimensions.");
			if (line.charAt(0) != '#') {
				String[] rowdata = line.split(",");
				if (rowdata.length != width)
					throw new Exception("\nlength of line \"" + line + "\" did not match width=" + width + " specified in dimensions.");
				for (int y = 0; y < width; y++)
					layer[lengthN][y] = Integer.parseInt(rowdata[y]);
			}
			lengthN--;
		}
		if (lengthN >= 0 && isFixedLength)
			throw new Exception("# of lines in layer was  < length=" + length + " specified in dimensions.");
	}

	//****************************  FUNCTION - buildLayout *************************************************************************************//
	public TemplateTML buildLayout() {
		int layoutLayer = embed >= 0 && embed < height ? embed : 0;
		int layoutArea = 0;
		templateLayout = new boolean[length][width];
		for (int y = 0; y < length; y++) {
			for (int x = 0; x < width; x++) {
				templateLayout[y][x] = !rules[template[layoutLayer][y][x]].isPreserveRule();
				if (templateLayout[y][x])
					layoutArea++;
			}
		}
		buildOverStreets = layoutArea > MAX_NO_BUILDOVER_LAYOUT_AREA;
		return this;
	}

	//****************************  FUNCTION - buildWeightsAndIndex *************************************************************************************//
	public static int[][] buildWeightsAndIndex(List<TemplateTML> buildings) throws Exception {
		int[][] weightsAndIndex = new int[2][buildings.size()];
		int sum = 0;
		TemplateTML temp;
		Iterator<TemplateTML> itr = buildings.iterator();
		for (int m = 0; itr.hasNext(); m++) {
			temp = itr.next();
			weightsAndIndex[0][m] = temp.weight;
			sum += temp.weight;
			weightsAndIndex[1][m] = m;
		}
		if (sum == 0)
			throw ZERO_WEIGHT_EXCEPTION;
		return weightsAndIndex;
	}

    // currently unused
    public String printableString() {
        String temp = "TEMPLATE - " + name;
		for (int z = 0; z < height; z++) {
			for (int y = 0; y < length; y++) {
				for (int x = 0; x < width; x++) {
					temp += template[z][x][length - y - 1] + ",";
				}
			}
			temp += "endlayer";
		}
        return temp;
    }
}
