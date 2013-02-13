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
package org.movsim.consumption.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class InstantaneousPowerModelImpl implements InstantaneousPowerModel {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(InstantaneousPowerModelImpl.class);

    private final VehicleAttributes vehicle;

    InstantaneousPowerModelImpl(VehicleAttributes vehicleAttributes) {
        this.vehicle = vehicleAttributes;
    }

    @Override
    public double getFreeWheelingDeceleration(double speed) {
        // TODO consider slope gradient
        return -(ConsumptionConstants.GRAVITATION * vehicle.constantFrictionCoefficient()
                + ConsumptionConstants.GRAVITATION * vehicle.speedFrictionCoefficient() * speed + vehicle.cwValue()
                * ConsumptionConstants.RHO_AIR * vehicle.crossSectionSurface() * speed * speed / (2 * vehicle.mass()));
    }

    @Override
    public double getMechanicalPower(double speed, double acceleration, double slopeGrade) {
        return speed * getMechanicalForce(speed, acceleration, slopeGrade);
    }

    private double getMechanicalForce(double v, double acc, double slopeGrade) {
        final double c = vehicle.mass() * ConsumptionConstants.GRAVITATION
                * (vehicle.constantFrictionCoefficient() + slopeGrade);
        final double d = vehicle.mass() * ConsumptionConstants.GRAVITATION * vehicle.speedFrictionCoefficient();
        final double e = 0.5 * ConsumptionConstants.RHO_AIR * vehicle.cwValue() * vehicle.crossSectionSurface();
        // System.out.println("v=" + v + ", acc=" + acc + ", slope gradient=" + slopeGrade + ", c=" + c + ", d=" + d
        // + ", e=" + e);
        return vehicle.mass() * acc + c + d * v + e * v * v;
    }

}
