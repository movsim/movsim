package org.movsim.utilities.impl;

import org.movsim.simulator.Constants;

public class ScaleHelper {

    private ScaleHelper() {
    }

    public static double getScale(Object model) {
        double scaleCA;
        if (model.equals(Constants.MODEL_NAME_NSM) || model.equals(Constants.MODEL_NAME_BARL)) {
            scaleCA = 7.5;
        } else if (model.equals(Constants.MODEL_NAME_KKW)) {
            scaleCA = 0.5;
        } else {
            scaleCA = 1;
        }
        return scaleCA;
    }
}
