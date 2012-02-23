package org.movsim.input.model.vehicle.longitudinalmodel;

import java.util.Map;

import org.movsim.simulator.vehicles.longitudinalmodel.LongitudinalModelBase.ModelName;

/**
 * <CCS mass="80" A="1" cw="0.7" friction="0.02" T="1.2" s0="1" p0="200" v_c="8" p_herringbone="150" v_c_herringbone="1.5" b="1" b_maximal="2" />
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
    private double P0;
    private double v_c;
    private double p_herringbone;
    private double v_c_herringbone;
    private double b;
    private double b_maximal;

    public LongitudinalModelInputDataCCS(Map<String, String> map) {
        super(ModelName.CCS);
        mass = Double.parseDouble(map.get("mass"));
        A = Double.parseDouble(map.get("A"));
        cw = Double.parseDouble(map.get("cw"));
        friction = Double.parseDouble(map.get("friction"));
        T = Double.parseDouble(map.get("T"));
        s0 = Double.parseDouble(map.get("s0"));
        P0 = Double.parseDouble(map.get("p0"));
        v_c = Double.parseDouble(map.get("v_c"));
        p_herringbone = Double.parseDouble(map.get("p_herringbone"));
        v_c_herringbone = Double.parseDouble(map.get("v_c_herringbone"));
        b = Double.parseDouble(map.get("b"));
        b_maximal = Double.parseDouble(map.get("b_maximal"));
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
        return P0;
    }

    /**
     * @return the v_c
     */
    public double getV_c() {
        return v_c;
    }

    /**
     * @return the p_tackling
     */
    public double getP_herringbone() {
        return p_herringbone;
    }

    /**
     * @return the v_c_tackling
     */
    public double getV_c_herringbone() {
        return v_c_herringbone;
    }

    /**
     * @return the b
     */
    public double getB() {
        return b;
    }
    
    public double getB_maximal() {
        return b_maximal;
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
