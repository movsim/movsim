/**
 * Copyright (C) 2010, 2011 by Arne Kesting <movsim@akesting.de>, 
 *                             Martin Treiber <treibi@mtreiber.de>,
 *                             Ralph Germ <germ@ralphgerm.de>,
 *                             Martin Budden <mjbudden@gmail.com>
 *
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
package org.movsim.simulator.vehicles.longmodel.accelerationmodels;

import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.simulator.vehicles.VehicleContainer;

// TODO: Auto-generated Javadoc
/**
 * The Interface AccelerationModel.
 */
public interface AccelerationModel {

    /**
     * Model name.
     * 
     * @return the string
     */
    String modelName();

    /**
     * Checks if is cA.
     * 
     * @return true, if is cA
     */
    boolean isCA();

    /**
     * Checks if is iterated map.
     * 
     * @return true, if is iterated map
     */
    boolean isIteratedMap();

    /**
     * Gets the model category.
     * 
     * @return the model category
     */
    int getModelCategory();

    /**
     * Gets the required update time.
     * 
     * @return the required update time
     */
    double getRequiredUpdateTime();

    /**
     * Parameter v0.
     * 
     * @return the double
     */
    double parameterV0();

    /**
     * Acc.
     * 
     * @param me
     *            the me
     * @param vehContainer
     *            the veh container
     * @param alphaT
     *            the alpha t
     * @param alphaV0
     *            the alpha v0
     * @param alphaA
     *            the alpha a
     * @return the double
     */
    double acc(Vehicle me, VehicleContainer vehContainer, double alphaT, double alphaV0, double alphaA);

    /**
     * Acc simple.
     * 
     * @param s
     *            the s
     * @param v
     *            the v
     * @param dv
     *            the dv
     * @return the double
     */
    double accSimple(double s, double v, double dv);

}
