/*
 * Copyright (C) 2010, 2011, 2012 by Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden
 *                                   <movsim.org@gmail.com>
 * -----------------------------------------------------------------------------------------
 * 
 * This file is part of
 * 
 * MovSim - the multi-model open-source vehicular-traffic simulator.
 * 
 * MovSim is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MovSim is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MovSim. If not, see <http://www.gnu.org/licenses/>
 * or <http://www.movsim.org>.
 * 
 * -----------------------------------------------------------------------------------------
 */

package org.movsim.simulator.vehicles.longitudinalmodel.acceleration;

import org.movsim.core.autogen.ModelParameterACC;
import org.movsim.core.autogen.ModelParameterCCS;
import org.movsim.core.autogen.ModelParameterGipps;
import org.movsim.core.autogen.ModelParameterIDM;
import org.movsim.core.autogen.ModelParameterKKW;
import org.movsim.core.autogen.ModelParameterKrauss;
import org.movsim.core.autogen.ModelParameterNSM;
import org.movsim.core.autogen.ModelParameterNewell;
import org.movsim.core.autogen.ModelParameterOVMFVDM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LongitudinalModelFactory {

    /** The Constant logger. */
    private static final Logger logger = LoggerFactory.getLogger(LongitudinalModelFactory.class);

    /**
     * Long model factory with vehicle length vehicle length is only needed for KKW (explicit model parameter).
     * 
     * @param vehLength
     * @param modelParameter
     * @param simulationTimestep
     * @return
     */
    public static LongitudinalModelBase create(double vehLength, ModelParameter modelParameter,
            double simulationTimestep) {
        LongitudinalModelBase longModel = null;
        if (modelParameter instanceof ModelParameterIDM) {
            longModel = new IDM((ModelParameterIDM) modelParameter);
        } else if (modelParameter instanceof ModelParameterACC) {
            longModel = new ACC((ModelParameterACC) modelParameter);
        } else if (modelParameter instanceof ModelParameterOVMFVDM) {
            longModel = new OVM_FVDM((ModelParameterOVMFVDM) modelParameter);
        } else if (modelParameter instanceof ModelParameterGipps) {
            longModel = new Gipps(simulationTimestep, (ModelParameterGipps) modelParameter);
        } else if (modelParameter instanceof ModelParameterKrauss) {
            longModel = new Krauss(simulationTimestep, (ModelParameterKrauss) modelParameter);
        } else if (modelParameter instanceof ModelParameterNewell) {
            return new Newell(simulationTimestep, (ModelParameterNewell) modelParameter);
        } else if (modelParameter instanceof ModelParameterNSM) {
            longModel = new NSM((ModelParameterNSM) modelParameter);
        } else if (modelParameter instanceof ModelParameterKKW) {
            longModel = new KKW((ModelParameterKKW) modelParameter, vehLength);
        } else if (modelParameter instanceof ModelParameterCCS) {
            longModel = new CCS((ModelParameterCCS) modelParameter, vehLength);
        } else {
            logger.error("model input unknown: ", modelParameter.toString());
            System.exit(0); // TODO throw exception
        }
        return longModel;

    }

}
