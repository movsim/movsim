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

import org.movsim.input.model.vehicle.longitudinalmodel.LongitudinalModelInputData;
import org.movsim.input.model.vehicle.longitudinalmodel.LongitudinalModelInputDataACC;
import org.movsim.input.model.vehicle.longitudinalmodel.LongitudinalModelInputDataCCS;
import org.movsim.input.model.vehicle.longitudinalmodel.LongitudinalModelInputDataGipps;
import org.movsim.input.model.vehicle.longitudinalmodel.LongitudinalModelInputDataIDM;
import org.movsim.input.model.vehicle.longitudinalmodel.LongitudinalModelInputDataKKW;
import org.movsim.input.model.vehicle.longitudinalmodel.LongitudinalModelInputDataKrauss;
import org.movsim.input.model.vehicle.longitudinalmodel.LongitudinalModelInputDataNSM;
import org.movsim.input.model.vehicle.longitudinalmodel.LongitudinalModelInputDataNewell;
import org.movsim.input.model.vehicle.longitudinalmodel.LongitudinalModelInputDataOVM_FVDM;
import org.movsim.simulator.vehicles.longitudinalmodel.acceleration.LongitudinalModelBase.ModelName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LongitudinalModelFactory {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(LongitudinalModelFactory.class);

    /**
     * Long model factory with vehicle length vehicle length is only needed for KKW (explicit model parameter).
     * 
     * @param modelInputData
     *            the model input data
     * @param vehLength
     *            the vehicle length
     * @return the longitudinal model
     */
    public static LongitudinalModelBase create(double vehLength,
            LongitudinalModelInputData modelInputData, double simulationTimestep) {
        final ModelName modelName = modelInputData.getModelName();
        LongitudinalModelBase longModel = null;
        logger.debug("modelName = {}", modelName);
        if (modelName == ModelName.IDM) {
            longModel = new IDM((LongitudinalModelInputDataIDM) modelInputData);
        } else if (modelName == ModelName.ACC) {
            longModel = new ACC((LongitudinalModelInputDataACC) modelInputData);
        } else if (modelName == ModelName.OVM_FVDM) {
            longModel = new OVM_FVDM((LongitudinalModelInputDataOVM_FVDM) modelInputData);
        } else if (modelName == ModelName.GIPPS) {
            longModel = new Gipps(simulationTimestep, (LongitudinalModelInputDataGipps) modelInputData);
        } else if (modelName == ModelName.KRAUSS) {
            longModel = new Krauss(simulationTimestep, (LongitudinalModelInputDataKrauss) modelInputData);
        } else if (modelName == ModelName.NEWELL) {
            return new Newell(simulationTimestep, (LongitudinalModelInputDataNewell) modelInputData);
        } else if (modelName == ModelName.NSM) {
            longModel = new NSM((LongitudinalModelInputDataNSM) modelInputData);
        } else if (modelName == ModelName.KKW) {
            longModel = new KKW((LongitudinalModelInputDataKKW) modelInputData, vehLength);
        } else if (modelName == ModelName.CCS) {
            longModel = new CCS((LongitudinalModelInputDataCCS) modelInputData, vehLength);
        } else {
            logger.error("create model by inputParameter: Model {} not known !", modelName);
            System.exit(0);
        }
        return longModel;
    }

}
