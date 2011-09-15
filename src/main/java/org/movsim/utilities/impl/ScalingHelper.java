package org.movsim.utilities.impl;

import org.movsim.simulator.Constants;

public class ScalingHelper {

    private ScalingHelper() {
        
    }

    public static double getScalingLength(final String modelName) {
        double scaleCA  = 1;
        if (modelName.equals(Constants.MODEL_NAME_NSM) || modelName.equals(Constants.MODEL_NAME_BARL)) {
            scaleCA = 7.5;
        } else if (modelName.equals(Constants.MODEL_NAME_KKW)) {
            scaleCA = 0.5;
        }
        return scaleCA;
    }
}
