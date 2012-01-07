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
package org.movsim.input.model.vehicle.longitudinalmodel;

// TODO: Auto-generated Javadoc
/**
 * The Interface LongitudinalModelInputDataNSM.
 */
public interface LongitudinalModelInputDataNSM extends LongitudinalModelInputData {

    /**
     * Gets the v0.
     * 
     * @return the v0
     */
    double getV0();

    /**
     * Gets the slowdown.
     * 
     * @return the slowdown
     */
    double getSlowdown();

    /**
     * Gets the slow to start.
     * 
     * @return the slow to start
     */
    double getSlowToStart();

    /**
     * Gets the v0 default.
     * 
     * @return the v0 default
     */
    double getV0Default();

    /**
     * Gets the p slowdown.
     * 
     * @return the p slowdown
     */
    double getpSlowdown();

    /**
     * Gets the p slowdown default.
     * 
     * @return the p slowdown default
     */
    double getpSlowdownDefault();

    /**
     * Gets the p slow to start.
     * 
     * @return the p slow to start
     */
    double getpSlowToStart();

    /**
     * Gets the p slow to start default.
     * 
     * @return the p slow to start default
     */
    double getpSlowToStartDefault();

    /**
     * Sets the v0.
     * 
     * @param v0
     *            the new v0
     */
    void setV0(double v0);

    /**
     * Sets the p slowdown.
     * 
     * @param pSlowdown
     *            the new p slowdown
     */
    void setpSlowdown(double pSlowdown);

    /**
     * Sets the p slow to start.
     * 
     * @param pSlowToStart
     *            the new p slow to start
     */
    void setpSlowToStart(double pSlowToStart);

}