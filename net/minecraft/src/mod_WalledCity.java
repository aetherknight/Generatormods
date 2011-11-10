package net.minecraft.src;
/*
 *  Source code for the The Great Wall Mod and Walled City Generator Mods for the game Minecraft
 *  Copyright (C) 2011 by formivore

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * mod_WalledCity is the main class that hooks into ModLoader for the Walled City Mod.
 * It reads the globalSettings file, keeps track of city locations, and runs WorldGenWalledCitys and WorldGenUndergroundCities.
 */

import java.util.Random;
import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

//MP PORT
//import net.minecraft.server.MinecraftServer;
//import java.util.logging.Logger;
import net.minecraft.client.Minecraft;

//BUKKIT PORT
//public class mod_WalledCity extends BlockPopulator implements IBuildingExplorationHandler

public class mod_WalledCity extends BuildingExplorationHandler
{
	public final static int MIN_CITY_LENGTH=40;
	private final static int MAX_EXPLORATION_DISTANCE=30;
	public final static File SETTINGS_FILE, STYLES_DIRECTORY,STREETS_DIRECTORY, LOG_FILE;
	public final static int CITY_TYPE_WALLED=0, CITY_TYPE_UNDERGROUND=1;
	static{
		//BUKKIT PORT / MP PORT
		//File baseDir=new File(".");
		File baseDir=Minecraft.getMinecraftDir();
		SETTINGS_FILE=new File(baseDir,"WalledCitySettings.txt");
		LOG_FILE=new File(baseDir,"walled_city_log.txt");
		STYLES_DIRECTORY=new File(new File(baseDir,"resources"),"walledcity");
		STREETS_DIRECTORY=new File(STYLES_DIRECTORY,"streets");
	}

	//USER MODIFIABLE PARAMETERS, values here are defaults
	public float GlobalFrequency=0.05F, UndergroundGlobalFrequency=0.005F;
	public int TriesPerChunk=1;
	public int MinCitySeparation=500, UndergroundMinCitySeparation=250;
	public boolean CityBuiltMessage=true;

	//DATA VARIABLES
	public ArrayList<TemplateWall> cityStyles=null, undergroundCityStyles=new ArrayList<TemplateWall>();
	private long explrWorldCode;
	private ArrayList<int[]> cityLocations, undergroundCityLocations;
	private HashMap<Long,ArrayList<int[]> > worldCityLocationsMap=new HashMap<Long,ArrayList<int[]> >(),
											undergroundWorldCityLocationsMap=new HashMap<Long,ArrayList<int[]> >();
	public LinkedList<int[]> citiesBuiltMessages=new LinkedList<int[]>();
	//private LinkedList<WorldGeneratorThread> exploreThreads=new LinkedList<WorldGeneratorThread>();
	
	//BUKKIT PORT / MP PORT - uncomment
	//public static Logger logger=MinecraftServer.logger;

	//****************************  CONSTRUCTOR - mod_WalledCity  *************************************************************************************//
	public mod_WalledCity() {
		ModLoader.SetInGameHook(this,true,true);
		loadingMessage="Generating cities";
		max_exploration_distance=MAX_EXPLORATION_DISTANCE;
		
		//MP PORT - uncomment
		//loadDataFiles();
	}
	
	@Override
	public String toString(){
		return WALLED_CITY_MOD_STRING;
	}
	
	//****************************  FUNCTION - ModsLoaded *************************************************************************************//
	//Load templates after mods have loaded so we can check whether any modded blockIDs are valid
	public void ModsLoaded(){
		if(!dataFilesLoaded){
			initializeHumansPlusReflection();
		 	loadDataFiles();
		}
	}
	
	//****************************  FUNCTION - loadDataFiles *************************************************************************************//
	public void loadDataFiles(){
		try {
			//read and check values from file
			lw= new PrintWriter( new BufferedWriter( new FileWriter(LOG_FILE ) ));
			
			logOrPrint("Loading options and templates for the Walled City Generator.");
			getGlobalOptions();
			

			cityStyles=TemplateWall.loadWallStylesFromDir(STYLES_DIRECTORY,this);
			TemplateWall.loadStreets(cityStyles,STREETS_DIRECTORY,this);
			for(int m=0; m<cityStyles.size(); m++){
				if(cityStyles.get(m).underground){
					TemplateWall uws = cityStyles.remove(m);
					uws.streets.add(uws); //underground cities have no outer walls, so this should be a street style
					undergroundCityStyles.add(uws);
					m--;
			}}

			lw.println("\nTemplate loading complete.");
			lw.println("Probability of generation attempt per chunk explored is "+GlobalFrequency+", with "+TriesPerChunk+" tries per chunk.");
			if(GlobalFrequency <0.000001 && UndergroundGlobalFrequency<0.000001) errFlag=true;
		} catch( Exception e ) {
			errFlag=true;
			lw.println( "There was a problem loading the walled city mod: "+e.getMessage() );
			logOrPrint( "There was a problem loading the walled city mod: "+e.getMessage() );
			e.printStackTrace();
		}finally{ if(lw!=null) lw.close(); }

		dataFilesLoaded=true;
	}
	
	//****************************  FUNCTION - cityIsSeparated *************************************************************************************//
	public boolean cityIsSeparated(int i, int k, int cityType){
		ArrayList<int[]> locations = cityType==CITY_TYPE_WALLED ? cityLocations : undergroundCityLocations;
		if(locations ==null) 
			return true;
		for(int [] cityPt : locations ){
			if( Math.abs(cityPt[0]-i) + Math.abs(cityPt[1]-k) < (cityType==CITY_TYPE_WALLED ?  MinCitySeparation : UndergroundMinCitySeparation)){
				return false;
			}
		}
		return true;
	}
	
	//****************************  FUNCTION - addCityLocation *************************************************************************************//
	public void addCityLocation(int i, int k, int cityType){
		if(cityType==CITY_TYPE_UNDERGROUND) undergroundCityLocations.add(new int[]{i,k});
		else cityLocations.add(new int[]{i,k});
	}

	//****************************  FUNCTION - updateWorldExplored *************************************************************************************//
	public void updateWorldExplored(World world_) {
		if(Building.getWorldCode(world_)!=explrWorldCode){
			world=world_;
 			explrWorldCode=Building.getWorldCode(world);
			chunksExploredThisTick=0;
			chunksExploredFromStart=0;
			if(world.isNewWorld && world.worldInfo.getWorldTime()==0){
				isCreatingDefaultChunks=true;
			}
			logOrPrint("Starting to survey a world for city generation...");
			
			//kill zombies
			for(WorldGeneratorThread wgt: exploreThreads) killZombie(wgt);
			exploreThreads=new LinkedList<WorldGeneratorThread>();
			
			if(!worldCityLocationsMap.containsKey(explrWorldCode)){
				worldCityLocationsMap.put(explrWorldCode,new ArrayList<int[]>());
				undergroundWorldCityLocationsMap.put(explrWorldCode,new ArrayList<int[]>());
			}
			cityLocations=worldCityLocationsMap.get(explrWorldCode);
			undergroundCityLocations=undergroundWorldCityLocationsMap.get(explrWorldCode);
		}
	}
	
	//****************************  FUNCTION - isGeneratorStillValid *************************************************************************************//
	public boolean isGeneratorStillValid(WorldGeneratorThread wgt){
		return cityIsSeparated(wgt.chunkI,wgt.chunkK,wgt.spawn_surface ? CITY_TYPE_WALLED : CITY_TYPE_UNDERGROUND);
	}
	
	
	//****************************  FUNCTION - chatCityBuilt *************************************************************************************//
	//BUKKIT PORT / MP PORT
	//public void chatBuildingCity(String msg){ if(msg!=null) logOrPrint(msg); }
	//public void chatCityBuilt(int[] args){}
	
	public void chatBuildingCity(String msg){
		if(msg!=null) logOrPrint(msg);
		if(CityBuiltMessage && mc.thePlayer!=null){
			mc.thePlayer.addChatMessage("** Building city... **");
		}
	}
	
	public void chatCityBuilt(int[] args){
		if(!CityBuiltMessage) return;
		
		if(mc.thePlayer==null){
			citiesBuiltMessages.add(args);
		}else{
			String dirStr="";
			int dI=args[0] - (int)mc.thePlayer.posX;
			int dK=args[2] - (int)mc.thePlayer.posZ;
			if(dI*dI+dK*dK < args[4]*args[4]){
				dirStr="nearby";
			}
			dirStr="to the ";
			if(Math.abs(dI)>2*Math.abs(dK)) dirStr+= dI>0 ? "south" : "north";
			else if(Math.abs(dK)>2*Math.abs(dI)) dirStr+= dK>0 ? "west" : "east";
			else dirStr+= dI > 0 ? (dK>0 ? "southwest" : "southeast") : (dK>0 ? "northwest" : "northeast");

			mc.thePlayer.addChatMessage("** Built city "+dirStr+" ("+args[0]+","+args[1]+","+args[2]+")! **");
		}
	}
	
	


	//****************************  FUNCTION - GenerateSurface  *************************************************************************************//
	//BUKKIT PORT
	//public void populate(World world, Random random, Chunk source){
	//	int chunkI=source.getX(), chunkK=source.getZ();
	public void GenerateSurface( World world, Random random, int i, int k ) {
		if(errFlag) return;
		updateWorldExplored(world);
		chunksExploredFromStart++;

		
		//BUKKIT PORT / MP PORT - Comment out below block
		if(CityBuiltMessage && mc.thePlayer!=null)
			while(citiesBuiltMessages.size()>0) 
				chatCityBuilt(citiesBuiltMessages.remove());

		//Put flushGenThreads before the exploreThreads enqueues and include the callChunk argument.
		//This is to avoid putting mineral deposits in cities etc.
		if(isCreatingDefaultChunks) flushGenThreads(new int[]{i,k});
		
		if(cityStyles.size() > 0 && cityIsSeparated(i,k,CITY_TYPE_WALLED) && random.nextFloat() < GlobalFrequency){
			exploreThreads.add(new WorldGenWalledCity(this, world, random, i, k,TriesPerChunk, GlobalFrequency));
		}
		if(undergroundCityStyles.size() > 0 && cityIsSeparated(i,k,CITY_TYPE_UNDERGROUND) && random.nextFloat() < UndergroundGlobalFrequency){
			WorldGeneratorThread wgt=new WorldGenUndergroundCity(this, world, random, i, k,1, UndergroundGlobalFrequency);
			int j=Building.findSurfaceJ(world,i,k,127,false,false)- WorldGenUndergroundCity.MAX_DIAM/2 - 5;
			wgt.setSpawnHeight(j-WorldGenUndergroundCity.MAX_DIAM, j, false);
			exploreThreads.add(wgt);
		}
	}
	
	public void GenerateNether( World world, Random random, int chunkI, int chunkK ) {
		GenerateSurface(world,random,chunkI,chunkK);
	}
	
	//****************************  FUNCTION - OnTickInGame  *************************************************************************************//
	//MP Port
	//public boolean OnTickInGame() {
	@Override
	public boolean OnTickInGame(float tick, net.minecraft.client.Minecraft game) {
		//if(exploreThreads.size()==0) doQueuedLighting();
		flushGenThreads(NO_CALL_CHUNK);
		runWorldGenThreads();	
		return true;
	}
	

	
	//****************************  FUNCTION - getGlobalOptions  *************************************************************************************//
	public void getGlobalOptions() {
		if(SETTINGS_FILE.exists()){
			BufferedReader br = null;
			try{
				br=new BufferedReader( new FileReader(SETTINGS_FILE) );
				String read = br.readLine();  
				lw.println("Getting global options...");    
		
				while( read != null ) {
		
					//outer wall parameters
					if(read.startsWith( "GlobalFrequency" )) GlobalFrequency = TemplateWall.readFloatParam(lw,GlobalFrequency,":",read);
					if(read.startsWith( "UndergroundGlobalFrequency" )) UndergroundGlobalFrequency = TemplateWall.readFloatParam(lw,UndergroundGlobalFrequency,":",read);
					if(read.startsWith( "TriesPerChunk" )) TriesPerChunk = TemplateWall.readIntParam(lw,TriesPerChunk,":",read);
					if(read.startsWith( "MinCitySeparation" )) MinCitySeparation= TemplateWall.readIntParam(lw,MinCitySeparation,":",read);
					if(read.startsWith( "MinUndergroundCitySeparation" )) UndergroundMinCitySeparation= TemplateWall.readIntParam(lw,UndergroundMinCitySeparation,":",read);
		
					if(read.startsWith( "ConcaveSmoothingScale" )) ConcaveSmoothingScale = TemplateWall.readIntParam(lw,ConcaveSmoothingScale,":",read);
					if(read.startsWith( "ConvexSmoothingScale" )) ConvexSmoothingScale = TemplateWall.readIntParam(lw,ConvexSmoothingScale,":",read);
					if(read.startsWith( "BacktrackLength" )) BacktrackLength = TemplateWall.readIntParam(lw,BacktrackLength,":",read);
					if(read.startsWith( "CityBuiltMessage" )) CityBuiltMessage = TemplateWall.readIntParam(lw,1,":",read)==1;
					
					readChestItemsList(lw,read,br);
		
					read = br.readLine();
				}
				if(TriesPerChunk > MAX_TRIES_PER_CHUNK) TriesPerChunk = MAX_TRIES_PER_CHUNK;
			}catch(IOException e) { lw.println(e.getMessage()); }
			finally{ try{ if(br!=null) br.close();} catch(IOException e) {} }
		}else{
			copyDefaultChestItems();
			PrintWriter pw=null;
			try{
				pw=new PrintWriter( new BufferedWriter( new FileWriter(SETTINGS_FILE) ) );
				pw.println("<-README: put this file in the main minecraft folder->");
				pw.println();
				pw.println("<-GlobalFrequency/UndergroundGlobalFrequency controls how likely aboveground/belowground cities are to appear. Should be between 0.0 and 1.0. Lower to make less common->");
				pw.println("<-MinCitySeparation/UndergroundMinCitySeparation define a minimum allowable separation between city spawns.->");
				pw.println("<-CityBuiltMessage controls whether the player receives message when a city is building. Set to 1 to receive message, 0 for no messages.->");
				pw.println("GlobalFrequency:"+GlobalFrequency);
				pw.println("UndergroundGlobalFrequency:"+UndergroundGlobalFrequency);
				pw.println("MinCitySeparation:"+MinCitySeparation);
				pw.println("MinUndergroundCitySeparation:"+UndergroundMinCitySeparation);
				pw.println("CityBuiltMessage:"+(CityBuiltMessage ? 1:0));
				pw.println();
				pw.println("<-Wall Pathfinding->");
				pw.println("<-ConcaveSmoothingScale and ConvexSmoothingScale specifiy the maximum length that can be smoothed away in walls for cocave/convex curves respectively.->");
				pw.println("<-BacktrackLength - length of backtracking for wall planning if a dead end is hit->");
				pw.println("ConcaveSmoothingScale:"+ConcaveSmoothingScale);
				pw.println("ConvexSmoothingScale:"+ConvexSmoothingScale);
				pw.println("BacktrackLength:"+BacktrackLength);
				pw.println();
				pw.println();
				pw.println("<-Chest contents->");
				pw.println("<-Tries is the number of selections that will be made for this chest type.->");
				pw.println("<-Format for items is <itemID>,<selection weight>,<min stack size>,<max stack size> ->");
				pw.println("<-So e.g. 262,1,1,12 means a stack of between 1 and 12 arrows, with a selection weight of 1.->");
				printDefaultChestItems(pw);
			}catch(IOException e) { lw.println(e.getMessage()); }
			finally{ if(pw!=null) pw.close(); }
		}
	}


}



