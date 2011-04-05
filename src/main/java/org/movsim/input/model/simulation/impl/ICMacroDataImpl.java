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
package org.movsim.input.model.simulation.impl;

import java.util.Map;

import org.movsim.input.model.simulation.ICMacroData;


public class ICMacroDataImpl implements ICMacroData {

    private double x;
	private double rho;  // in 1/m
	private double speed; // in m/s, (default value)
	
	public ICMacroDataImpl(Map<String, String> map) {
	    this.x   = Double.parseDouble(map.get("x"));
        this.rho = Double.parseDouble(map.get("rho_invkm"))/1000.; //convert to SI
        this.speed = Double.parseDouble(map.get("v"));
	}

    /* (non-Javadoc)
     * @see org.movsim.input.model.simulation.impl.ICMacroData#getX()
     */
    public double getX() {
        return x;
    }
    
    /* (non-Javadoc)
     * @see org.movsim.input.model.simulation.impl.ICMacroData#getRho()
     */
    public double getRho() {
        return rho;
    }

    /* (non-Javadoc)
     * @see org.movsim.input.model.simulation.impl.ICMacroData#getSpeed()
     */
    public double getSpeed() {
        return speed;
    }
    
    

}
