package org.movsim.simulator.vehicles.lanechanging;

import org.movsim.simulator.vehicles.VehicleContainer;

public interface LaneChangingModel {

    boolean isMandatoryLaneChangeSafe(final VehicleContainer vehContainerTargetLane);

}
