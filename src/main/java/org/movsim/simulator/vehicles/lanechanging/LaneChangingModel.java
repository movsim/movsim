package org.movsim.simulator.vehicles.lanechanging;

import org.movsim.simulator.vehicles.VehicleContainer;
import org.movsim.simulator.vehicles.Vehicle;

public interface LaneChangingModel {

    void updateLaneChangeStatusFromRamp(double dt, final Vehicle me, final VehicleContainer vehContainerTargetLane);

}
