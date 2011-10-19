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
package org.movsim.simulator.vehicles;

import org.movsim.input.model.VehicleInput;
import org.movsim.simulator.Constants;
import org.movsim.simulator.vehicles.longmodel.accelerationmodels.AccelerationModel;
import org.movsim.simulator.vehicles.longmodel.equilibrium.EquilibriumProperties;

// TODO: Auto-generated Javadoc
/**
 * The Class VehiclePrototype.
 */
public class VehiclePrototype {

    /** The label. */
    private final String label;

    /** The length. */
    private final double length;

    /** The fraction. */
    private double fraction;

    /** The reaction time. */
    private final double reactionTime;

    /** The long model. */
    private final AccelerationModel longModel;

    /** The equi properties. */
    private final EquilibriumProperties equiProperties;

    /** The vehicle input. */
    private final VehicleInput vehicleInput;
    
    private final double relativeRandomizationV0;


    /**
     * Instantiates a new vehicle prototype.
     *
     * @param label the label
     * @param fraction the fraction
     * @param longModel the long model
     * @param equilProperties the equil properties
     * @param vehicleInput the vehicle input
     * @param relativeRandomizationV0 the relative randomization v0
     */
    public VehiclePrototype(String label, double fraction, AccelerationModel longModel,
            EquilibriumProperties equilProperties, VehicleInput vehicleInput, double relativeRandomizationV0) {
        this.label = label;
        this.length = vehicleInput.getLength();
        this.reactionTime = vehicleInput.getReactionTime();
        this.fraction = fraction;
        this.longModel = longModel;
        this.equiProperties = equilProperties;
        this.vehicleInput = vehicleInput;
        this.relativeRandomizationV0 = relativeRandomizationV0;
    }

    /**
     * Label.
     * 
     * @return the String
     */
    public String getLabel() {
        return label;
    }

    /**
     * Length.
     * 
     * @return the double
     */
    public double length() {
        return length;
    }

    /**
     * Reaction time.
     * 
     * @return the double
     */
    public double reactionTime() {
        return reactionTime;
    }

    /**
     * Checks for reaction time.
     * 
     * @return true, if successful
     */
    public boolean hasReactionTime() {
        return (reactionTime + Constants.SMALL_VALUE > 0);
    }

    /**
     * Fraction.
     * 
     * @return the double
     */
    public double fraction() {
        return fraction;
    }

    /**
     * Sets the fraction.
     * 
     * @param normFraction
     *            the new fraction
     */
    public void setFraction(double normFraction) {
        this.fraction = normFraction;
    }

    /**
     * Gets the long model.
     * 
     * @return the long model
     */
    public AccelerationModel getLongModel() {
        return longModel;
    }

    /**
     * Gets the rho q max.
     * 
     * @return the rho q max
     */
    public double getRhoQMax() {
        return equiProperties.getRhoQMax();
    }

    /**
     * Gets the equilibrium speed.
     * 
     * @param rho
     *            the rho
     * @return the equilibrium speed
     */
    public double getEquilibriumSpeed(double rho) {
        return equiProperties.getVEq(rho);
    }

    /**
     * Write fundamental diagram.
     * 
     * @param filename
     *            the filename
     */
    public void writeFundamentalDiagram(String filename) {
        equiProperties.writeOutput(filename);
    }

    /**
     * Gets the vehicle input.
     * 
     * @return the vehicle input
     */
    public VehicleInput getVehicleInput() {
        return vehicleInput;
    }

    
    /**
     * Gets the relative randomization v0.
     *
     * @return the relative randomization v0
     */
    public double getRelativeRandomizationV0() {
        return relativeRandomizationV0;
    }
}
