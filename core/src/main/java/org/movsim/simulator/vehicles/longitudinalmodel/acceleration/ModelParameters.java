package org.movsim.simulator.vehicles.longitudinalmodel.acceleration;

import org.movsim.autogen.ModelParameterIDM;
import org.movsim.simulator.MovsimConstants;
import org.movsim.simulator.vehicles.longitudinalmodel.acceleration.parameter.IModelParameter;
import org.movsim.simulator.vehicles.longitudinalmodel.acceleration.parameter.IModelParameterACC;
import org.movsim.simulator.vehicles.longitudinalmodel.acceleration.parameter.IModelParameterCCS;
import org.movsim.simulator.vehicles.longitudinalmodel.acceleration.parameter.IModelParameterGipps;
import org.movsim.simulator.vehicles.longitudinalmodel.acceleration.parameter.IModelParameterIDM;
import org.movsim.simulator.vehicles.longitudinalmodel.acceleration.parameter.IModelParameterKKW;
import org.movsim.simulator.vehicles.longitudinalmodel.acceleration.parameter.IModelParameterKrauss;
import org.movsim.simulator.vehicles.longitudinalmodel.acceleration.parameter.IModelParameterNSM;
import org.movsim.simulator.vehicles.longitudinalmodel.acceleration.parameter.IModelParameterNewell;
import org.movsim.simulator.vehicles.longitudinalmodel.acceleration.parameter.IModelParameterOVMFVDM;
import org.movsim.simulator.vehicles.longitudinalmodel.acceleration.parameter.IModelParameterPTM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Collection of utility builders. Will be extended depending on use cases.
 * 
 * 
 * @author kesting
 * 
 */
public final class ModelParameters {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(ModelParameters.class);

    public static void test() {
        ModelParameterIDM modelParameterIDM = new ModelParameterIDM();
        modelParameterIDM.isSetB();

        // IDM idm = new IDM(ModelParameterIDM);
    }
    
    /**
     * Returns the model parameter 'desired timegap' if provided by the model. Otherwise the default value is provided.
     * 
     * @param longModel
     * @param defaultValue
     * @return the timegap parameter if supported by model or the default value
     */
    public static double determineTimeGapParmeter(LongitudinalModelBase longModel, double defaultValue) {
        if (longModel instanceof IDM) {
            return ((IDM) longModel).getParameter().getT();
        } else if (longModel instanceof ACC) {
            return ((ACC) longModel).getParameter().getT();
        } else if (longModel instanceof OVM_FVDM) {
            return ((OVM_FVDM) longModel).getParameter().getTau();
        } else if (longModel instanceof Gipps) {
            return ((Gipps) longModel).getParameterT();
        }
        // TODO check if a useful time gap value can be derived/approximated
        return defaultValue;
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

    // TODO check implementation!!! not yet tested/used
    public static boolean isValidDesiredSpeed(IModelParameter param) {
        return param.getV0() >= 0 && param.getV0() <= MovsimConstants.MAX_VEHICLE_SPEED;
    }

    public static boolean isValidMinimumGap(IModelParameter param) {
        return param.getS0() >= 0;
    }

    private static boolean isValidProbabilityRange(double p) {
        return p >= 0 && p <= 1;
    }

    // overloading isValid Methods for different models

    public static boolean isValidParameters(IModelParameterACC param) {
        if (param.getCoolness() < 0 || param.getCoolness() > 1) {
            throw new IllegalStateException("Invalid parameter for ACC coolness parameter= " + param.getCoolness()
                    + ". Choose value within [0,1].");
        }
        if (!isValidDesiredSpeed(param) || isValidMinimumGap(param) || param.getT() <= 0 || param.getS1() < 0
                || param.getDelta() <= 0 || param.getA() <= 0 || param.getB() <= 0) {
            throw new IllegalStateException("invalid parameters for ACC model");
        }
        return true;
    }

    public static boolean isValidParameters(IModelParameterIDM param) {
        if (!isValidDesiredSpeed(param) || isValidMinimumGap(param) || param.getT() <= 0 || param.getS1() < 0
                || param.getDelta() <= 0 || param.getA() <= 0 || param.getB() <= 0) {
            throw new IllegalStateException("invalid parameters for IDM model");
        }
        return true;
    }

    public static boolean isValidParameter(IModelParameterOVMFVDM param) {
        if (!isValidDesiredSpeed(param) || !isValidMinimumGap(param) || param.getTau() <= 0
                || param.getTransitionWidth() < 0 || param.getBeta() < 0 || param.getGamma() < 0) {
            throw new IllegalStateException("invalid parameters for OVM/FVDM.");
        }
        return true;
    }

    public static boolean isValidParameters(IModelParameterGipps param) {
        if (!isValidDesiredSpeed(param) || !isValidMinimumGap(param) || param.getA() <= 0 || param.getB() <= 0) {
            throw new IllegalStateException("invalid parameters for Gipps model");
        }
        return true;
    }

    public static boolean isValidParameter(IModelParameterKrauss param) {
        if (!isValidDesiredSpeed(param) || isValidMinimumGap(param) || param.getA() <= 0 || param.getB() <= 0) {
            throw new IllegalStateException("invalid parameters for Krauss model");
        }
        return true;
    }

    public static boolean isValidParameter(IModelParameterNewell param) {
        if (!isValidDesiredSpeed(param) || isValidMinimumGap(param)) {
            throw new IllegalStateException("invalid parameters for Newell model");
        }
        return true;
    }

    public static boolean isValidParameter(IModelParameterNSM param) {
        if (!isValidDesiredSpeed(param) || isValidMinimumGap(param)
                || !ModelParameters.isValidProbabilityRange(param.getPSlowdown())
                || !ModelParameters.isValidProbabilityRange(param.getPSlowdown())) {
            throw new IllegalStateException("invalid parameters for NSM");
        }
        return true;
    }

    public static boolean isValidParameter(IModelParameterKKW param) {
        if (!isValidDesiredSpeed(param) || isValidMinimumGap(param) || param.getK() < 0
                || !ModelParameters.isValidProbabilityRange(param.getPb0())
                || !ModelParameters.isValidProbabilityRange(param.getPb1())
                || !ModelParameters.isValidProbabilityRange(param.getPa1())
                || !ModelParameters.isValidProbabilityRange(param.getPa2())
                || !ModelParameters.isValidProbabilityRange(param.getVp())) {
            throw new IllegalStateException("invalid parameters for KKW model");
        }
        return true;
    }

    public static boolean isValidParameters(IModelParameterPTM param) {
        // TODO implement method
        return false;
    }

    public static boolean isValidParameters(IModelParameterCCS param) {
        // TODO implement method
        return false;
    }
}
