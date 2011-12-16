/**
 * Copyright (C) 2010, 2011 by Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden <info@movsim.org>
 * ----------------------------------------------------------------------
 * 
 * This file is part of
 * 
 * MovSim - the multi-model open-source vehicular-traffic simulator
 * 
 * MovSim is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * MovSim is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with MovSim. If not, see <http://www.gnu.org/licenses/> or
 * <http://www.movsim.org>.
 * 
 * ----------------------------------------------------------------------
 */
package org.movsim.input.model.vehicle.longitudinalmodel.impl;

import org.movsim.input.model.vehicle.longitudinalmodel.LongitudinalModelInputData;
import org.movsim.simulator.vehicles.longitudinalmodel.LongitudinalModelBase.ModelName;
import org.movsim.utilities.ObservableImpl;

// TODO: Auto-generated Javadoc
/**
 * The Class LongitudinalModelInputDataImpl.
 */
public abstract class LongitudinalModelInputDataImpl extends ObservableImpl implements LongitudinalModelInputData {

    private final ModelName modelName;

    @Override
    public abstract void resetParametersToDefault();

    /**
     * Check parameters.
     */
    protected abstract void checkParameters();

    /**
     * Parameters updated.
     */
    protected void parametersUpdated() {
        checkParameters();
        notifyObservers();
    }

    /**
     * Instantiates a new model input data impl.
     * 
     * @param modelName
     *            the model name
     */
    public LongitudinalModelInputDataImpl(ModelName modelName) {
        this.modelName = modelName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.vehicle.longModel.AccelerationModelInputData# getModelName()
     */
    @Override
    public ModelName getModelName() {
        return modelName;
    }

}
