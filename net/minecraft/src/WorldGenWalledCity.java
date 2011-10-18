package net.minecraft.src;
/*
//  By formivore 2011 for Minecraft Beta.
//	Builds a walled city
 */

import java.util.Random;
import java.util.LinkedList;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.Math;

public class WorldGenWalledCity extends WorldGeneratorThread
{
	private final static int GATE_HEIGHT=6;
	private final static int JMEAN_DEVIATION_SLOPE=10;
	private final static int LEVELLING_DEVIATION_SLOPE=18;
	private final static int MIN_SIDE_LENGTH=10; //can be less than MIN_CITY_LENGTH due to squiggles

	private final static int[] DIR_GROUP_TO_DIR_CODE=new int[]{Building.DIR_NORTH,Building.DIR_EAST,Building.DIR_SOUTH,Building.DIR_WEST};

	//**** WORKING VARIABLES **** 
	private mod_WalledCity wc;
	private WallStyle ows, sws;
	private BuildingWall[] walls;
	private int axXHand;
	private int[] dir=null;
	private int Lmean, jmean;
	private int corner1[], corner2[], mincorner[];
	public int[][] layout;

	//****************************************  CONSTRUCTOR - WorldGenWalledCity  *************************************************************************************//
	public WorldGenWalledCity (mod_WalledCity wc_,World world_, Random random_, int chunkI_, int chunkK_, int TriesPerChunk_, double ChunkTryProb_) { 
		super(wc_, world_, random_, chunkI_, chunkK_, TriesPerChunk_, ChunkTryProb_);
		wc=wc_;
		setName("WorldGenWalledCityThread");
	}
	
	//****************************************  FUNCTION - generate  *************************************************************************************//
	@Override
	public boolean generate(int i0,int j0,int k0) throws InterruptedException{
		ows=WallStyle.pickBiomeWeightedWallStyle(wc.cityStyles,world,i0,k0,random,false);
		if(ows==null) return false;
		sws=WallStyle.pickBiomeWeightedWallStyle(ows.streets,world,i0,k0,random,false);
		if(sws==null) return false;
		if(!wc.cityIsSeparated(i0,k0,mod_WalledCity.CITY_TYPE_WALLED)) return false;
		if(ows.EndTowers) ows.EndTowers=false;
		
		int ID=(random.nextInt(9000)+1000)*100;
		int minJ=ows.LevelInterior ? Building.WORLD_HEIGHT/2 - 2 : BuildingWall.NO_MIN_J;
		//boolean circular=random.nextFloat() < ows.CircularProb;
		chooseDirection(i0 >> 4, k0 >>4);


		//==========================      outer walls    ====================================================
		if(ows.MinL < mod_WalledCity.MIN_CITY_LENGTH) ows.MinL=mod_WalledCity.MIN_CITY_LENGTH;
		walls=new BuildingWall[4];
		ows.setFixedRules(random);

		//plan walls[0]
		walls[0] = new BuildingWall(ID,this,ows,dir[0],axXHand,ows.MinL+random.nextInt(ows.MaxL-ows.MinL),false,i0,j0,k0).setMinJ(minJ);
		walls[0].plan(1,0,BuildingWall.DEFAULT_LOOKAHEAD,true);
		if(BuildingWall.DEBUG>1) System.out.println("Planning for "+walls[0].IDString()+" from "+walls[0].globalCoordString(0,0,0));
		if(walls[0].bLength<ows.MinL) return false;

		//plan walls[1]
		walls[0].setCursor(walls[0].bLength-1);
		walls[1] = new BuildingWall(ID+1,this,ows,dir[1],axXHand, ows.MinL+random.nextInt(ows.MaxL-ows.MinL),false,walls[0].getIJKPt(-1-ows.TowerXOffset,0,1+ows.TowerXOffset)).setTowers(walls[0]).setMinJ(minJ);
		if(!wc.cityIsSeparated(walls[1].i0,walls[1].k0,mod_WalledCity.CITY_TYPE_WALLED)) return false;
		walls[1].plan(1,0,BuildingWall.DEFAULT_LOOKAHEAD,false);
		if(BuildingWall.DEBUG>1) System.out.println("Planning for "+walls[1].IDString()+" from "+walls[1].globalCoordString(0,0,0));
		if(walls[1].bLength<ows.MinL) return false;

		//plan walls[2]
		walls[1].setCursor(walls[1].bLength-1);
		int distToTarget=walls[0].bLength + walls[1].xArray[walls[1].bLength-1];
		if(distToTarget<MIN_SIDE_LENGTH) return false;
		walls[2] = new BuildingWall(ID+2,this,ows,dir[2],axXHand,distToTarget+2,false,walls[1].getIJKPt(-1-ows.TowerXOffset,0,1+ows.TowerXOffset)).setTowers(walls[0]).setMinJ(minJ);
		if(!wc.cityIsSeparated(walls[2].i0,walls[2].k0,mod_WalledCity.CITY_TYPE_WALLED)) return false;
		walls[2].setCursor(0);
		walls[2].setTarget(walls[2].getIJKPt(0,0,distToTarget));
		walls[2].plan(1,0,BuildingWall.DEFAULT_LOOKAHEAD,false);
		if(BuildingWall.DEBUG>1) System.out.println("Planning for "+walls[2].IDString()+" from "+walls[2].globalCoordString(0,0,0));
		if(walls[2].bLength<walls[2].y_targ){
			if(BuildingWall.DEBUG>1) System.out.println("Abandoning on 3rd wall "+walls[2].IDString()+" planned length "+walls[2].bLength+" less than targeted length "+walls[2].y_targ+". Reason: "+walls[2].failString());
			return false;
		}

		//plan walls[3]
		walls[2].setCursor(walls[2].bLength-1);
		distToTarget=walls[1].bLength - walls[0].xArray[walls[0].bLength-1] + walls[1].xArray[walls[1].bLength-1];
		if(distToTarget<MIN_SIDE_LENGTH) return false;
		walls[3] = new BuildingWall(ID+3,this,ows,dir[3],axXHand,distToTarget+2,false,walls[2].getIJKPt(-1-ows.TowerXOffset,0,1+ows.TowerXOffset)).setTowers(walls[0]).setMinJ(minJ);
		if(!wc.cityIsSeparated(walls[3].i0,walls[3].k0,mod_WalledCity.CITY_TYPE_WALLED)) return false;
		walls[0].setCursor(0);
		walls[3].setCursor(0);
		walls[3].setTarget(walls[0].getIJKPt(-1-ows.TowerXOffset,0,-1-ows.TowerXOffset));
		if(BuildingWall.DEBUG>1) System.out.println("Planning for "+walls[3].IDString()+" from "+walls[3].globalCoordString(0,0,0));
		walls[3].plan(1,0,BuildingWall.DEFAULT_LOOKAHEAD,false);
		if(walls[3].bLength<walls[3].y_targ){
			if(BuildingWall.DEBUG>1)  System.out.println("Abandoning on 4th wall "+walls[3].IDString()+" planned length "+walls[3].bLength+" less than targeted "+walls[3].y_targ+". Reason: "+walls[3].failString());
			return false;
		}
		
		//smoothing
		for(BuildingWall w : walls) w.smooth(wc.Smooth1,wc.Smooth1,true);
		
		//======================= Additional site checks =======================================
		
		//calculate the corners
		int[] xmax=new int[4];
		for(int w=0;w<4;w++){
			xmax[w]=0;
			for(int n=0; n<walls[w].bLength; n++)
				if(walls[w].xArray[n]>xmax[w]) xmax[w]=walls[w].xArray[n];
		}
		for(BuildingWall w : walls) w.setCursor(0);
		corner1=walls[1].getIJKPt(xmax[1]+walls[1].bWidth+1, 0, walls[0].xArray[walls[0].bLength-1] - xmax[0] - walls[0].bWidth - 2);
		corner2=walls[3].getIJKPt(xmax[3]+walls[3].bWidth+1, 0, walls[2].xArray[walls[2].bLength-1] - xmax[2] - walls[2].bWidth - 2);
		mincorner=new int[]{Math.min(corner1[0], corner2[0]),0,Math.min(corner1[2], corner2[2])};
		
		//reject cities if too z-displaced at corners
		Lmean=(walls[0].bLength + walls[1].bLength + walls[2].bLength + walls[3].bLength)/4;
		jmean=0;
		for(BuildingWall w : walls) for(int n=0;n<w.bLength;n++) jmean+=w.zArray[n]+w.j0;
		jmean/=(Lmean*4);
		for(BuildingWall w : walls){
			if(Math.abs(w.j0 - jmean) > w.bLength/JMEAN_DEVIATION_SLOPE) {
				wc.logOrPrint("Rejected city "+ID+", height at corner differed from mean by "+(Math.abs(w.j0 - jmean))+".");
				return false;
			}
		}


		//Monte Carlo estimate of % of interior surface that is water, reject if too high.
		//Threshold set so enclosures will have <=25% water with 90% confidence.
		if(!ows.LevelInterior){
			int[] pt;
			int waterCount=0;
			for(int tries=0;tries<50; tries++){
				pt=randInteriorPoint();
				if(pt==null || pt[1]==-1 || pt[1]==Building.HIT_WATER) waterCount++;
			}
			if(waterCount>9) {
				wc.logOrPrint("Rejected city "+ID+", too much water! Sampled "+waterCount+" out of 50 water blocks.");
				return false;
			}
		}
		
		//query the exploration handler again to see if we've built nearby cities in the meanwhile
		for(BuildingWall w : walls){
			if(!wc.cityIsSeparated(w.i0,w.k0,mod_WalledCity.CITY_TYPE_WALLED)){
				wc.logOrPrint("Rejected city "+ID+" nearby city was built during planning!");
				return false;
			}
		}
		//We've passed all checks, register this city site
		walls[0].setCursor(0);
		int[] cityCenter=walls[0].getSurfaceIJKPt(-walls[1].bLength/2, walls[0].bLength/2,Building.WORLD_HEIGHT, false,false);
		for(int w=0;w<4;w++) wc.addCityLocation(walls[w].i0,walls[w].k0,mod_WalledCity.CITY_TYPE_WALLED);


		//=================================== Build it! =========================================
		exploreArea(corner1, corner2, true);
		willBuild=true;
		if(!explorationHandler.isFlushingGenThreads) suspendGen();
		
		wc.chatBuildingCity("\n***** Building "+ows.name+" city"+", ID="+ID+" between "+walls[0].globalCoordString(0,0,0)+" and "+walls[2].globalCoordString(0,0,0) + " ******\n");
		if(ows.LevelInterior) levelCity();
		
		WallStyle avenueWS=WallStyle.pickBiomeWeightedWallStyle(ows.streets,world,i0,k0,random,false);
		LinkedList<BuildingWall> interiorAvenues=new LinkedList<BuildingWall>();
		
		//layout
		layout=new int[Math.abs(corner1[0]-corner2[0])][Math.abs(corner1[2]-corner2[2])];
		for(int x=0;x<layout.length;x++) for(int y=0;y<layout[0].length;y++) layout[x][y]=LAYOUT_CODE_EMPTY;
		for(BuildingWall w : walls) w.setLayoutCode(LAYOUT_CODE_WALL);

		int gateFlankingTowers=0;
		for(BuildingWall w : walls){
			//build city walls
			if(BuildingWall.DEBUG > 1) w.printWall();
			w.endTLength=0;
			w.buildFromTML();
			BuildingWall[] avenues=w.buildGateway(w.bLength/4,3*w.bLength/4,GATE_HEIGHT,avenueWS.WWidth,avenueWS,random.nextInt(6)<gateFlankingTowers ? 0:axXHand,500,null,-axXHand,150,cityCenter,w.bDir>0 ? 1:-1);
			w.buildTowers(axXHand==-1,axXHand==1,true,false, false);
			if(w.gatewayStart!=BuildingWall.NO_GATEWAY) gateFlankingTowers++;

			
			//build avenues
			if(avenues!=null){
				avenues[0].buildFromTML();
				//avenues[1].buildFromTML(true);
				interiorAvenues.add(avenues[1]);
			}else {
				//no gateway on this city side, try just building an interior avenue from midpoint
				w.setCursor(w.bLength/2);
				BuildingWall midpointAvenue=new BuildingWall(0, this,sws,Building.rotateDir(w.bDir,-axXHand),w.bDir>0 ? 1:-1, ows.MaxL,false,w.getSurfaceIJKPt(-1, 0,Building.WORLD_HEIGHT, false,false));
				midpointAvenue.setTarget(cityCenter);
				midpointAvenue.plan(1,0,BuildingWall.DEFAULT_LOOKAHEAD,true);
				if(midpointAvenue.bLength > 20){
					System.out.println("Built a non-gateway avenue for dir="+w.bDir+" wall.");
					midpointAvenue.smooth(10,10,true);
					//midpointAvenue.buildFromTML(true);
					interiorAvenues.add(midpointAvenue);
				}
			}
		}
		
		

		//corner towers
		for(BuildingWall w : walls) w.setCursor(0);
		for(int w=0;w<4;w++){
			int zmean=(walls[w].zArray[2]-walls[w].j0+walls[(w+3)%4].zArray[walls[(w+3)%4].bLength-3]+walls[(w+3)%4].j0) / 2;
			int minCornerWidth=ows.WWidth+2+(ows.TowerXOffset < 0 ? 2*ows.TowerXOffset:0);
			int TWidth= ows.getTMaxWidth(walls[w].circular) < minCornerWidth ? minCornerWidth : ows.getTMaxWidth(walls[w].circular) ;
			BuildingTower tower=new BuildingTower(ID+10+w,walls[w], dir[(w+2)%4], -axXHand, TWidth, ows.getTMaxHeight(walls[w].circular), TWidth,
					                              walls[w].getIJKPt(-2-(ows.TowerXOffset < 0 ? ows.TowerXOffset:0),zmean,2));
			setLayoutCode(tower.getIJKPt(0,0,0),tower.getIJKPt(TWidth-1,0,TWidth-1),LAYOUT_CODE_TOWER);
			tower.build(0,0,true);
		}
		
		

		
		
		if(!explorationHandler.isFlushingGenThreads) suspendGen();
		//===============================================      streets   ===============================================
		//build avenues and cross avenues
		LinkedList<BuildingDoubleWall> branchAvenues=new LinkedList<BuildingDoubleWall>();
		int avInterval=ows.StreetDensity > 3*WallStyle.MAX_STREET_DENSITY/4 ? 60 : 35;
		for(BuildingWall avenue : interiorAvenues){
			for(int n=avenue.bLength-avInterval; n>=25; n-=avInterval){
				avenue.setCursor(n);
				BuildingDoubleWall crossAvenue=new BuildingDoubleWall(ID,this,sws,Building.rotateDir(avenue.bDir,Building.ROT_R),Building.R_HAND,avenue.getIJKPt(0,0,0));
				if(crossAvenue.plan())
					branchAvenues.add(crossAvenue);
			}
			avenue.buildFromTML();
			avenue.setLayoutCode(LAYOUT_CODE_AVENUE);
		}
		for(BuildingDoubleWall avenue : branchAvenues) avenue.build(LAYOUT_CODE_AVENUE);
		

		
		int maxTries=Lmean*ows.StreetDensity/12;
		//int maxAvenues=Lmean/8;
		LinkedList<BuildingDoubleWall> plannedStreets=new LinkedList<BuildingDoubleWall>();

		for(int tries=0;tries<maxTries; tries++){
			
			if(tries % 5==0 && !explorationHandler.isFlushingGenThreads) suspendGen();
			int[] pt=randInteriorPoint();
			sws=WallStyle.pickBiomeWeightedWallStyle(ows.streets,world,i0,k0,random,true);
			if(pt!=null && pt[1]!=-1){

				//streets
				BuildingDoubleWall street=new BuildingDoubleWall(ID+tries,this,sws,Building.pickDir(random),Building.R_HAND,pt);
				if(street.plan()) plannedStreets.add(street);
			}
		}
		for(BuildingDoubleWall street : plannedStreets) street.build(LAYOUT_CODE_STREET);
		
		//build towers
		for(BuildingWall avenue : interiorAvenues)
			avenue.buildTowers(true,true,false,ows.StreetDensity > WallStyle.MAX_STREET_DENSITY/2, true);
		for(BuildingDoubleWall avenue : branchAvenues)
			avenue.buildTowers(true,true,false,ows.StreetDensity > WallStyle.MAX_STREET_DENSITY/2, true);
		for(BuildingDoubleWall street : plannedStreets){
			if(!explorationHandler.isFlushingGenThreads) suspendGen();
			street.buildTowers(true,true,sws.GatehouseTowers,ows.StreetDensity > WallStyle.MAX_STREET_DENSITY/2, false);
		}
		
		
		wc.chatCityBuilt(new int[]{i0,j0,k0,mod_WalledCity.CITY_TYPE_WALLED,Lmean/2+40});
		
		//printLayout(new File("layout.txt"));
		
		return true;
	}
	

	
	
	//****************************************  FUNCTION - layoutIsClear *************************************************************************************//
	@Override
	public boolean isLayoutGenerator(){ return true; }
	
	@Override
	public boolean layoutIsClear(int[] pt1, int[] pt2, int layoutCode){
		for(int i=Math.min(pt1[0],pt2[0]); i<=Math.max(pt1[0],pt2[0]); i++)
			for(int k=Math.min(pt1[2],pt2[2]); k<=Math.max(pt1[2],pt2[2]); k++)
				if(i>=mincorner[0] && k>=mincorner[2] && i-mincorner[0]<layout.length && k-mincorner[2] < layout[0].length)
					if(LAYOUT_CODE_OVERRIDE_MATRIX[layout[i-mincorner[0]][k-mincorner[2]]][layoutCode]==0)
						return false;
		return true;
	}
	
	@Override
	public boolean layoutIsClear(Building building, boolean[][] templateLayout, int layoutCode){
		for(int y=0; y<templateLayout.length;y++){
    		for(int x=0; x<templateLayout[0].length;x++){
    			if(templateLayout[y][x]){
    				int i=building.getI(x,y), k=building.getK(x, y);
    				if(i>=mincorner[0] && k>=mincorner[2] && i-mincorner[0]<layout.length && k-mincorner[2] < layout[0].length)
    					if(LAYOUT_CODE_OVERRIDE_MATRIX[layout[i-mincorner[0]][k-mincorner[2]]][layoutCode]==0)
    						return false;
    	}}}
		return true;
	}
	
	//****************************************  FUNCTION - setLayoutCode *************************************************************************************//
	@Override
	public void setLayoutCode(int[] pt1, int[] pt2, int layoutCode){
		for(int i=Math.min(pt1[0],pt2[0]); i<=Math.max(pt1[0],pt2[0]); i++)
			for(int k=Math.min(pt1[2],pt2[2]); k<=Math.max(pt1[2],pt2[2]); k++)
				if(i>=mincorner[0] && k>=mincorner[2] && i-mincorner[0]<layout.length && k-mincorner[2] < layout[0].length)
					layout[i-mincorner[0]][k-mincorner[2]]=layoutCode;
	}
	
	@Override
	public void setLayoutCode(Building building, boolean[][] templateLayout, int layoutCode) {
		for(int y=0; y<templateLayout.length;y++){
    		for(int x=0; x<templateLayout[0].length;x++){
    			if(templateLayout[y][x]){
    				int i=building.getI(x,y), k=building.getK(x, y);
    				if(i>=mincorner[0] && k>=mincorner[2] && i-mincorner[0]<layout.length && k-mincorner[2] < layout[0].length)
    					layout[i-mincorner[0]][k-mincorner[2]]=layoutCode;
    	}}}	
	}
	
	//****************************************  FUNCTION - printLayout *************************************************************************************//
	private void printLayout(File f){
		try{
		 PrintWriter pw=new PrintWriter( new BufferedWriter( new FileWriter(f ) ));
		 pw.println("  +y   ");
		 pw.println("   ^   ");
		 pw.println("+x<.>-x");
		 pw.println("   v   ");
		 pw.println("  -y   ");
		 pw.println();
		 for(int y=layout[0].length-1;y>=0; y--){
			 for(int x=layout.length-1;x>=0;x--){
				 pw.print(LAYOUT_CODE_TO_CHAR[layout[x][y]]);
			 }
			 pw.println();
		 }
		 pw.close();
		}catch(Exception e){}
	}

	//****************************************  FUNCTION - randInteriorPoint  *************************************************************************************//
	//RETURNS: Coordinates (i,j,k) of interior surface point, j will be -1 if point was water
	private int[] randInteriorPoint(){
		int tries=0;
		int[] pt=new int[3];
		if(BuildingWall.DEBUG>1) System.out.println("Finding random interior point for city seeded at corner ("+walls[0].i0+","+walls[0].j0+","+walls[0].k0+")"+walls[0].IDString());
		while(tries < 20){
			pt[0]=mincorner[0] + random.nextInt( Math.abs(corner1[0]-corner2[0]));
			pt[2]=mincorner[2] + random.nextInt(Math.abs(corner1[2]-corner2[2]));
			pt[1]=Building.findSurfaceJ(world,pt[0],pt[2],127,true,true);
			boolean enclosed=true;
			for(BuildingWall w : walls) if(w.ptIsToXHand(pt,-sws.WWidth)) enclosed=false;
			if(enclosed) return pt;
			tries++;
		}
		System.out.println("Could not find point within bounds!");
		return null;
	}

	

	//****************************************  FUNCTION - levelCity  *************************************************************************************//
	//BUKKIT PORT
	//replace world.getBlockId( with world.getBlockTypeIdAt(
	//replace world.setBlock( with world.getBlockAt().setTypeId(
	//replace world.setBlockAndMetadata( with world.getBlockAt().setTypeIdAndData(
	
	private void levelCity() throws InterruptedException{
		for(BuildingWall w : walls) w.setCursor(0);
		int incI=Building.signum(corner2[0]-corner1[0],0), incK=Building.signum(corner2[2]-corner1[2],0);
		int[] pt=new int[3];
		int jmin=Building.isNether(world) ? jmean : Math.max(jmean, Building.WORLD_HEIGHT/2);
		for(BuildingWall w : walls){
			for(int n=0;n<w.bLength;n++)
				if(w.zArray[n]+w.j0+w.WalkHeight-1 < jmin && (Building.isNether(world) || jmin >= Building.WORLD_HEIGHT/2))
					jmin=w.zArray[n]+w.j0+w.WalkHeight-1;
		}
		int jmax=Math.max(jmean + Lmean/LEVELLING_DEVIATION_SLOPE, jmin);
		//int jmax=Math.max(jmean + walls[0].WalkHeight, jmin);
		
		for(pt[0]=corner1[0]; (corner2[0]-pt[0])*incI > 0; pt[0]+=incI){
			for(pt[2]=corner1[2]; (corner2[2]-pt[2])*incK > 0; pt[2]+=incK){
				boolean enclosed=true;
				for(BuildingWall w : walls) if(w.ptIsToXHand(pt,1)) enclosed=false;
				if(enclosed){
					pt[1]=Building.findSurfaceJ(world,pt[0],pt[2],127,false,true);
					int oldSurfaceBlockId=world.getBlockId(pt[0], pt[1], pt[2]);
					if(pt[1]>jmax) {
						while(world.getBlockId(pt[0],pt[1]+1,pt[2])!=Building.AIR_ID) pt[1]++; //go back up to grab any trees or whatnot
						for(; pt[1]>jmax; pt[1]--)
							Building.setBlockNoLighting(world,pt[0],pt[1],pt[2], Building.AIR_ID);
						if(world.getBlockId(pt[0],jmax-1,pt[2])!=Building.AIR_ID) 
							Building.setBlockNoLighting(world,pt[0],jmax,pt[2],oldSurfaceBlockId);
					}
					
					if(pt[1]<jmin) Building.fillDown(pt, jmin, world);
		}}}
		
		//update heightmap
		//BUKKIT PORT - comment out below block
		//MP PORT - replace generateheightMap(); with generateSkylightMap();
		for(int chunkI=corner1[0]>>4; ((corner2[0]>>4)-chunkI)*incI > 0; chunkI+=incI)
			for(int chunkK=corner1[2]>>4; ((corner2[2]>>4)-chunkK)*incK > 0; chunkK+=incK)
				world.getChunkFromChunkCoords(chunkI,chunkK).generateHeightMap();
		
		if(!explorationHandler.isFlushingGenThreads) suspendGen();
	}
	
	//****************************************  FUNCTION - chooseDirection *************************************************************************************//
	private void chooseDirection(int chunkI, int chunkK){	
		//BUKKIT PORT - comment out below block
		boolean[] exploredChunk = new boolean[4];
		exploredChunk[0]=world.blockExists((chunkI-1) << 4, 0, chunkK << 4); //North
		exploredChunk[1]=world.blockExists(chunkI << 4, 0, (chunkK-1) << 4); //East
		exploredChunk[2]=world.blockExists((chunkI+1) << 4, 0, chunkK << 4); //South
		exploredChunk[3]=world.blockExists(chunkI << 4, 0, (chunkK+1) << 4); //West

		//pick an explored direction if it exists
		int dir0=random.nextInt(4), dir1=(dir0+1)%4;
		//BUKKIT PORT - comment out below
		for(; dir1!=dir0; dir1=(dir1+1)%4) if(exploredChunk[dir1]) break;

		//Choose axXHand (careful it is opposite the turn direction of the square).
		//if RH direction explored, then turn RH; else turn LH;
		//BUKKIT PORT - comment out below
		//axXHand=2*random.nextInt(2)-1;
		axXHand= exploredChunk[(dir1+1)%4] ? -1 : 1;

		dir=new int[4];
		dir[0]=DIR_GROUP_TO_DIR_CODE[dir1];
		dir[1]=Building.rotateDir(dir[0],-axXHand);
		dir[2]=Building.rotateDir(dir[1],-axXHand);
		dir[3]=Building.rotateDir(dir[2],-axXHand);

	}

}









