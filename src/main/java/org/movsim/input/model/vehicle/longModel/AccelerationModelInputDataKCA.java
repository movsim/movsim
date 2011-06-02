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
 * The Interface AccelerationModelInputDataKCA.
 */
public interface AccelerationModelInputDataKCA extends AccelerationModelInputData{

   

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
    
    
    double getV0Default();

    double getkDefault();

    double getPb0Default();

    double getPb1Default();

    double getPa1Default();

    double getPa2Default();

    double getVpDefault();
    
    
    void setV0(double v0);

    void setK(double k);
    
    void setPb0(double pb0);

    void setPb1(double pb1);

    void setPa1(double pa1);

    void setPa2(double pa2);

    void setVp(double vp);


}