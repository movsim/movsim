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
package org.movsim.simulator.vehicles.longitudinalmodel;

import org.movsim.input.model.vehicle.longitudinalmodel.LongitudinalModelInputData;
import org.movsim.simulator.roadnetwork.LaneSegment;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.utilities.MyRandom;
import org.movsim.utilities.ScalingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for a general microscopic traffic longitudinal driver model.
 */
public abstract class LongitudinalModelBase{

    public enum ModelCategory {
        TIME_CONTINUOUS_MODEL, ITERATED_COUPLED_MAP_MODEL, CELLULAR_AUTOMATON;

        public boolean isCA() {
            return (this == CELLULAR_AUTOMATON);
        }

        public boolean isIteratedMap() {
            return (this == ITERATED_COUPLED_MAP_MODEL);
        }

        @Override
        public String toString() {
            return name();
        }
    }

    public enum ModelName {
        IDM(ModelCategory.TIME_CONTINUOUS_MODEL, "Intelligent-Driver-Model"), ACC(ModelCategory.TIME_CONTINUOUS_MODEL,
                "Adaptive-Cruise-Control-Model"), OVM_FVDM(ModelCategory.TIME_CONTINUOUS_MODEL,
                "Optimal-Velocity-Model / Full-Velocity-Difference-Model"), GIPPS(ModelCategory.ITERATED_COUPLED_MAP_MODEL,
                "Gipps-Model"), NEWELL(ModelCategory.ITERATED_COUPLED_MAP_MODEL, "Newell-Model"),KRAUSS(
                ModelCategory.ITERATED_COUPLED_MAP_MODEL, "Krauss-Model"), NSM(ModelCategory.CELLULAR_AUTOMATON,
                "Nagel-Schreckenberg-Model / Barlovic-Model"), KKW(ModelCategory.CELLULAR_AUTOMATON,
                "Kerner-Klenov-Wolf-Model");

        private final ModelCategory modelCategory;

        private final String detailedName;

        private ModelName(ModelCategory modelCategory, String detailedName) {
            this.modelCategory = modelCategory;
            this.detailedName = detailedName;
        }

        public final ModelCategory getCategory() {
            return modelCategory;
        }

        public final String getDetailedName() {
            return detailedName;
        }

        public final String getShortName() {
            return name();
        }

        @Override
        public String toString() {
            return name();
        }
    }

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(LongitudinalModelBase.class);
    private final ModelName modelName;
    private final double scalingLength;
    protected final LongitudinalModelInputData parameters;
    protected long id;

    /**
     * Constructor.
     * 
     * @param modelName
     *            the model name
     * @param parameters
     *            the parameters
     */
    protected LongitudinalModelBase(ModelName modelName, LongitudinalModelInputData parameters) {
        this.modelName = modelName;
        this.parameters = parameters;
        this.scalingLength = ScalingHelper.getScalingLength(modelName);
    }

    /**
     * Model name.
     * 
     * @return the string
     */
    public ModelName modelName() {
        return modelName;
    }

    /**
     * Gets the model category.
     * 
     * @return the model category
     */
    public ModelCategory getModelCategory() {
        return modelName.getCategory();
    }
    
    /**
     * Checks if is cellular automaton.
     * 
     * @return true, if is cA
     */
    public boolean isCA() {
        return modelName.getCategory().isCA();
    }

    /**
     * Checks if is iterated map.
     * 
     * @return true, if is iterated map
     */
    public boolean isIteratedMap() {
        return modelName.getCategory().isIteratedMap();
    }

    /**
     * Gets the scaling length.
     * 
     * @return the scaling length
     */
    public double getScalingLength() {
        return scalingLength;
    }

    /**
     * Sets the vehicle's desired speed (m/s).
     * 
     * @param v0
     *            desired speed (m/s)
     */
    protected abstract void setDesiredSpeed(double v0);

    /**
     * Returns the desired speed in free traffic.
     * 
     * @return the desired speed (m/s)
     */
    public abstract double getDesiredSpeed();

    /**
     * Sets the relative randomization v0.
     * 
     * @param relRandomizationFactor
     *            the new relative randomization v0
     */
    public void setRelativeRandomizationV0(double relRandomizationFactor) {
        final double equalRandom = 2 * MyRandom.nextDouble() - 1; // in [-1,1]
        final double newV0 = getDesiredSpeed() * (1 + relRandomizationFactor * equalRandom);
        logger.debug("randomization of desired speeds: v0={}, new v0={}", getDesiredSpeed(), newV0);
        setDesiredSpeed(newV0);
    }

    protected double calcSmoothFraction(double speedMe, double speedFront) {
        final double widthDeltaSpeed = 1; // parameter
        double x = 0; // limiting case: consider only acceleration in vehicle's lane
        if (speedFront >= 0) {
            x = 0.5 * (1 + Math.tanh((speedMe - speedFront) / widthDeltaSpeed));
        }
        return x;
    }

    public double calcAccEur(double vCritEur, Vehicle me, LaneSegment laneSegment, LaneSegment leftLaneSegment,
            double alphaT, double alphaV0, double alphaA) {

        // calculate normal acceleration in own lane
        final double accInOwnLane = calcAcc(me, laneSegment, alphaT, alphaV0, alphaA);

        // no lane on left-hand side
        if (leftLaneSegment == null) {
            return accInOwnLane;
        }

        // check left-vehicle's speed

        final Vehicle newFrontLeft = leftLaneSegment.frontVehicle(me);
        if (newFrontLeft == null) {
            return accInOwnLane;
        }
        final double speedFront = newFrontLeft.getSpeed();
        if (speedFront <= vCritEur) {
            return accInOwnLane;
        }

        // condition me.getSpeed() > speedFront will be evaluated by softer tanh
        // condition below
        final double accLeft = calcAcc(me, leftLaneSegment, alphaT, alphaV0, alphaA);

        // avoid hard switching by condition vMe>vLeft needed in European
        // acceleration rule
        final double frac = calcSmoothFraction(me.getSpeed(), speedFront);
        final double accResult = frac * Math.min(accInOwnLane, accLeft) + (1 - frac) * accInOwnLane;

        // if (speedFront != -1) {
        // logger.debug(String
        // .format("pos=%.4f, accLeft: frac=%.4f, acc=%.4f, accLeft=%.4f, accResult=%.4f, meSpeed=%.2f, frontLeftSpeed=%.2f\n",
        // me.getPosition(), frac, accInOwnLane, accLeft, accResult, me.getSpeed(), speedFront));
        // }
        return accResult;
    }

    public abstract double calcAcc(Vehicle me, LaneSegment laneSegment, double alphaT, double alphaV0, double alphaA);

    public abstract double calcAcc(final Vehicle me, final Vehicle frontVehicle);

    /**
     * Calc acc simple.
     * 
     * @param s
     *            the s
     * @param v
     *            the v
     * @param dv
     *            the dv
     * @return the double
     */
    public abstract double calcAccSimple(double s, double v, double dv);
}
