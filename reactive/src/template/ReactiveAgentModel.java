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

public class ReactiveAgentModel implements ReactiveBehavior {

	// Potential table; access it through get/setPotential
	private HashMap<State, Double> potentials = new HashMap<State, Double>();

	// Action table; best move for state/potential; use getPreferableAction
	private HashMap<State, City> actions = new HashMap<State, City>();

	// For reward per action ration computation
	private Agent agent;
	private int counterSteps = 0;

	// Setup transition table for our Reactive Agent
	@Override
	public void setup(Topology topology, TaskDistribution td, Agent agent) {
		// Reads the gamma factor from the agents.xml file.
		// If the property is not present it defaults to 0.95
		Double gamma = agent.readProperty("gamma", Double.class, 0.95);

		List<City> tasks = new ArrayList<Topology.City>(topology.cities());
		tasks.add(null); // The "no task" state

		Vehicle vehicle = agent.vehicles().get(0);

		boolean improvement = false;
		do {

			improvement = false;

			// For all states
			for (City city : topology) {
				for (City task : tasks) {

					// Skip invalid state
					if (city.equals(task))
						continue;

					// Current state
					State state = new State(city, task);

					double bestQ = 0.0;
					City bestAction = null;

					Set<City> dests = getLegalDestinations(state);

					// Compute potential for all actions (that is, for all legal moves), and keep only the best
					for (City action : dests) {
						double q = reward(state, action, vehicle, td);

						// For all possible next state' (prime)
						City cityP = action;
						for (City taskP : tasks) {
							State stateP = new State(cityP, taskP);

							q += gamma * transitionProbability(state, action, stateP, td) * getPotential(stateP);
						}

						if (bestQ < q) {
							bestQ = q;
							bestAction = action;
						}
					}

					// Update potential if change
					Double currentPotential = getPotential(state);
					if (!currentPotential.equals(bestQ)) {
						setPotential(state, bestQ, bestAction);
						improvement = true;
					}
				}
			}

		} while (improvement);

		this.agent = agent;
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
		City destination = getPreferableAction(state);

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
	private Set<City> getLegalDestinations(State state) {
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
		return potentials.getOrDefault(state, 0.0);
	}

	private City getPreferableAction(State state) {
		return actions.get(state);
	}

	// Update the potential with the corresponding action
	private void setPotential(State state, double potential, City action) {
		potentials.put(state, potential);
		actions.put(state, action);
	}

}