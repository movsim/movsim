package org.movsim.simulator.vehicles.longitudinalmodel.acceleration;

import org.movsim.input.model.vehicle.longitudinalmodel.LongitudinalModelInputData;
import org.movsim.simulator.roadnetwork.LaneSegment;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.simulator.vehicles.longitudinalmodel.LongitudinalModelBase;

public class CCS extends LongitudinalModelBase {

    protected CCS(ModelName modelName, LongitudinalModelInputData parameters) {
        super(modelName, parameters);
        // TODO Auto-generated constructor stub
    }

    @Override
    public double calcAcc(Vehicle me, LaneSegment laneSegment, double alphaT, double alphaV0, double alphaA) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double calcAcc(Vehicle me, Vehicle frontVehicle) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double calcAccSimple(double s, double v, double dv) {
        // TODO Auto-generated method stub
        return 0;
    }

}
