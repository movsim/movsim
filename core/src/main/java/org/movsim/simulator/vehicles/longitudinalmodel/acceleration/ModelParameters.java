package org.movsim.simulator.vehicles.longitudinalmodel.acceleration;

import org.movsim.core.autogen.ModelParameterIDM;

/**
 * Collection of utility builders. Will be extended depending on use cases.
 * 
 * 
 * @author kesting
 * 
 */
public class ModelParameters {

    public static boolean isValidProbabilityRange(double p) {
        return p >= 0 && p <= 1;
    }
    public static void test() {
        ModelParameterIDM modelParameterIDM = new ModelParameterIDM();
        modelParameterIDM.isSetB();

        // IDM idm = new IDM(ModelParameterIDM);

    }

    public static ModelParameterIDM getDefaultModelParameterIDM() {
        // TODO fluent interface
        ModelParameterIDM param = new ModelParameterIDM();
        param.setV0(120 / 3.6);
        param.setT(1.5);
        param.setA(1);
        param.setB(1);
        param.setS0(2);
        param.setS1(0);
        param.setDelta(4);

        // TODO good access to model parameter validation: static method
        // if (!IDM.isValidParameters(param)) {
        // throw new IllegalArgumentException();
        // }
        return param;
    }
}
