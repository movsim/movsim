package org.movsim.simulator.vehicles.longitudinalmodel.acceleration;

import org.movsim.input.model.vehicle.longitudinalmodel.LongitudinalModelInputDataCCS;
import org.movsim.simulator.roadnetwork.LaneSegment;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.simulator.vehicles.longitudinalmodel.LongitudinalModelBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CCS extends LongitudinalModelBase {

    final static Logger logger = LoggerFactory.getLogger(CCS.class);
    

    final static private double DENSITY_AIR = 1.3;
    final static private double EARTH_GRAVITY = 9.81;

    private double mass;
    private double A;
    private double cw;
    private double friction;
    private double T;
    private double P0;
    private double v_c;
    private double P_straddle;
    private double V_c_straddle;
    private double b;

    private double a;

    private double lenght;

    private int gradient;

    /**
     * Instantiates a new CCS (cross country skiing).
     * 
     * @param parameters
     *            the parameters
     * @param vehLength
     */
    public CCS(LongitudinalModelInputDataCCS parameters, double vehLength) {
        super(ModelName.CCS, parameters);
        lenght = vehLength;
        mass = parameters.getMass();
        A = parameters.getA();
        cw = parameters.getCw();
        friction = parameters.getFriction();
        T = parameters.getT();
        P0 = parameters.getP0();
        v_c = parameters.getV_c();
        P_straddle = parameters.getP_straddle();
        V_c_straddle = parameters.getV_c_straddle();
        b = parameters.getB();

//        System.out.println("P0"+P0);
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

        

        gradient = 0;
        return acc(s, v, dv, gradient );
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

//        System.out.println("v," +v+" gradient "+gradient );
        
        return acc(s, v, dv, gradient);
    }

    @Override
    public double calcAccSimple(double s, double v, double dv) {
        return acc(s, v, dv, gradient);
    }

    private double acc(double s, double v, double dv, double gradient) {

        double a_max = 4*P0 / (v_c*mass);
        double P_diagonal = 4 * (v/v_c)*(1-v/v_c) * ((v<v_c) ? 1 : 0);
        
        double P_straddle = 4 * (v/V_c_straddle)*(1-v/v_c) * ((v<V_c_straddle) ? 1 : 0);
        
        double P = Math.max(P0, P_straddle);
        
        double acc_free = ((v>0.01*v_c)? P/(mass*v) : a_max) - 0.5*cw*A*DENSITY_AIR*v*v/mass - EARTH_GRAVITY  *(friction*v + gradient);
        
        double b_kin = dv*dv * ((dv>0) ? 1: 0) / Math.max(s-s0, 0.01*s0);
     
        double acc_int = - Math.min(b_kin*b_kin, b*b) / b - Math.max(acc_free, 0.5*a_max ) * v * T / Math.max(s-s0, 0.01*s0);
        
        double aWanted = Math.max(acc_free + acc_int, -b-gradient*EARTH_GRAVITY);
        
        logger.debug("aWanted = {}", aWanted);
        return aWanted;
    }

   
    @Override
    public double getDesiredSpeed() {
        return 20;
        // throw new UnsupportedOperationException("getDesiredSpeed not applicable for CSS model.");
    }

    @Override
    public double calcAcc(Vehicle me, Vehicle frontVehicle, double alphaT, double alphaV0, double alphaA) {
        // TODO Auto-generated method stub
        return 0;
    }

}
