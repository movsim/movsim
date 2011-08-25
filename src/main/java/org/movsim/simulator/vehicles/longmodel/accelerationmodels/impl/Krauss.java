package org.movsim.simulator.vehicles.longmodel.accelerationmodels.impl;

import org.movsim.input.model.vehicle.longModel.AccelerationModelInputDataKrauss;
import org.movsim.simulator.vehicles.Moveable;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.simulator.vehicles.VehicleContainer;
import org.movsim.simulator.vehicles.longmodel.accelerationmodels.AccelerationModel;
import org.movsim.simulator.vehicles.longmodel.accelerationmodels.AccelerationModelCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
// paper reference and modifications ...

/**
 * The Class Krauss.
 */
public class Krauss extends LongitudinalModel implements AccelerationModel {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(Krauss.class);

    /**
     * The parameter T is given by the update timestep dt: dt = T = Tr =
     * tau_relax
     */
    private double T, dt;

    /** The v0. */
    private double v0;

    /** The a. */
    private double a;

    /** The b. */
    private double b;

    /** The s0. */
    private double s0;

    private double epsilon;

    /**
     * Instantiates a new krauss instance.
     * 
     * @param modelName
     *            the model name
     * @param parameters
     *            the parameters
     */
    public Krauss(String modelName, AccelerationModelInputDataKrauss parameters) {
        super(modelName, AccelerationModelCategory.INTERATED_MAP_MODEL, parameters);
        initParameters();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.longmodel.accelerationmodels.impl.
     * LongitudinalModel#initParameters()
     */
    @Override
    protected void initParameters() {
        logger.debug("init model parameters");
        this.T = this.dt = ((AccelerationModelInputDataKrauss) parameters).getDt();
        this.v0 = ((AccelerationModelInputDataKrauss) parameters).getV0();
        this.a = ((AccelerationModelInputDataKrauss) parameters).getA();
        this.b = ((AccelerationModelInputDataKrauss) parameters).getB();
        this.s0 = ((AccelerationModelInputDataKrauss) parameters).getS0();
        // the dimensionless epsilon has similar effects as the braking
        // probability of the Nagel-S CA
        // default value 0.4 (PRE) or 1 (EPJB)
        this.epsilon = ((AccelerationModelInputDataKrauss) parameters).getEpsilon();
    }

    /**
     * Gets the t.
     * 
     * @return the t
     */
    public double getT() {
        return T;
    }

    /**
     * Gets the v0.
     * 
     * @return the v0
     */
    public double getV0() {
        return v0;
    }

    /**
     * Gets the a.
     * 
     * @return the a
     */
    public double getA() {
        return a;
    }

    /**
     * Gets the b.
     * 
     * @return the b
     */
    public double getB() {
        return b;
    }

    /**
     * Gets the s0.
     * 
     * @return the s0
     */
    public double getS0() {
        return s0;
    }

    /**
     * Gets the epsilon.
     * 
     * @return the epsilon
     */
    public double getEpsilon() {
        return epsilon;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.simulator.vehicles.longmodel.accelerationmodels.AccelerationModel
     * #acc(org.movsim.simulator.vehicles.Vehicle,
     * org.movsim.simulator.vehicles.VehicleContainer, double, double, double)
     */
    @Override
    public double acc(Vehicle me, VehicleContainer vehContainer, double alphaT, double alphaV0, double alphaA) {

        // Local dynamical variables
        final Moveable vehFront = vehContainer.getLeader(me);
        final double s = me.netDistance(vehFront);
        final double v = me.getSpeed();
        final double dv = (vehFront == null) ? 0 : v - vehFront.getSpeed();

        // space dependencies modeled by speedlimits, alpha's

        // consider external speedlimit
        final double v0Local = Math.min(alphaV0 * v0, me.speedlimit());

        // #############################################################
        // space dependencies modelled by alpha_T
        // (!!! watch for alpha_T: dt unchanged, possibly inconsistent!)
        // #############################################################

        final double TLocal = alphaT * T;

        // actual Krauss formula
        return acc(s, v, dv, v0Local, TLocal);

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.simulator.vehicles.longmodel.accelerationmodels.AccelerationModel
     * #accSimple(double, double, double)
     */
    @Override
    public double accSimple(double s, double v, double dv) {
        return acc(s, v, dv, v0, T);
    }

    /**
     * Acc.
     * 
     * @param s
     *            the s
     * @param v
     *            the v
     * @param dv
     *            the dv
     * @param v0Local
     *            the v0 local
     * @param TLocal
     *            the local time gap. Notice that inconsistencies may arise for
     *            nontrivial values since then no longer dt=T=tau_relax making
     *            the vSafe formula possibly inconsistent
     * @return the double
     */
    private double acc(double s, double v, double dv, double v0Local, double TLocal) {
        final double vp = v - dv;
        /*
         * safe speed; I checked that the complicated formula in PRE 55, 5601
         * (1997) is essentially my vSafe formula for the simple Gipps model.
         * The complicated formula considers effects of finite dt; this is
         * treated uniformly for all models in our update routine, so it is not
         * necessary here. Therefore, I chose the simple Gipps vSafe formula see
         * cmp_vsafe_GippsKrauss.gnu for details
         */
        final double vSafe = -b * T + Math.sqrt(b * b * T * T + vp * vp + 2 * b * Math.max(s - s0, 0.));

        /*
         * vUpper =upper limit of new speed (denoted v1 in PRE) corresponds to
         * vNew of the Gipps model
         */
        final double vUpper = Math.min(vSafe, Math.min(v + a * TLocal, v0Local));

        // ===============================================
        // The Krauss model is essentially the Gipps model with the following
        // three additional code lines!
        // ===============================================

        /*
         * vLower =lower limit of new speed (denoted v0 in PRE) some
         * modifications due to dimensional units were applied. Notice that
         * vLower may be > vUpper in some cut-in situations: these
         * inconsistencies were not recognized/treated in the PRE publication
         */
//        System.out.println("ooo:  "+ Math.max(0, (v - b * TLocal)));
        
        double vLower = (1 - epsilon) * vUpper + epsilon * Math.max(0, (v - b * TLocal));
        final double r = Math.random(); // should be an instance of a
                                        // uniform(0,1) distributed pseudorandom
                                        // number
        final double vNew = vLower + r * (vUpper - vLower);
        // ===============================================
        final double aWanted = (vNew - v) / TLocal;
//        System.out.println();
//        System.out.println("r: "+r);
//        System.out.println("v: "+ v);
//        System.out.println("Vupper: " + vUpper);
//        System.out.println("vLower: "+vLower);
//        System.out.println("VNew: "+ vNew);
        
        return aWanted;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.longmodel.accelerationmodels.impl.
     * LongitudinalModel#parameterV0()
     */
    @Override
    public double parameterV0() {
        return v0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.vehicles.longmodel.accelerationmodels.impl.
     * LongitudinalModel#getRequiredUpdateTime()
     */
    @Override
    public double getRequiredUpdateTime() {
        return this.T; // iterated map requires specific timestep!!
    }

}
