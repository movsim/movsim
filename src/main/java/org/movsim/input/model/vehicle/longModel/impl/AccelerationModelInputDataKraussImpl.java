/**
 * 
 * Copyright (C) 2010 by Ralph Germ (http://www.ralphgerm.de)
 * 
 */
package org.movsim.input.model.vehicle.longModel.impl;

import java.util.Map;

import org.movsim.input.model.vehicle.longModel.AccelerationModelInputDataKrauss;

/**
 * @author ralph
 * 
 */
public class AccelerationModelInputDataKraussImpl extends AccelerationModelInputDataGippsImpl implements
        AccelerationModelInputDataKrauss {

    /** The epsilon. */
    private double epsilon;
    private final double epsilonDefault;

    /**
     * @param modelName
     * @param map
     */
    public AccelerationModelInputDataKraussImpl(String modelName, Map<String, String> map) {
        super(modelName, map);
        epsilon = epsilonDefault = Double.parseDouble(map.get("epsilon"));
        checkParameters();
    }

    protected void checkParameters() {
        super.checkParameters();
        // TODO epsilon
    }

    public void resetParametersToDefault() {
        super.resetParametersToDefault();
        epsilon = epsilonDefault;
    }

    public void setEpsilon(double epsilon) {
        this.epsilon = epsilon;
        parametersUpdated();
    }

    public double getEpsilon() {
        return epsilon;
    }
}
