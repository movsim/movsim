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
 * The Interface AccelerationModelInputDataKKW.
 */
public interface AccelerationModelInputDataKKW extends AccelerationModelInputData {

    /**
     * Gets the v0.
     * 
     * @return the v0
     */
    double getV0();

    /**
     * Gets the k.
     * 
     * @return the k
     */
    double getK();

    /**
     * Gets the pb0.
     * 
     * @return the pb0
     */
    double getPb0();

    /**
     * Gets the pb1.
     * 
     * @return the pb1
     */
    double getPb1();

    /**
     * Gets the pa1.
     * 
     * @return the pa1
     */
    double getPa1();

    /**
     * Gets the pa2.
     * 
     * @return the pa2
     */
    double getPa2();

    /**
     * Gets the vp.
     * 
     * @return the vp
     */
    double getVp();

    /**
     * Gets the v0 default.
     * 
     * @return the v0 default
     */
    double getV0Default();

    /**
     * Gets the k default.
     * 
     * @return the k default
     */
    double getkDefault();

    /**
     * Gets the pb0 default.
     * 
     * @return the pb0 default
     */
    double getPb0Default();

    /**
     * Gets the pb1 default.
     * 
     * @return the pb1 default
     */
    double getPb1Default();

    /**
     * Gets the pa1 default.
     * 
     * @return the pa1 default
     */
    double getPa1Default();

    /**
     * Gets the pa2 default.
     * 
     * @return the pa2 default
     */
    double getPa2Default();

    /**
     * Gets the vp default.
     * 
     * @return the vp default
     */
    double getVpDefault();

    /**
     * Sets the v0.
     * 
     * @param v0
     *            the new v0
     */
    void setV0(double v0);

    /**
     * Sets the k.
     * 
     * @param k
     *            the new k
     */
    void setK(double k);

    /**
     * Sets the pb0.
     * 
     * @param pb0
     *            the new pb0
     */
    void setPb0(double pb0);

    /**
     * Sets the pb1.
     * 
     * @param pb1
     *            the new pb1
     */
    void setPb1(double pb1);

    /**
     * Sets the pa1.
     * 
     * @param pa1
     *            the new pa1
     */
    void setPa1(double pa1);

    /**
     * Sets the pa2.
     * 
     * @param pa2
     *            the new pa2
     */
    void setPa2(double pa2);

    /**
     * Sets the vp.
     * 
     * @param vp
     *            the new vp
     */
    void setVp(double vp);

}