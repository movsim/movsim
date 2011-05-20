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
package org.movsim.input.model.vehicle.longModel;

// TODO: Auto-generated Javadoc
/**
 * The Interface AccelerationModelInputDataACC.
 */
public interface AccelerationModelInputDataACC {

    /**
     * Gets the model name.
     * 
     * @return the model name
     */
    String getModelName();

    /**
     * Gets the v0.
     * 
     * @return the v0
     */
    double getV0();

    /**
     * Gets the t.
     * 
     * @return the t
     */
    double getT();

    /**
     * Gets the s0.
     * 
     * @return the s0
     */
    double getS0();

    /**
     * Gets the s1.
     * 
     * @return the s1
     */
    double getS1();

    /**
     * Gets the delta.
     * 
     * @return the delta
     */
    double getDelta();

    /**
     * Gets the a.
     * 
     * @return the a
     */
    double getA();

    /**
     * Gets the b.
     * 
     * @return the b
     */
    double getB();

    /**
     * Gets the coolness.
     * 
     * @return the coolness
     */
    double getCoolness();

}