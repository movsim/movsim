/**
 * Copyright (C) 2010, 2011 by Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden
 *                             <movsim.org@gmail.com>
 * ---------------------------------------------------------------------------------------------------------------------
 * 
 *  This file is part of 
 *  
 *  MovSim - the multi-model open-source vehicular-traffic simulator 
 *
 *  MovSim is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 *  version.
 *
 *  MovSim is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 *  warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with MovSim.
 *  If not, see <http://www.gnu.org/licenses/> or <http://www.movsim.org>.
 *  
 * ---------------------------------------------------------------------------------------------------------------------
 */
package org.movsim.input.model.vehicle.longitudinalmodel;

// TODO: Auto-generated Javadoc
/**
 * The Interface LongitudinalModelInputDataKrauss.
 * 
 */
public interface LongitudinalModelInputDataKrauss extends LongitudinalModelInputData {

    /**
     * Gets the v0.
     * 
     * @return the v0
     */
    double getV0();

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
     * Gets the s0.
     * 
     * @return the s0
     */
    double getS0();

    /**
     * Gets the v0 default.
     * 
     * @return the v0 default
     */
    public double getV0Default();

    /**
     * Gets the a default.
     * 
     * @return the a default
     */
    public double getaDefault();

    /**
     * Gets the b default.
     * 
     * @return the b default
     */
    public double getbDefault();

    /**
     * Gets the s0 default.
     * 
     * @return the s0 default
     */
    public double getS0Default();

    /**
     * Sets the v0.
     * 
     * @param v0
     *            the new v0
     */
    public void setV0(double v0);

    /**
     * Sets the a.
     * 
     * @param a
     *            the new a
     */
    public void setA(double a);

    /**
     * Sets the b.
     * 
     * @param b
     *            the new b
     */
    public void setB(double b);

    /**
     * Sets the s0.
     * 
     * @param s0
     *            the new s0
     */
    public void setS0(double s0);

    /**
     * Gets the epsilon.
     * 
     * @return the epsilon
     */
    double getEpsilon();

    /**
     * Sets the epsilon.
     * 
     * @param bChangeValue
     *            the new epsilon
     */
    void setEpsilon(double bChangeValue);
}
