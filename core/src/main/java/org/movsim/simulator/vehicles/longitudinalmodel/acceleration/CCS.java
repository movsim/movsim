package org.movsim.simulator.vehicles.longitudinalmodel.acceleration;

import org.movsim.input.model.vehicle.longitudinalmodel.LongitudinalModelInputDataCCS;
import org.movsim.simulator.roadnetwork.LaneSegment;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.simulator.vehicles.longitudinalmodel.LongitudinalModelBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CCS extends LongitudinalModelBase {

    final static Logger logger = LoggerFactory.getLogger(CCS.class);

    private double mass;
    private double A;
    private double cw;
    private double friction;
    private double T;
    private double p0;
    private double V_c;
    private double P_tackling;
    private double V_c_tackling;
    private double b;

    /**
     * Instantiates a new CCS (cross county skiing).
     * 
     * @param parameters
     *            the parameters
     */
    protected CCS(ModelName modelName, LongitudinalModelInputDataCCS parameters) {
        super(modelName, parameters);
        logger.debug("init model parameters");
        mass = parameters.getMass();
        A = parameters.getA();
        cw = parameters.getCw();
        friction = parameters.getFriction();
        T = parameters.getT();
        p0 = parameters.getP0();
        V_c = parameters.getV_c();
        P_tackling = parameters.getP_tackling();
        V_c_tackling = parameters.getV_c_tackling();
        b = parameters.getB();
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

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.longitudinalmodel.LongitudinalModelBase#getDesiredSpeed()
     */
    @Override
    public double getDesiredSpeed() {
        throw new UnsupportedOperationException("getDesiredSpeed not applicable for CSS model.");
    }

}
