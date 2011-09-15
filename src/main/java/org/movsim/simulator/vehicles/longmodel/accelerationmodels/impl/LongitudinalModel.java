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
public abstract class LongitudinalModel implements Observer {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(LongitudinalModel.class);

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
    public LongitudinalModel(String modelName, int modelCategory, AccelerationModelInputData parameters) {
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
     * Gets the required update time.
     * 
     * @return the required update time
     */
    public abstract double getRequiredUpdateTime();

    /**
     * Parameter V0.
     * 
     * @return the double
     */
    public abstract double parameterV0();
    
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

    
    
}
