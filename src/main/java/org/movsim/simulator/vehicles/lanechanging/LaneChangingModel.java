package org.movsim.simulator.vehicles.lanechanging;

import org.movsim.simulator.vehicles.VehicleContainer;

public interface LaneChangingModel {

    //void checkLaneChangeFromRamp(double dt, final Vehicle me, final VehicleContainer vehContainerTargetLane);
    boolean checkLaneChangeFromRamp(double dt, final VehicleContainer vehContainerTargetLane);

}
