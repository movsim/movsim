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
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.simulator.vehicles.VehicleContainer;
import org.movsim.utilities.Observer;
import org.movsim.utilities.impl.ScalingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class LongitudinalModel.
 */
public abstract class AccelerationModelAbstract implements Observer {

    public enum ModelCategory {
        CONTINUOUS_MODEL, INTERATED_MAP_MODEL, CELLULAR_AUTOMATON;
        
        public boolean isCA() {
            return (this == CELLULAR_AUTOMATON);
        }

        public boolean isIteratedMap() {
            return (this == INTERATED_MAP_MODEL);
        }
    }
    
    
    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(AccelerationModelAbstract.class);

    /** The model name. */
    private final String modelName;
    
    
    private final double scalingLength;

    /** The model category. */
    private final ModelCategory modelCategory;

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
    public AccelerationModelAbstract(String modelName, ModelCategory modelCategory, AccelerationModelInputData parameters) {
        this.modelName = modelName;
        this.modelCategory = modelCategory;
        this.parameters = parameters;
        this.id = MyRandom.nextInt();
        this.scalingLength = ScalingHelper.getScalingLength(modelName);
        parameters.registerObserver(this);
    }

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
     * Gets the model category.
     * 
     * @return the model category
     */
    public ModelCategory getModelCategory() {
        return modelCategory;
    }
    
    /**
     * Checks if is cellular automaton.
     * 
     * @return true, if is cA
     */
    public boolean isCA() {
        return modelCategory.isCA();
    }

    /**
     * Checks if is iterated map.
     * 
     * @return true, if is iterated map
     */
    public boolean isIteratedMap() {
        return modelCategory.isIteratedMap();
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
        logger.debug("randomization of desired speeds: v0={}, new v0={}", getDesiredSpeedParameterV0(), newV0);
        setDesiredSpeedV0(newV0);
    }   

    protected double calcSmoothFraction(double speedMe, double speedFront){
        final double widthDeltaSpeed = 1;  // parameter
        double x = 0; // limiting case: consider only acceleration in vehicle's lane
        if(speedFront >= 0 ){
            x = 0.5*( 1+Math.tanh( (speedMe-speedFront)/widthDeltaSpeed) );
        }   
        return x;
    }
    
    
    public abstract double calcAcc(Vehicle me, VehicleContainer vehContainer, double alphaT, double alphaV0, double alphaA);
    
    public double calcAccEur(double vCritEur, Vehicle me, VehicleContainer vehContainer,
            VehicleContainer vehContainerLeftLane, double alphaT, double alphaV0, double alphaA) {

        // calculate normal acceleration in own lane
        final double accInOwnLane = calcAcc(me, vehContainer, alphaT, alphaV0, alphaA);

        // no lane on left-hand side
        if (vehContainerLeftLane == null) {
            return accInOwnLane;
        }

        // check left-vehicle's speed

        final Vehicle newFrontLeft = vehContainerLeftLane.getLeader(me);
        double speedFront = (newFrontLeft != null) ? newFrontLeft.getSpeed() : -1;

        // condition me.getSpeed() > speedFront will be evaluated by softer tanh
        // condition below
        double accLeft = (speedFront > vCritEur) ? calcAcc(me, vehContainerLeftLane, alphaT, alphaV0, alphaA)
                : Double.MAX_VALUE;

        // avoid hard switching by condition vMe>vLeft needed in European
        // acceleration rule

        final double frac = calcSmoothFraction(me.getSpeed(), speedFront);
        final double accResult = frac * Math.min(accInOwnLane, accLeft) + (1 - frac) * accInOwnLane;

//        if (speedFront != -1) {
//            logger.debug(String
//                    .format("pos=%.4f, accLeft: frac=%.4f, acc=%.4f, accLeft=%.4f, accResult=%.4f, meSpeed=%.2f, frontLeftSpeed=%.2f\n",
//                            me.getPosition(), frac, accInOwnLane, accLeft, accResult, me.getSpeed(), speedFront));
//        }
        return accResult;
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
