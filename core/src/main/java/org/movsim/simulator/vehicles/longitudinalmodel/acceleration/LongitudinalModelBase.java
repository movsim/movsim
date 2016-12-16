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

import org.movsim.autogen.DistributionTypeEnum;
import org.movsim.simulator.roadnetwork.LaneSegment;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.simulator.vehicles.longitudinalmodel.acceleration.parameter.IModelParameter;
import org.movsim.utilities.MyRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

/**
 * Abstract base class for a general microscopic traffic longitudinal driver model.
 */
public abstract class LongitudinalModelBase {

    public enum ModelCategory {
        TIME_CONTINUOUS_MODEL,
        ITERATED_COUPLED_MAP_MODEL,
        CELLULAR_AUTOMATON;

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
        IDM(ModelCategory.TIME_CONTINUOUS_MODEL, "Intelligent-Driver-Model"),
        ACC(ModelCategory.TIME_CONTINUOUS_MODEL, "Adaptive-Cruise-Control-Model"),
        OVM_FVDM(ModelCategory.TIME_CONTINUOUS_MODEL, "Optimal-Velocity-Model / Full-Velocity-Difference-Model"),
        GIPPS(ModelCategory.ITERATED_COUPLED_MAP_MODEL, "Gipps-Model"),
        NEWELL(ModelCategory.ITERATED_COUPLED_MAP_MODEL, "Newell-Model"),
        KRAUSS(ModelCategory.ITERATED_COUPLED_MAP_MODEL, "Krauss-Model"),
        NSM(ModelCategory.CELLULAR_AUTOMATON, "Nagel-Schreckenberg-Model / Barlovic-Model"),
        KKW(ModelCategory.CELLULAR_AUTOMATON, "Kerner-Klenov-Wolf-Model"),
        CCS(ModelCategory.TIME_CONTINUOUS_MODEL, "Cross-Country-Skiing-Model"),
        PTM(ModelCategory.TIME_CONTINUOUS_MODEL, "Prospect-Theory Model");

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

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(LongitudinalModelBase.class);
    protected final ModelName modelName;
    private final double scalingLength;
    protected double v0RandomizationFactor = 1;

    protected LongitudinalModelBase(ModelName modelName) {
        this.modelName = modelName;
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
     * Returns the desired speed.
     * 
     * <br>
     * Overwrite {@code setRelativeRandomizationV0} if model is not able to handle such randomization. <br>
     * Remark: CCS is the only model without a desired speed, so that this method cannot be final :(
     * 
     * @return the desired speed (m/s)
     */
    public double getDesiredSpeed() {
        return v0RandomizationFactor * getParameter().getV0();
    }

    public boolean hasDesiredSpeed() {
        try {
            getDesiredSpeed();
            return true;
        } catch (UnsupportedOperationException e) {
            return false;
        }
    }

    /**
     * Returns the minimum gap in a standstill.
     * 
     * <br>
     * Can be overwritten if model does not contain such a parameter.
     * 
     * @return the minimum gap (m)
     */
    public double getMinimumGap() {
        return getParameter().getS0();
    }

    public boolean hasMinimumGap() {
        try {
            getMinimumGap();
            return true;
        } catch (UnsupportedOperationException e) {
            return false;
        }
    }

    protected abstract IModelParameter getParameter();

    /**
     * Sets the relative randomization v0.
     * 
     * <br>
     * Needs to be overwritten if not applicable to model.
     * 
     * @param relRandomizationFactor
     *            the new relative randomization v0
     */
    public void setRelativeRandomizationV0(double relRandomizationFactor, DistributionTypeEnum distributionType) {
        if (distributionType == DistributionTypeEnum.GAUSSIAN) {
            v0RandomizationFactor = MyRandom.getGaussiansDistributedRandomizedFactor(relRandomizationFactor, 3);
        } else {
            v0RandomizationFactor = MyRandom.getUniformlyDistributedRandomizedFactor(relRandomizationFactor);
        }
        Preconditions.checkArgument(v0RandomizationFactor > 0, "relative v0 randomization factor must be > 0");
        LOG.debug("randomization (of type={}) of desired speeds with randomization factor=", distributionType,
                v0RandomizationFactor);
    }

    final static double calcSmoothFraction(double speedMe, double speedFront) {
        final double widthDeltaSpeed = 1; // parameter
        double x = 0; // limiting case: consider only acceleration in vehicle's lane
        if (speedFront >= 0) {
            x = 0.5 * (1 + Math.tanh((speedMe - speedFront) / widthDeltaSpeed));
        }
        return x;
    }

    /**
     * Calculates the acceleration of vehicle me, under European lane changing rules (no "undertaking").
     * 
     * @param vCritEur
     *            critical speed under which European rules no longer apply
     * @param me
     * @param laneSegment
     * @param leftLaneSegment
     * @param alphaT
     * @param alphaV0
     * @param alphaA
     * @return the acceleration of vehicle me
     */
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
        // LOG.debug(String
        // .format("pos=%.4f, accLeft: frac=%.4f, acc=%.4f, accLeft=%.4f, accResult=%.4f, meSpeed=%.2f, frontLeftSpeed=%.2f\n",
        // me.getPosition(), frac, accInOwnLane, accLeft, accResult, me.getSpeed(), speedFront));
        // }
        return accResult;
    }

    /**
     * Calculates the acceleration of vehicle me.
     * 
     * @param me
     * @param laneSegment
     * @param alphaT
     * @param alphaV0
     * @param alphaA
     * @return the calculated acceleration
     */
    public double calcAcc(Vehicle me, LaneSegment laneSegment, double alphaT, double alphaV0, double alphaA) {
        // By default only consider the vehicle in front when calculating acceleration.
        // LDMs that consider more than the front vehicle should override this method.
        final Vehicle frontVehicle = laneSegment.frontVehicle(me);
        return calcAcc(me, frontVehicle, alphaT, alphaV0, alphaA);
    }

    /**
     * Calculates the acceleration of vehicle me.
     * 
     * @param me
     * @param frontVehicle
     * @param alphaT
     * @param alphaV0
     * @param alphaA
     * @return the calculated acceleration
     */
    public abstract double calcAcc(Vehicle me, Vehicle frontVehicle, double alphaT, double alphaV0, double alphaA);

    /**
     * Calculates the acceleration of vehicle me.
     * 
     * @param me
     * @param frontVehicle
     * @return the calculated acceleration
     */
    public double calcAcc(Vehicle me, Vehicle frontVehicle) {
        return calcAcc(me, frontVehicle, 1.0, 1.0, 1.0);
    }

    /**
     * Calculates the vehicular acceleration.
     * 
     * @param s
     *            the s
     * @param v
     *            the v
     * @param dv
     *            the dv
     * @return the calculated acceleration
     */
    public abstract double calcAccSimple(double s, double v, double dv);

}
