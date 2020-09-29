import java.awt.Color;
import java.util.ArrayList;

import uchicago.src.sim.engine.BasicAction;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.engine.SimModelImpl;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.ColorMap;
import uchicago.src.sim.gui.Object2DDisplay;
import uchicago.src.sim.gui.Value2DDisplay;
import uchicago.src.sim.util.SimUtilities;

public class RabbitsGrassSimulationModel extends SimModelImpl {
  // Default Values
  private static final int NUMAGENTS = 100;
  private static final int GRIDSIZE = 20;
  //private static final int WORLDYSIZE = 40;
  private static final int TOTALMONEY = 1000;
 // private static final int AGENT_MIN_LIFESPAN = 30;
  //private static final int AGENT_MAX_LIFESPAN = 50;

  private int numAgents = NUMAGENTS;
  private int worldXSize = GRIDSIZE;
  private int worldYSize = GRIDSIZE;
  private int money = TOTALMONEY;
  //private int agentMinLifespan = AGENT_MIN_LIFESPAN;
  //private int agentMaxLifespan = AGENT_MAX_LIFESPAN;
  private int init_grass = 200;
  private int init_energy = 5;
  private int birthThreshold = 8;

  private Schedule schedule;

  private RabbitsGrassSimulationSpace rgSpace;

  private ArrayList agentList;

  private DisplaySurface displaySurf;


  public void setup(){
    System.out.println("Running setup");
    rgspace = null;
    agentList = new ArrayList();
    schedule = new Schedule(1);

    if (displaySurf != null){
      displaySurf.dispose();
    }
    displaySurf = null;

    displaySurf = new DisplaySurface(this, "Carry Drop Model Window 1");

    registerDisplaySurface("Carry Drop Model Window 1", displaySurf);
  }

  public void begin(){
    buildModel();
    buildSchedule();
    buildDisplay();
    displaySurf.display();
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
          if(rga.getEnergy()>=birthThreshold){		//Rabbits reproduction
        	  addNewAgent();
        	  rga.setEnergy(rga.getEnergy()-init_energy);
          }
        }

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

  }
  
  

  private void addNewAgent(){
    RabbitsGrassSimulationAgent a = new RabbitsGrassSimulationAgent(init_energy);
    agentList.add(a);
    rgSpace.addAgent(a);
  }

  public void reapDeadAgents() {			//Removes dead rabbits if its energy is 0
		for(int i = (agentList.size() - 1); i >= 0 ; i--){
	      RabbitsGrassSimulationAgent cda = agentList.get(i);
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

  public String[] getInitParam(){
    String[] initParams = { "NumAgents", "WorldXSize", "WorldYSize", "Money", "AgentMinLifespan", "AgentMaxLifespan"};
    return initParams;
  }

  public int getNumAgents(){
    return numAgents;
  }

  public void setNumAgents(int na){
    numAgents = na;
  }

  public int getWorldXSize(){
    return worldXSize;
  }

  public void setWorldXSize(int wxs){
    worldXSize = wxs;
  }

  public int getWorldYSize(){
    return worldYSize;
  }

  public void setWorldYSize(int wys){
    worldYSize = wys;
  }

  public int getMoney() {
    return money;
  }

  public void setMoney(int i) {
    money = i;
  }

  

  public static void main(String[] args) {
    SimInit init = new SimInit();
    CarryDropModel model = new CarryDropModel();
    init.loadModel(model, "", false);
  }

}