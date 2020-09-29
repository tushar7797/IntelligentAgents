import java.awt.Color;

import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;
import uchicago.src.sim.space.Object2DGrid;

public class RabbitsGrassSimulationAgent implements Drawable{
  private int x;
  private int y;
  private int vX;
  private int vY;
  private int energy;
  //private int stepsToLive;
  private static int IDNumber = 0;
  private int ID;
  private RabbitsGrassSimulationSpace rgSpace;

  public RabbitsGrassSimulationAgent(int init_energy){
    x = -1;
    y = -1;
    energy = init_energy;
    setVxVy();
    IDNumber++;
    ID = IDNumber;
  }

  private void setVxVy(){
    vX = 0;
    vY = 0;
    while((vX == 0) && ( vY == 0)){
      vX = (int)Math.floor(Math.random() * 3) - 1;
      vY = (int)Math.floor(Math.random() * 3) - 1;
    }
  }

  public void setXY(int newX, int newY){
    x = newX;
    y = newY;
  }

  public void setRabbitsGrassSimulationSpace(RabbitsGrassSimulationSpace cds){
    rgSpace = cds;
  }

  public String getID(){
    return "A-" + ID;
  }

  public int getEnergy(){
    return energy;
  }
  
  public void setEnergy(int set_energy){
	    energy = set_energy;
	  }


  public void report(){
    System.out.println(getID() +
                       " at " +
                       x + ", " + y +
                       " has " +
                       getEnergy());
  }

  public int getX(){
    return x;
  }

  public int getY(){
    return y;
  }

  public void draw(SimGraphics G){
    if(energy > 0)
      G.drawFastRoundRect(Color.blue);
  }
  
  public void step() {		//Update status of rabbits after movement
		
		int newX = 0;
	    int newY = 0;
	    int count = 0;
		do{
			setVxVy();
			newX = x + vX;
		    newY = y + vY;
			Object2DGrid grid = rgSpace.getCurrentAgentSpace();
		    newX = (newX + grid.getSizeX()) % grid.getSizeX();
		    newY = (newY + grid.getSizeY()) % grid.getSizeY();
		    
		}while(!tryMove(newX, newY) && count < 50);
		
		if(count<50){
			x=newX;
			y=newY;
			energy += rgSpace.takeGrassAt(newX,newY);
		}
		energy--;
	}

  
  
  
  private boolean tryMove(int newX, int newY){
    return rgSpace.moveAgentAt(x, y, newX, newY);
  }


}