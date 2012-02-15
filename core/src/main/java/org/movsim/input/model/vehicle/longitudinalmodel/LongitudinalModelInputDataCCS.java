package org.movsim.input.model.vehicle.longitudinalmodel;

import java.util.Map;

import org.movsim.simulator.vehicles.longitudinalmodel.LongitudinalModelBase.ModelName;

/**
 * <CCS mass="80" A="1" cw="0.7" friction="0.02" T="1.2" s0="0.3" P0="150" V_c="5" P_tackling="100" V_c_tackling="1.5" b="2"/>
 *
 * @author ralph
 *
 */
public class LongitudinalModelInputDataCCS extends LongitudinalModelInputData {

    private double mass;
    private double A;
    private double cw;
    private double friction;
    private double T;
    private double s0;
    private double p0;
    private double V_c;
    private double P_tackling;
    private double V_c_tackling;
    private double b;

    public LongitudinalModelInputDataCCS(Map<String, String> map) {
        super(ModelName.CCS);
        mass = Double.parseDouble(map.get("mass"));
        A = Double.parseDouble(map.get("A"));
        cw = Double.parseDouble(map.get("cw"));
        friction = Double.parseDouble(map.get("friction"));
        T = Double.parseDouble(map.get("T"));
        s0 = Double.parseDouble(map.get("s0"));
        p0 = Double.parseDouble(map.get("p0"));
        V_c = Double.parseDouble(map.get("V_c"));
        P_tackling = Double.parseDouble(map.get("P_tackling"));
        V_c_tackling = Double.parseDouble(map.get("V_c_tackling"));
        b = Double.parseDouble(map.get("b"));
        
    }
    
    /**
     * @return the mass
     */
    public double getMass() {
        return mass;
    }

    /**
     * @return the a
     */
    public double getA() {
        return A;
    }

    /**
     * @return the cw
     */
    public double getCw() {
        return cw;
    }

    /**
     * @return the friction
     */
    public double getFriction() {
        return friction;
    }

    /**
     * @return the t
     */
    public double getT() {
        return T;
    }

    /**
     * @return the s0
     */
    public double getS0() {
        return s0;
    }

    /**
     * @return the p0
     */
    public double getP0() {
        return p0;
    }

    /**
     * @return the v_c
     */
    public double getV_c() {
        return V_c;
    }

    /**
     * @return the p_tackling
     */
    public double getP_tackling() {
        return P_tackling;
    }

    /**
     * @return the v_c_tackling
     */
    public double getV_c_tackling() {
        return V_c_tackling;
    }

    /**
     * @return the b
     */
    public double getB() {
        return b;
    }

    @Override
    public void resetParametersToDefault() {
        // TODO Auto-generated method stub

    }

    @Override
    protected void checkParameters() {
        // TODO Auto-generated method stub

    }

}
