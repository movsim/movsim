package org.movsim.simulator.vehicles.longmodel;

import org.movsim.simulator.roadSection.TrafficLight;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.simulator.vehicles.longmodel.accelerationmodels.AccelerationModel;

public interface TrafficLightApproaching {

	boolean considerTrafficLight();
	
	double accApproaching();

	void update(Vehicle me, double time, TrafficLight trafficLight,
			AccelerationModel longModel);

}