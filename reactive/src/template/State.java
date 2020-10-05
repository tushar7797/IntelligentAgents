package template;

import logist.topology.Topology.City;


public class State {

	public City city;
	public City task;

	public State(City city, City task) {
		this.city = city;
		this.task = task;
	}

	// Eclipse generate this; no human should read it!
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((city == null) ? 0 : city.hashCode());
		result = prime * result + ((task == null) ? 0 : task.hashCode());
		return result;
	}

	// Eclipse generate this; no human should read it!
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		State other = (State) obj;
		if (city == null) {
			if (other.city != null)
				return false;
		} else if (!city.equals(other.city))
			return false;
		if (task == null) {
			if (other.task != null)
				return false;
		} else if (!task.equals(other.task))
			return false;
		return true;
	}

}