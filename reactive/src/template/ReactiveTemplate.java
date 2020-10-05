package template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import logist.agent.Agent;
import logist.behavior.ReactiveBehavior;
import logist.plan.Action;
import logist.plan.Action.Move;
import logist.plan.Action.Pickup;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.topology.Topology;
import logist.topology.Topology.City;

public class ReactiveTemplate implements ReactiveBehavior {

	// Potential table; access it through get/setPotential
	public HashMap<State, Double> V = new HashMap<State, Double>(); // V is the Potential of a state

	// Action table; best move for state/potential; use getPreferableAction
	public HashMap<State, City> best_action_table = new HashMap<State, City>(); 

	// For reward per action ration computation
	private Agent agent;
	private int counterSteps = 0;

	// Setup transition table for our Reactive Agent
	@Override
	public void setup(Topology topology, TaskDistribution td, Agent agent) {
		// Reads the gamma factor from the agents.xml file.
		// If the property is not present it defaults to 0.95
		Double discount = agent.readProperty("discount-factor", Double.class, 0.75);
		

		List<City> tasks = new ArrayList<Topology.City>(topology.cities());
		tasks.add(null); // The "no task" state
		Vehicle vehicle = agent.vehicles().get(0);
		// System.out.println(tasks);
		boolean change = false;
		int count = 0;
		do {

			change = false;
			count = count+1;
			// For all states
			for (City city : topology) {
				for (City task : tasks) {
					
					if (city.equals(task))
						continue;
					
					State state = new State(city, task);
					
					double best_val = 0;
					City best_action = null;
					Set<City> legal_destinations = getLegalDestinations(state);
					//System.out.println(legal_destinations);
					for (City action:legal_destinations) {
					
						double q_sa = reward(state, action, vehicle, td);
						
						for (City tasks_2 : tasks) {
							State state_new = new State(action, tasks_2);
							q_sa = q_sa + discount*transitionProbability(state, action, state_new, td)*V.getOrDefault(state_new, 0.0);
						}
						
						if (q_sa > best_val) {
							best_val = q_sa;
							best_action = action;
						}
					}
					if (V.getOrDefault(state, 0.0) != best_val) {
						//System.out.println("change");
						V.put(state, best_val);
						best_action_table.put(state, best_action);
						change = true;
					}
				
				}
			}

		} while (change);
		System.out.println(count);
		this.agent = agent;
		System.out.println(best_action_table);
	}

	@Override
	public Action act(Vehicle vehicle, Task availableTask) {
		
		if ((counterSteps > 0) && (counterSteps % 100 == 0)) {
			System.out.println("The total profit after " + counterSteps + " steps is " + agent.getTotalProfit() + ".");
			System.out.println("The profit per action after " + counterSteps + " steps is "
					+ ((double) agent.getTotalProfit() / counterSteps) + ".");
		}
		counterSteps++;

		State state = new State(vehicle.getCurrentCity(), availableTask == null ? null : availableTask.deliveryCity);

		// Choose best action
		City destination = best_action_table.get(state);

		// If the destination and the task's destination match, take the task
		if (destination.equals(state.task)) {
			return new Pickup(availableTask);
		} else {
			return new Move(destination);
		}
	}


	// Compute the probability to be in `stateP` after taking `action` from `state`
	private double transitionProbability(State state, City action, State stateP, TaskDistribution td) {
		if (state.city.equals(action) || state.city.equals(state.task) || state.city.equals(stateP.city)
				|| !stateP.city.equals(action)) {
			return 0.0;
		} else {
			return td.probability(stateP.city, stateP.task);
		}
	}

	// From the current city we can go to any neighbors or, if any, the destination of the task
	// Using `Set` to avoid duplicates with task destination
	public Set<City> getLegalDestinations(State state) {
		Set<City> dests = new HashSet<Topology.City>();

		dests.addAll(state.city.neighbors());
		if (state.task != null) {
			dests.add(state.task);
		}

		return dests;
	}

	// Compute reward for the given state-action pair
	private double reward(State state, City action, Vehicle vehicle, TaskDistribution td) {
		double win = 0;

		// If we have a task with us, we can get some candy!
		if (action.equals(state.task)) {
			win = td.reward(state.city, action);
		}

		double lost = state.city.distanceTo(action) * vehicle.costPerKm();

		return win - lost;
	}

	private Double getPotential(State state) {
		// By default the table is empty but by default we assume a value of 0.0
		return V.getOrDefault(state, 0.0);
	}

	private City getPreferableAction(State state) {
		return best_action_table.get(state);
	}

	// Update the potential with the corresponding action
	private void setPotential(State state, double potential, City action) {
		V.put(state, potential);
		best_action_table.put(state, action);
	}

}