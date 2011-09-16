/**
 * Copyright (C) 2010, 2011 by Arne Kesting, Martin Treiber,
 *                             Ralph Germ, Martin Budden
 *                             <info@movsim.org>
 * ----------------------------------------------------------------------
 * 
 *  This file is part of 
 *  
 *  MovSim - the multi-model open-source vehicular-traffic simulator 
 *
 *  MovSim is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  MovSim is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with MovSim.  If not, see <http://www.gnu.org/licenses/> or
 *  <http://www.movsim.org>.
 *  
 * ----------------------------------------------------------------------
 */
package org.movsim.simulator.vehicles.longmodel.accelerationmodels.impl;

import org.movsim.input.model.vehicle.longModel.AccelerationModelInputData;
import org.movsim.simulator.impl.MyRandom;
import org.movsim.simulator.vehicles.longmodel.accelerationmodels.AccelerationModelCategory;
import org.movsim.utilities.Observer;
import org.movsim.utilities.impl.ScalingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class LongitudinalModel.
 */
public abstract class AccelerationModelAbstract implements Observer {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(AccelerationModelAbstract.class);

    /** The model name. */
    private final String modelName;
    
    
    private final double scalingLength;

    /** The model category. */
    private final int modelCategory;

    /** The parameters. */
    public AccelerationModelInputData parameters;

    /**
     * Inits the parameters.
     */
    protected abstract void initParameters();

    /** The id. */
    protected long id;

    /**
     * Instantiates a new longitudinal model impl.
     * 
     * @param modelName
     *            the model name
     * @param modelCategory
     *            the model category
     * @param parameters
     *            the parameters
     */
    public AccelerationModelAbstract(String modelName, int modelCategory, AccelerationModelInputData parameters) {
        this.modelName = modelName;
        this.modelCategory = modelCategory;
        this.parameters = parameters;
        this.id = MyRandom.nextInt();
        this.scalingLength = ScalingHelper.getScalingLength(modelName);
        parameters.registerObserver(this);
    }

    // public LongitudinalModel(String modelName, int modelCategory) {
    // this.modelName = modelName;
    // this.modelCategory = modelCategory;
    // }

    /**
     * Removes the observer.
     */
    public void removeObserver() {
        if (parameters != null) {
            parameters.removeObserver(this);
        }
    }

    /**
     * Model name.
     * 
     * @return the string
     */
    public String modelName() {
        return modelName;
    }

    /**
     * Checks if is cellular automaton.
     * 
     * @return true, if is cA
     */
    public boolean isCA() {
        return (modelCategory == AccelerationModelCategory.CELLULAR_AUTOMATON);
    }

    /**
     * Checks if is iterated map.
     * 
     * @return true, if is iterated map
     */
    public boolean isIteratedMap() {
        return (modelCategory == AccelerationModelCategory.INTERATED_MAP_MODEL);
    }

    /**
     * Gets the model category.
     * 
     * @return the model category
     */
    public int getModelCategory() {
        return modelCategory;
    }

    /**
     * Gets the scaling length.
     *
     * @return the scaling length
     */
    public double getScalingLength() {
        return scalingLength;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.utilities.Observer#notifyObserver()
     */
    @Override
    public void notifyObserver() {
        initParameters();
        logger.debug("observer notified");
    }

    /**
     * Sets the relative randomization v0.
     *
     * @param relRandomizationFactor the new relative randomization v0
     */
    public void setRelativeRandomizationV0(double relRandomizationFactor) {
        final double equalRandom = 2 * MyRandom.nextDouble() - 1; // in [-1,1]
        final double newV0 = getDesiredSpeedParameterV0() * (1 + relRandomizationFactor * equalRandom);
        logger.info("randomization of desired speeds: v0={}, new v0={}", getDesiredSpeedParameterV0(), newV0);
        setDesiredSpeedV0(newV0);
    }   
    
    /**
     * Gets the desired speed parameter v0.
     *
     * @return the desired speed parameter v0
     */
    public abstract double getDesiredSpeedParameterV0();
    
    /**
     * Sets the desired speed v0.
     *
     * @param v0 the new desired speed v0
     */
    protected abstract void setDesiredSpeedV0(double v0);
    
}
