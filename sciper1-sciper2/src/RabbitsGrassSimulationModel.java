import java.awt.Color;
import java.util.ArrayList;

import uchicago.src.sim.analysis.BinDataSource;
import uchicago.src.sim.analysis.DataSource;
import uchicago.src.sim.analysis.OpenSequenceGraph;
import uchicago.src.sim.analysis.Sequence;
import uchicago.src.sim.engine.BasicAction;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.engine.SimModelImpl;
import uchicago.src.sim.gui.ColorMap;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.Object2DDisplay;
import uchicago.src.sim.gui.Value2DDisplay;
import uchicago.src.sim.util.SimUtilities;

/**
 * Class that implements the simulation model for the rabbits grass
 * simulation.  This is the first class which needs to be setup in
 * order to run Repast simulation. It manages the entire RePast
 * environment and the simulation.
 *
 * @author 
 */


public class RabbitsGrassSimulationModel extends SimModelImpl {	
	
	private static final int NUMAGENTS = 10;
							 
	  private static final int GRIDSIZE = 20;
	  //private static final int WORLDYSIZE = 40;
	 // private static final int AGENT_MIN_LIFESPAN = 30;
	  //private static final int AGENT_MAX_LIFESPAN = 50;
	  private int GridSize = GRIDSIZE;
	  
	  private int worldXSize = GridSize;
	  private int worldYSize = GridSize;
	  
	  //private int agentMinLifespan = AGENT_MIN_LIFESPAN;
	  //private int agentMaxLifespan = AGENT_MAX_LIFESPAN;
	  private int NumInitGrass = 500;
	  private int init_grass = NumInitGrass ;
	  private int init_energy = 10;
	  private int BirthThreshold = 30;
	  private int GrassGrowthRate = 40;
	  private int reproduce_energy = 20;
	  private int NumInitRabbits = NUMAGENTS;
	  private int numAgents = NumInitRabbits;
	  private OpenSequenceGraph amountOfTotalEnergyInRabbit;	
	  private OpenSequenceGraph amountOfGrass;	

	  private Schedule schedule;

	  private RabbitsGrassSimulationSpace rgSpace;

	  private ArrayList agentList;

	  private DisplaySurface displaySurf;
	  

	  public void setup(){
	    System.out.println("Running setup");
	    rgSpace = null;
	    agentList = new ArrayList();
	    schedule = new Schedule(1);

	    if (displaySurf != null){
	      displaySurf.dispose();
	    }
	    displaySurf = null;

	    displaySurf = new DisplaySurface(this, "Carry Drop Model Window 1");

	    registerDisplaySurface("Carry Drop Model Window 1", displaySurf);
	    
	    if(amountOfTotalEnergyInRabbit != null){
			amountOfTotalEnergyInRabbit.dispose();
		}
	    amountOfTotalEnergyInRabbit = new OpenSequenceGraph("Total Rabbit Energy",this);
	    
	    if(amountOfGrass != null){
			amountOfGrass.display();
		}
		amountOfGrass = new OpenSequenceGraph("Grass Num", this);
	    
	    this.registerMediaProducer("Plot", amountOfTotalEnergyInRabbit);
	    this.registerMediaProducer("Plot", amountOfGrass);
	  }

	  public void begin(){
	    buildModel();
	    buildSchedule();
	    buildDisplay();
	    displaySurf.display();
	    amountOfTotalEnergyInRabbit.display();
	    amountOfGrass.display();
	    
	  }

	  public void buildModel(){
	    System.out.println("Running BuildModel");
	    rgSpace = new RabbitsGrassSimulationSpace(worldXSize, worldYSize);
	    rgSpace.growGrass(init_grass);

	    for(int i = 0; i < numAgents; i++){
	      addNewAgent();
	    }
	    for(int i = 0; i < agentList.size(); i++){
	      RabbitsGrassSimulationAgent rga = (RabbitsGrassSimulationAgent)agentList.get(i);
	      rga.report();
	    }
	  }

	  public void buildSchedule(){
	    System.out.println("Running BuildSchedule");

	    class RabbitGrassStep extends BasicAction {
	      public void execute() {
	        SimUtilities.shuffle(agentList);
	        for(int i =0; i < agentList.size(); i++){
	          RabbitsGrassSimulationAgent rga = (RabbitsGrassSimulationAgent)agentList.get(i);
	          rga.step();
	          if(rga.getEnergy()>=BirthThreshold){		//Rabbits reproduction
	        	  addNewAgent();
	        	  rga.setEnergy(rga.getEnergy()-reproduce_energy);
	          } 
	        }
	        reapDeadAgents();
	        rgSpace.growGrass(GrassGrowthRate);
	        displaySurf.updateDisplay();
	      }
	    }

	    schedule.scheduleActionBeginning(0, new RabbitGrassStep());

	    class RabbitGrassCountLiving extends BasicAction {
	      public void execute(){
	        countLivingAgents();
	      }
	    }

	    schedule.scheduleActionAtInterval(10, new RabbitGrassCountLiving());
	    
	    class RabbitUpdateEnergyInSpace extends BasicAction {
			public void execute(){
		        amountOfTotalEnergyInRabbit.step();
		      }
		 	}
	    schedule.scheduleActionAtInterval(10, new RabbitUpdateEnergyInSpace());
	    
	    class GrassUpdateNumInSpace extends BasicAction {
			public void execute(){
		        amountOfGrass.step();
		      }
		 	}
	    schedule.scheduleActionAtInterval(10, new GrassUpdateNumInSpace());
	  }

	  public void buildDisplay(){
	    System.out.println("Running BuildDisplay");

	    ColorMap map = new ColorMap();

	    for(int i = 1; i<16; i++){
	      map.mapColor(i, new Color(0, (int)(i * 8 + 127), 0));
	    }
	    map.mapColor(0, Color.white);

	    Value2DDisplay displayGrass =
	        new Value2DDisplay(rgSpace.getCurrentGrassSpace(), map);

	    Object2DDisplay displayAgents = new Object2DDisplay(rgSpace.getCurrentAgentSpace());
	    displayAgents.setObjectList(agentList);

	    displaySurf.addDisplayable(displayGrass, "Money");
	    displaySurf.addDisplayable(displayAgents, "Agents");

	    amountOfTotalEnergyInRabbit.addSequence("Total energy In Rabbits", new EnergyInSpace());
	    amountOfGrass.addSequence("Grass Num", new GrassNumInSpcace());
	  }
	  
	  class EnergyInSpace implements DataSource, Sequence {
			public Object execute() {
		      return new Double(getSValue());
		    }

		    @Override
			public double getSValue() {
		      int energy=0;
		      for(int i = 0; i < agentList.size(); i++){
			      RabbitsGrassSimulationAgent rga = (RabbitsGrassSimulationAgent)agentList.get(i);
			      energy = rga.getEnergy();
			    }
		      return agentList.size();
		    }
		  }
	  
	  class GrassNumInSpcace implements DataSource,Sequence{

			int totalgrass = 0;
			public double getSValue() {
				for(int x=0; x<GridSize; x++) {
					  for(int y=0; y<GridSize; y++) {
						  totalgrass += rgSpace.getGrassAt(x,y);
					  }
				
				}
				return totalgrass;
			}
			
		
			public Object execute() {
				return new Double(getSValue());
			}
			
	  }

	  private void addNewAgent(){
	    RabbitsGrassSimulationAgent a = new RabbitsGrassSimulationAgent(init_energy);
	    agentList.add(a);
	    rgSpace.addAgent(a);
	  }

	  public void reapDeadAgents() {			//Removes dead rabbits if its energy is 0
			for(int i = (agentList.size() - 1); i >= 0 ; i--){
		      RabbitsGrassSimulationAgent cda = (RabbitsGrassSimulationAgent) agentList.get(i);
		      if(cda.getEnergy() < 1){
		        rgSpace.removeAgentAt(cda.getX(), cda.getY());
		        agentList.remove(i);
		        //System.out.println("dead");
		      }
		    }
		}
	  
	  
	  
	  
	  private int countLivingAgents(){
	    int livingAgents = 0;
	    for(int i = 0; i < agentList.size(); i++){
	      RabbitsGrassSimulationAgent cda = (RabbitsGrassSimulationAgent)agentList.get(i);
	      if(cda.getEnergy() > 0) livingAgents++;
	    }
	    System.out.println("Number of living agents/rabbits is: " + livingAgents);

	    return livingAgents;
	  }

	  public Schedule getSchedule(){
	    return schedule;
	  }


		public static void main(String[] args) {
			
			System.out.println("Rabbit skeleton");

			SimInit init = new SimInit();
			RabbitsGrassSimulationModel model = new RabbitsGrassSimulationModel();
			// Do "not" modify the following lines of parsing arguments
			if (args.length == 0) // by default, you don't use parameter file nor batch mode 
				init.loadModel(model, "", false);
			else
				init.loadModel(model, args[0], Boolean.parseBoolean(args[1]));
			
		}


		public String[] getInitParam() {
			// TODO Auto-generated method stub
			// Parameters to be set by users via the Repast UI slider bar
			// Do "not" modify the parameters names provided in the skeleton code, you can add more if you want 
			String[] params = { "GridSize", "NumInitRabbits", "NumInitGrass", "GrassGrowthRate", "BirthThreshold"};
			return params;
		}

		public String getName() {
			// TODO Auto-generated method stub
			return null;
		}
		
}

		

		

