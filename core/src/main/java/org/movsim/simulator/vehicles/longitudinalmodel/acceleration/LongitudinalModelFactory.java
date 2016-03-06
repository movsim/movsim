/*
 * Copyright (C) 2010, 2011, 2012 by Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden
 * <movsim.org@gmail.com>
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

import org.movsim.autogen.AccelerationModelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LongitudinalModelFactory {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(LongitudinalModelFactory.class);

    /**
     * Long model factory with vehicle length vehicle length is only needed for KKW (explicit model parameter).
     * 
     * @param vehLength
     * @param longitudinalModelType
     * @param simulationTimestep
     */
    public static LongitudinalModelBase create(double vehLength, AccelerationModelType longitudinalModelType,
            double simulationTimestep) {
        LongitudinalModelBase longModel = null;
        if (longitudinalModelType.isSetModelParameterIDM()) {
            longModel = new IDM(longitudinalModelType.getModelParameterIDM());
        } else if (longitudinalModelType.isSetModelParameterACC()) {
            longModel = new ACC(longitudinalModelType.getModelParameterACC());
        } else if (longitudinalModelType.isSetModelParameterOVMFVDM()) {
            longModel = new OVM_FVDM(longitudinalModelType.getModelParameterOVMFVDM());
        } else if (longitudinalModelType.isSetModelParameterGipps()) {
            longModel = new Gipps(simulationTimestep, longitudinalModelType.getModelParameterGipps());
        } else if (longitudinalModelType.isSetModelParameterKrauss()) {
            longModel = new Krauss(simulationTimestep, longitudinalModelType.getModelParameterKrauss());
        } else if (longitudinalModelType.isSetModelParameterNewell()) {
            return new Newell(simulationTimestep, longitudinalModelType.getModelParameterNewell());
        } else if (longitudinalModelType.isSetModelParameterNSM()) {
            longModel = new NSM(longitudinalModelType.getModelParameterNSM());
        } else if (longitudinalModelType.isSetModelParameterKKW()) {
            longModel = new KKW(longitudinalModelType.getModelParameterKKW(), vehLength);
        } else if (longitudinalModelType.isSetModelParameterCCS()) {
            longModel = new CCS(longitudinalModelType.getModelParameterCCS(), vehLength);
        } else if (longitudinalModelType.isSetModelParameterPTM()) {
            longModel = new PTM(simulationTimestep, longitudinalModelType.getModelParameterPTM());
        } else {
            throw new IllegalArgumentException("unknown acceleration model=" + longitudinalModelType.toString());
        }
        return longModel;
    }

}
