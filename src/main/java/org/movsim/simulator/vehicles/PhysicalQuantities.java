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

/**
 * The Class PhysicalQuantities. Converts scaled to physical SI units needed for
 * CAs /cellular automata)
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
    public PhysicalQuantities(final Vehicle veh) {
        this.me = veh;

        xScale = veh.getAccelerationModel().getScalingLength();

        vScale = xScale / tscale;
        accScale = Math.pow(xScale / tscale, 2);
        rhoScale = 1. / xScale;
    }

    /**
     * Gets the length.
     * 
     * @return the length
     */
    public double getLength() {
        return xScale * me.getLength();
    }

    /**
     * Gets the width.
     * 
     * @return the width
     */
    public double getWidth() {
        return me.getWidth();
    }

    /**
     * Gets the position.
     * 
     * @return the position
     */
    public double getPosition() {
        return xScale * me.getPosition();
    }

    /**
     * Pos front bumper.
     * 
     * @return the double
     */
    public double posFrontBumper() {
        return xScale * me.posFrontBumper();
    }

    /**
     * Pos rear bumper.
     * 
     * @return the double
     */
    public double posRearBumper() {
        return xScale * me.posRearBumper();
    }

    /**
     * Gets the position old.
     * 
     * @return the position old
     */
    public double getPositionOld() {
        return xScale * me.getPositionOld();
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

    /**
     * Gets the net distance.
     * 
     * @param vehFront
     *            the veh front
     * @return the net distance
     */
    public double getNetDistance(final Moveable vehFront) {
        return xScale * me.getNetDistance(vehFront);
    }

    /**
     * Gets the rel speed.
     * 
     * @param vehFront
     *            the veh front
     * @return the rel speed
     */
    public double getRelSpeed(final Moveable vehFront) {
        return vScale * me.getRelSpeed(vehFront);
    }

}
