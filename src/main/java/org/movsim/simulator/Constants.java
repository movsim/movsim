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
package org.movsim.simulator;

public interface Constants {
	
    final String COMMENT_CHAR = "#";
    
	final double SMALL_VALUE = 1e-7;
	
	final double MAX_VEHICLE_SPEED = 200/3.6;
	
	final int MOST_RIGHT_LANE = 1; // increment lane index for further lanes to the left
	 
	final String MODEL_NAME_IDM = "IDM";
	final String MODEL_NAME_ACC = "ACC";
	final String MODEL_NAME_OVM_VDIFF = "OVM_VDIFF";
	final String MODEL_NAME_GIPPS = "GIPPS";
	final String MODEL_NAME_NEWELL = "NEWELL"; 
	final String MODEL_NAME_NSM = "NSM";
	final String MODEL_NAME_KCA = "KCA";
	 
	
}
