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

import org.movsim.input.model.simulation.ICMicroData;



public class ICMicroDataImpl implements ICMicroData {

    private double initPosition;
	private double initSpeed;
	private int initLane; // most right lane: Constants.MOST_RIGHT_LANE
	private String typeLabel;  // empty string if no type

	public ICMicroDataImpl(Map<String, String> map) {
	    this.initPosition = Double.parseDouble(map.get("x"));
        this.initSpeed = Double.parseDouble(map.get("v"));
        this.initLane  = Integer.parseInt(map.get("lane"));
        this.typeLabel = map.get("label");
	}

	/* (non-Javadoc)
     * @see org.movsim.input.model.simulation.impl.ICMicroData#getX()
     */
	public double getX() {
        return initPosition;
    }

    /* (non-Javadoc)
     * @see org.movsim.input.model.simulation.impl.ICMicroData#getSpeed()
     */
    public double getSpeed() {
        return initSpeed;
    }
    
    
    public int getInitLane() {
        return initLane;
    }

    /* (non-Javadoc)
     * @see org.movsim.input.model.simulation.impl.ICMicroData#getType()
     */
    public String getLabel(){
        return typeLabel; 
    }

}
