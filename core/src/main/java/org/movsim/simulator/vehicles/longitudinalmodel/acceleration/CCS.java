package org.movsim.simulator.vehicles.longitudinalmodel.acceleration;

import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.simulator.vehicles.longitudinalmodel.acceleration.parameter.IModelParameterCCS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO reduce visibility to package private
public class CCS extends LongitudinalModelBase {

    private static final Logger logger = LoggerFactory.getLogger(CCS.class);

    private static final double DENSITY_AIR = 1.3;
    private static final double EARTH_GRAVITY = 9.81;

    // HACK here for start phase
    public static enum Waves {
        NOWAVE, FOURWAVES, TENWAVES
    }

    private static Waves wave = Waves.NOWAVE;

    // /**
    // * @return the wave
    // */
    // public static Waves getWave() {
    // return wave;
    // }

    /**
     * @param wave
     *            the wave to set
     */
    public static void setWave(Waves wave) {
        CCS.wave = wave;
    }

    private int counter = 0;

    private final double length;

    private final IModelParameterCCS param;

    /**
     * Instantiates a new CCS (cross country skiing).
     * 
     * @param modelParameter
     * @param vehLength
     */
    public CCS(IModelParameterCCS modelParameter, double vehLength) {
        super(ModelName.CCS);
        this.param = modelParameter;
        this.length = vehLength;
    }

    /**
     * make (mis)use of randomization factor intended for desired speed.
     */
    private double getP0() {
        return param.getP0() * v0RandomizationFactor;
    }

    @Override
    public double calcAcc(Vehicle me, final Vehicle frontVehicle) {
        // Local dynamical variables
        final double s = me.getNetDistance(frontVehicle);
        final double v = me.getSpeed();
        final double dv = me.getRelSpeed(frontVehicle);
        final double gradient = me.getSlope();

        return acc(s, v, dv, gradient);
    }

    @Override
    public double calcAccSimple(double s, double v, double dv) {
        return acc(s, v, dv, 0);
    }

    private double acc(double s, double v, double dv, double gradient) {

        // System.out.println("v," +v+"   gradient "+gradient );

        final double v_c = param.getVC();

        double a_max = 4 * getP0() / (v_c * param.getMass());
        double gradientSlip = a_max / EARTH_GRAVITY;

        double F_diagonal = (4 * getP0() / v_c) * (1 - v / v_c) * ((v < v_c) ? 1 : 0);
        double F_herringbone = (4 * param.getPHerringbone() / param.getVCHerringbone()) * (1 - v / v_c)
                * ((v < param.getVCHerringbone()) ? 1 : 0);

        double F = (gradient < 0.5 * gradientSlip) ? F_diagonal : Math.max(F_diagonal, F_herringbone);

        final double s0 = getMinimumGap();
        double b_kin = 0.5 * v * dv * ((dv > 0) ? 1 : 0) / Math.max(s, 0.00001 * s0);

        double acc_free = F / param.getMass() - 0.5 * param.getCw() * param.getA() * DENSITY_AIR * v * v
                / param.getMass() - EARTH_GRAVITY * (param.getFriction() + gradient);

        double s_rel = (v * param.getT() + 0.5 * s0) / Math.max(s - 0.5 * s0, 0.00001 * s0);
        double acc_int = -(b_kin * b_kin / param.getB()) - Math.max(param.getB() * (s_rel - 1), 0)
                - Math.max(acc_free * s_rel, 0);
        double aWanted = Math.max(acc_free + acc_int, -param.getBMaximum() - gradient * EARTH_GRAVITY);
        // if(s<2) { System.out.println("s: "+s+ "s0:  "+s0+"  s_rel: "+
        // s_rel+"   v:  "+v+"   afree: "+acc_free+"    acc_int: "+acc_int+ "   awanted: "+ aWanted);
        // }
        logger.debug("aWanted = {}", aWanted);
        return aWanted;
    }

    @Override
    public double calcAcc(Vehicle me, Vehicle frontVehicle, double alphaT, double alphaV0, double alphaA) {
        // wave start hack 300 = 1min
        if (wave == Waves.FOURWAVES) {
            if ((me.roadSegmentId() <= 7 && counter < 1500) || (me.roadSegmentId() <= 5 && counter < 3000)
                    || (me.roadSegmentId() <= 2 && counter < 9000)) {
                counter++;
                me.setSpeed(0);
                return 0;
            }
        } else if (wave == Waves.TENWAVES) {
            if ((me.roadSegmentId() <= 8 && counter < 1500)
                    || (me.roadSegmentId() <= 7 && counter < 3000) || (me.roadSegmentId() <= 6 && counter < 4500)
                    || (me.roadSegmentId() <= 5 && counter < 6000) || (me.roadSegmentId() <= 4 && counter < 7500)
                    || (me.roadSegmentId() <= 3 && counter < 9000) || (me.roadSegmentId() <= 2 && counter < 10500)) {
                counter++;
                me.setSpeed(0);
                return 0;
            }
        }

        // Local dynamical variables
        final double s = me.getNetDistance(frontVehicle);
        final double v = me.getSpeed();
        final double dv = me.getRelSpeed(frontVehicle);
        final double gradient = me.getSlope();
        return acc(s, v, dv, gradient);
    }

    @Override
    public double getDesiredSpeed() {
        throw new UnsupportedOperationException("getDesiredSpeed not applicable for CSS model.");
    }

    @Override
    protected IModelParameterCCS getParameter() {
        return param;
    }

}
