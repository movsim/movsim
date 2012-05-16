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
package org.movsim.simulator.vehicles;

/**
 * The Class PhysicalQuantities. Converts scaled to physical SI units needed for CAs /cellular automata)
 */
public class PhysicalQuantities {

    private final double tscale = 1;

    private final double xScale;
    private final double vScale;
    private final double accScale;

    private final double rhoScale;

    private final Vehicle me;

    /**
     * Instantiates a new physical quantities.
     * 
     * @param veh
     *            the veh
     */
    public PhysicalQuantities(Vehicle veh) {
        this.me = veh;

        xScale = veh.getLongitudinalModel() == null ? 1.0 : veh.getLongitudinalModel().getScalingLength();

        vScale = xScale / tscale;
        accScale = xScale * Math.pow(tscale, 2);
        rhoScale = 1. / xScale;
    }

    /**
     * Returns this vehicle's length.
     * 
     * @return vehicle's length, in meters
     */
    public double getLength() {
        return xScale * me.getLength();
    }

    /**
     * Returns this vehicle's width.
     * 
     * @return vehicle's width, in meters
     */
    public double getWidth() {
        return me.getWidth();
    }

    /**
     * Returns the position of the middle of this vehicle.
     * 
     * @return position of the middle of this vehicle
     */
    public double getMidPosition() {
        return xScale * me.getMidPosition();
    }

    /**
     * Pos front bumper.
     * 
     * @return the double
     */
    public double getFrontPosition() {
        return xScale * me.getFrontPosition();
    }

    /**
     * Pos rear bumper.
     * 
     * @return the double
     */
    public double getRearPosition() {
        return xScale * me.getRearPosition();
    }

    /**
     * Gets the position old.
     * 
     * @return the position old
     */
    public double getFrontPositionOld() {
        return xScale * me.getFrontPositionOld();
    }

    /**
     * Gets the speed.
     * 
     * @return the speed
     */
    public double getSpeed() {
        return vScale * me.getSpeed();
    }

    /**
     * Gets the acc.
     * 
     * @return the acc
     */
    public double getAcc() {
        return accScale * me.getAcc();
    }

    public double accModel() {
        return accScale * me.accModel();
    }

    /**
     * Gets the net distance.
     * 
     * @param vehFront
     *            the veh front
     * @return the net distance
     */
    public double getNetDistance(Vehicle vehFront) {
        return xScale * me.getNetDistance(vehFront);
    }

    public final double totalTravelDistance() {
        return xScale * me.totalTravelDistance();
    }

    /**
     * Gets the rel speed.
     * 
     * @param vehFront
     *            the veh front
     * @return the rel speed
     */
    public double getRelSpeed(Vehicle vehFront) {
        return vScale * me.getRelSpeed(vehFront);
    }

    public double getxScale() {
        return xScale;
    }

    public double getvScale() {
        return vScale;
    }

    public double getAccScale() {
        return accScale;
    }
}
