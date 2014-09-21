/* Source code for the The Great Wall Mod and Walled City Generator Mods for the game Minecraft
 * Copyright (C) 2011 by formivore
 * Copyright (C) 2013-2014 by GotoLink
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
package generatormods;

import generatormods.config.CARuinsConfig;
import generatormods.config.CARule;
import generatormods.config.ChestContentsConfig;
import generatormods.config.ChestItemSpec;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

@Mod(modid = "CARuins", name = "Cellular Automata Generator", version = BuildingExplorationHandler.VERSION, dependencies = "after:ExtraBiomes,BiomesOPlenty", acceptableRemoteVersions = "*")
public class PopulatorCARuins extends BuildingExplorationHandler {
	@Instance("CARuins")
	public static PopulatorCARuins instance;

	public final static String[] SEED_TYPE_STRINGS = new String[] { "SymmetricSeedWeight", "LinearSeedWeight", "CircularSeedWeight", "CruciformSeedWeight" };
	public int[] seedTypeWeights = new int[] { 8, 2, 2, 1 };
	public float SymmetricSeedDensity = 0.5F;
	public int MinHeight = 20, MaxHeight = 70;
	public int ContainerWidth = 40, ContainerLength = 40;
	public int MinHeightBeforeOscillation = 12;
	public boolean SmoothWithStairs = true, MakeFloors = true;

    // blockRules index = biomeId +1
	public TemplateRule[] blockRules;// = new TemplateRule[DEFAULT_BLOCK_RULES.length];
	public TemplateRule[] spawnerRules = new TemplateRule[] { BuildingCellularAutomaton.DEFAULT_MEDIUM_LIGHT_NARROW_SPAWNER_RULE, BuildingCellularAutomaton.DEFAULT_MEDIUM_LIGHT_WIDE_SPAWNER_RULE,
			BuildingCellularAutomaton.DEFAULT_LOW_LIGHT_SPAWNER_RULE };
	ArrayList<byte[][]> caRules = null;
	int[][] caRulesWeightsAndIndex = null;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		logger = event.getModLog();
		settingsFileName = "CARuinsSettings.txt";
        if(event.getSourceFile().getName().endsWith(".jar") && event.getSide().isClient()){
            try {
                Class.forName("mods.mud.ModUpdateDetector").getDeclaredMethod("registerMod", ModContainer.class, String.class, String.class).invoke(null,
                        FMLCommonHandler.instance().findContainerFor(this),
                        "https://raw.github.com/GotoLink/Generatormods/master/update.xml",
                        "https://raw.github.com/GotoLink/Generatormods/master/changelog.md"
                );
            } catch (Throwable e) {
            }
        }
	}

	@EventHandler
	public void serverStarting(FMLServerStartingEvent event) {
		event.registerServerCommand(new CommandBuild());
        event.registerServerCommand(new CommandScan());
	}

	//****************************  FUNCTION - loadDataFiles *************************************************************************************//
	@Override
	public final void loadDataFiles() {
		try {
			initializeLogging("Loading options for the Cellular Automata Generator.");
            CARuinsConfig.initialize(CONFIG_DIRECTORY);
			getGlobalOptions();
			finalizeLoading(false, "ruin");
		} catch (Exception e) {
			errFlag = true;
			logOrPrint("There was a problem loading the Cellular Automata Generator: " + e.getMessage(), "SEVERE");
			lw.println("There was a problem loading the Cellular Automata Generator: " + e.getMessage());
			e.printStackTrace();
		} finally {
			if (lw != null)
				lw.close();
		}
		if (GlobalFrequency < 0.000001 || caRules == null || caRules.size() == 0)
			errFlag = true;
		dataFilesLoaded = true;
	}

	//****************************  FUNCTION - getGlobalOptions *************************************************************************************//
	@Override
	public void loadGlobalOptions(BufferedReader br) {
        GlobalFrequency = CARuinsConfig.globalFrequency;
        TriesPerChunk = CARuinsConfig.triesPerChunk;
        // AllowedDimensions is a List<Integer> instead of a point to an int[].
        for( int currInt : CARuinsConfig.allowedDimensions) {
            AllowedDimensions.add(new Integer(currInt));
        }
        logActivated = CARuinsConfig.logActivated;

        // CARuins-specific
        MinHeight = CARuinsConfig.minHeight;
        MaxHeight = CARuinsConfig.maxHeight;
        MinHeightBeforeOscillation = CARuinsConfig.minHeightBeforeOscillation;
        SmoothWithStairs = CARuinsConfig.smoothWithStairs;
        MakeFloors = CARuinsConfig.makeFloors;
        ContainerWidth = CARuinsConfig.containerWidth;
        ContainerLength = CARuinsConfig.containerLength;

        // Support the existing old chest format (array of values)
        for(Map.Entry<String, ChestContentsConfig> entry : CARuinsConfig.chestConfigs.entrySet()) {
            String chestType = entry.getKey();
            ChestContentsConfig chestSpec = entry.getValue();
            List<ChestItemSpec> chestItemList = chestSpec.getChestItems();
            Object[][] chestSpecOld = new Object[6][chestItemList.size()];
            for(int n = 0; n < chestItemList.size(); n++) {
                ChestItemSpec itemSpec = chestItemList.get(n);
                chestSpecOld[0][n] = n; // index
                chestSpecOld[1][n] = itemSpec.getBlockOrItem();
                chestSpecOld[2][n] = itemSpec.getMetadata();
                chestSpecOld[3][n] = itemSpec.getSelectionWeight();
                chestSpecOld[4][n] = itemSpec.getMinStackSize();
                chestSpecOld[5][n] = itemSpec.getMaxStackSize();
            }
            chestItems.put(chestType, chestSpecOld);
        }

        SymmetricSeedDensity = CARuinsConfig.symmetricSeedDensity;
        // Trusting that the indices here matches the constants.
        seedTypeWeights[0] = CARuinsConfig.symmetricSeedWeight;
        seedTypeWeights[1] = CARuinsConfig.linearSeedWeight;
        seedTypeWeights[2] = CARuinsConfig.circularSeedWeight;
        seedTypeWeights[3] = CARuinsConfig.cruciformSeedWeight;

        spawnerRules[0] = CARuinsConfig.mediumLightNarrowFloorSpawnerRule;
        spawnerRules[1] = CARuinsConfig.mediumLightWideFloorSpawnerRule;
        spawnerRules[2] = CARuinsConfig.lowLightSpawnerRule;

        blockRules = CARuinsConfig.blockRules;

        caRules = new ArrayList<byte[][]>();
        List<Integer> caRuleWeights = new ArrayList<Integer>();
        for(CARule caRule : CARuinsConfig.caRules) {
            caRules.add(caRule.toBytes());
            caRuleWeights.add(caRule.getWeight());
        }
        setRulesWeightAndIndex(caRuleWeights);
    }

	private void setRulesWeightAndIndex(List<Integer> caRuleWeights) {
		caRulesWeightsAndIndex = new int[2][caRuleWeights.size()];
		for (int m = 0; m < caRuleWeights.size(); m++) {
			caRulesWeightsAndIndex[0][m] = caRuleWeights.get(m);
			caRulesWeightsAndIndex[1][m] = m;
		}
	}

	@Override
	public void writeGlobalOptions(PrintWriter pw) {
		pw.println("Settings are now in CARuins.cfg");
	}

	//****************************  FUNCTION - generate *************************************************************************************//
	@Override
	public final void generate(World world, Random random, int i, int k) {
		if (random.nextFloat() < GlobalFrequency)
			(new WorldGenCARuins(this, world, random, i, k, TriesPerChunk, GlobalFrequency)).run();
	}

	@Override
	public String toString() {
		return "CARuins";
	}

	@EventHandler
	public void modsLoaded(FMLPostInitializationEvent event) {
		if (!dataFilesLoaded)
			loadDataFiles();
		if (!errFlag) {
			GameRegistry.registerWorldGenerator(this, 2);
		}
	}
}