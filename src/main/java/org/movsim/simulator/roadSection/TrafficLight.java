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
package org.movsim.simulator.roadSection;

// TODO: Auto-generated Javadoc
/**
 * The Interface TrafficLight.
 */
public interface TrafficLight {

    // cycle is GREEN --> GREEN_RED --> RED --> RED_GREEN --> GREEN
    /** The GREE n_ light. */
    final int GREEN_LIGHT = 0;
    
    /** The GREE n_ re d_ light. */
    final int GREEN_RED_LIGHT = 1;
    
    /** The RE d_ light. */
    final int RED_LIGHT = 2;
    
    /** The RE d_ gree n_ light. */
    final int RED_GREEN_LIGHT = 3;

    /**
     * Position.
     * 
     * @return the double
     */
    double position();

    /**
     * Checks if is green.
     * 
     * @return true, if is green
     */
    boolean isGreen();

    /**
     * Checks if is green red.
     * 
     * @return true, if is green red
     */
    boolean isGreenRed();

    /**
     * Checks if is red.
     * 
     * @return true, if is red
     */
    boolean isRed();

    /**
     * Checks if is red green.
     * 
     * @return true, if is red green
     */
    boolean isRedGreen();

    /**
     * Status.
     * 
     * @return the int
     */
    int status();

    // relativ, fuer Fahrzeug
    /**
     * Gets the time for next green.
     * 
     * @param alpha
     *            the alpha
     * @return the time for next green
     */
    double getTimeForNextGreen(double alpha);

    /**
     * Gets the time for next red.
     * 
     * @param alpha
     *            the alpha
     * @return the time for next red
     */
    double getTimeForNextRed(double alpha);

    // absolut, fuer plots
    /**
     * Gets the time for next green.
     * 
     * @return the time for next green
     */
    double getTimeForNextGreen();

    /**
     * Gets the time for next red.
     * 
     * @return the time for next red
     */
    double getTimeForNextRed();

    /**
     * Gets the current cycle time.
     * 
     * @return the current cycle time
     */
    double getCurrentCycleTime();

    /**
     * Gets the cycle time.
     * 
     * @return the cycle time
     */
    double getCycleTime();

    /**
     * Gets the crit time for next main phase.
     * 
     * @param alpha
     *            the alpha
     * @return the crit time for next main phase
     */
    double getCritTimeForNextMainPhase(double alpha);

    /**
     * Update.
     * 
     * @param time
     *            the time
     */
    void update(double time);

}
