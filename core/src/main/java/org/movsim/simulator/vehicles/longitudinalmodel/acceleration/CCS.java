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
    private double P0;
    private double V_c;
    private double P_tackling;
    private double V_c_tackling;
    private double b;

    private double a;

    private double lenght;

    /**
     * Instantiates a new CCS (cross country skiing).
     * 
     * @param parameters
     *            the parameters
     * @param vehLength 
     */
    public CCS(LongitudinalModelInputDataCCS parameters, double vehLength) {
        super(ModelName.CCS, parameters);
        logger.debug("init model parameters");
        lenght = vehLength;
        mass = parameters.getMass();
        A = parameters.getA();
        cw = parameters.getCw();
        friction = parameters.getFriction();
        T = parameters.getT();
        P0 = parameters.getP0();
        V_c = parameters.getV_c();
        P_tackling = parameters.getP_tackling();
        V_c_tackling = parameters.getV_c_tackling();
        b = parameters.getB();
        System.out.println("constr. CSS. p0: "+P0);
        System.out.println("length: "+ lenght);
    }

    @Override
    public double calcAcc(Vehicle me, LaneSegment laneSegment, double alphaT, double alphaV0, double alphaA) {

        // Local dynamical variables
        final Vehicle frontVehicle = laneSegment.frontVehicle(me);
        final double s = me.getNetDistance(frontVehicle);
        final double v = me.getSpeed();
        final double dv = me.getRelSpeed(frontVehicle);

        // space dependencies modeled by speedlimits, alpha's

        final double localT = alphaT * T;
      
        // consider external speedlimit
        final double localV0;
        if (me.getSpeedlimit() != 0.0) {
            localV0 = Math.min(alphaV0 * v0, me.getSpeedlimit());
        } else {
            localV0 = alphaV0 * v0;
        }
        final double localA = alphaA * a;

        return acc(s, v, dv, localT, localV0, localA);
    }

    @Override
    public double calcAcc(Vehicle me, final Vehicle frontVehicle) {
        // Local dynamical variables
        final double s = me.getNetDistance(frontVehicle);
        final double v = me.getSpeed();
        final double dv = me.getRelSpeed(frontVehicle);

        final double localT = T;
        final double localV0;
        if (me.getSpeedlimit() != 0.0) {
            localV0 = Math.min(v0, me.getSpeedlimit());
        } else {
            localV0 = v0;
        }
        final double localA = a;

        return acc(s, v, dv, localT, localV0, localA);
    }

    @Override
    public double calcAccSimple(double s, double v, double dv) {
        return acc(s, v, dv, T, v0, a);
    }

    private double acc(double s, double v, double dv, double TLocal, double v0Local, double aLocal) {
        // treat special case of v0=0 (standing obstacle)
        if (v0Local == 0.0) {
            return 0.0;
        }

        final double aWanted =  1; // aLocal * (1.0 - Math.pow((v / v0Local), delta) - (sstar / s) * (sstar / s));

        logger.debug("aWanted = {}", aWanted);
        return aWanted; // limit to -bMax in Vehicle
    }
    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.longitudinalmodel.LongitudinalModelBase#getDesiredSpeed()
     */
    @Override
    public double getDesiredSpeed() {
        return 100;
//        throw new UnsupportedOperationException("getDesiredSpeed not applicable for CSS model.");
    }

}
