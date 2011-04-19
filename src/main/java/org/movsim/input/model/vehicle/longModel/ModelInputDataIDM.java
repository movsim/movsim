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
package org.movsim.input.model.vehicle.longModel;

// TODO: Auto-generated Javadoc
/**
 * The Interface ModelInputDataIDM.
 */
public interface ModelInputDataIDM {

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
    public abstract double getV0();

    /**
     * Gets the t.
     * 
     * @return the t
     */
    public abstract double getT();

    /**
     * Gets the s0.
     * 
     * @return the s0
     */
    public abstract double getS0();

    /**
     * Gets the s1.
     * 
     * @return the s1
     */
    public abstract double getS1();

    /**
     * Gets the delta.
     * 
     * @return the delta
     */
    public abstract double getDelta();

    /**
     * Gets the a.
     * 
     * @return the a
     */
    public abstract double getA();

    /**
     * Gets the b.
     * 
     * @return the b
     */
    public abstract double getB();

}