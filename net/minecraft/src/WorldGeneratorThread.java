package net.minecraft.src;
//By formivore 2011 for Minecraft Beta.

import java.util.Random;

public abstract class WorldGeneratorThread extends Thread {
	public final static int LAYOUT_CODE_NOCODE=-1,LAYOUT_CODE_EMPTY=0,LAYOUT_CODE_WALL=1, LAYOUT_CODE_AVENUE=2, LAYOUT_CODE_STREET=3, LAYOUT_CODE_TOWER=4, LAYOUT_CODE_TEMPLATE=5;
	protected final static int[][] LAYOUT_CODE_OVERRIDE_MATRIX=new int[][]{{0,1,1,1,1,1},
															    		   {0,0,0,0,0,0},
															    		   {0,0,1,1,0,0},
															    		   {0,0,1,1,1,0},
															    		   {0,0,0,0,0,0},
															    		   {0,0,0,0,0,0}};
	public final static char[] LAYOUT_CODE_TO_CHAR=new char[]{' ','#','=','-','@','&'};
	public volatile boolean threadSuspended=false;
	public boolean hasStarted=false;
	public boolean hasTerminated=false;
	
	public World world;
	public Random random;
	public int chunkI, chunkK, TriesPerChunk;
	public double ChunkTryProb;
	public BuildingExplorationHandler explorationHandler;
	private int min_spawn_height=0, max_spawn_height=127;
	public boolean spawn_surface=true;
	public boolean willBuild=false;
	
	//****************************  CONSTRUCTOR - WorldGeneratorThread *************************************************************************************//
	public WorldGeneratorThread(BuildingExplorationHandler beh_, World world_, Random random_, int chunkI_, int chunkK_, int TriesPerChunk_, double ChunkTryProb_){
		world=world_;
		random=random_;
		chunkI=chunkI_;
		chunkK=chunkK_;
		TriesPerChunk=TriesPerChunk_;
		ChunkTryProb=ChunkTryProb_;
		explorationHandler=beh_;
	}
	
	//****************************  FUNCTION - abstract and stub functions  *************************************************************************************//
	public abstract boolean generate(int i0,int j0,int k0) throws InterruptedException;
	
	public boolean isLayoutGenerator(){ return false; }
	public boolean layoutIsClear(int[] pt1, int[] pt2, int layoutCode){ return true; }
	public boolean layoutIsClear(Building building, boolean[][] templateLayout, int layoutCode){ return true; }
	public void setLayoutCode(int[] pt1, int[] pt2, int layoutCode) {}
	public void setLayoutCode(Building building, boolean[][] templateLayout, int layoutCode) {}
	
	//****************************  FUNCTION - run *************************************************************************************//
	public void run(){
		hasStarted=true;
		boolean success=false;
		int tries=0, j0=0, i0=0, k0=0;
		try{
			do{
				if(tries==0 || random.nextDouble()<ChunkTryProb){
					i0=chunkI+random.nextInt(16) + 8;
					k0=chunkK  + random.nextInt(16) + 8;
					if(spawn_surface){
						j0=Building.findSurfaceJ(world,i0,k0,127,true,true)+1;
					}else{
						j0=min_spawn_height+random.nextInt(max_spawn_height - min_spawn_height +1);
					}
					if(j0>0)
						success=generate(i0,j0,k0);
				}
				tries++;
			}while(!success && tries<TriesPerChunk && j0!=Building.HIT_WATER);
		} catch(InterruptedException e){ }
		
		synchronized(explorationHandler){
			hasTerminated=true;
			threadSuspended=true;
			explorationHandler.notifyAll();
		}
	}

	//****************************  FUNCTION - setSpawnHeight *************************************************************************************//
	public void setSpawnHeight(int min_spawn_height_, int max_spawn_height_, boolean spawn_surface_){
		min_spawn_height=min_spawn_height_;
		max_spawn_height=max_spawn_height_;
		spawn_surface=spawn_surface_;
	}
	
	//****************************  FUNCTION - exploreArea *************************************************************************************//
	public boolean exploreArea(int[] pt1, int[] pt2, boolean ignoreTerminate) throws InterruptedException{
		int incI=Building.signum(pt2[0]-pt1[0],0), incK=Building.signum(pt2[2]-pt1[2],0);
		for(int chunkI=pt1[0]>>4; ((pt2[0]>>4)-chunkI)*incI > 0; chunkI+=incI)
			for(int chunkK=pt1[2]>>4; ((pt2[2]>>4)-chunkK)*incK > 0; chunkK+=incK)
				if(!queryExplorationHandler(chunkI<<4, chunkK<<4) && !ignoreTerminate) return false;
		return true;
	}
	
	//****************************  FUNCTION - queryExplorationHandler *************************************************************************************//
	public boolean queryExplorationHandler(int i, int k) throws InterruptedException {
    	if(world.blockExists(i,0,k)) return true;
    	
    	//else this chunk does not exist
    	int threadAction=explorationHandler.queryChunk(i>>4, k>>4);
    	if(threadAction==BuildingExplorationHandler.THREAD_TERMINATE) return false;
    	if(threadAction==BuildingExplorationHandler.THREAD_SUSPEND){
    		//suspend this thread
    		suspendGen();
    	}
    	
    	//MP PORT
    	//world.getChunkProvider().loadChunk(i>>4, k>>4);
    	world.getBlockId(i,0,k); //force world to load this chunk
    	
    	return true;
    }
	
	//****************************  FUNCTION - suspendGen *************************************************************************************//
	public void suspendGen() throws InterruptedException{
		threadSuspended=true;
            synchronized(this) {
                while (threadSuspended){
                	synchronized(explorationHandler){
                		explorationHandler.notifyAll();
                	}
                	wait();
                }
            }
	}

}