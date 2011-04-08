/** 
 * Copyright (C) 2010, 2011 by Arne Kesting  <mail@akesting.de>, 
 * 				Martin Treiber <treibi@mtreiber.de>,
 * 				Ralph Germn <germ@ralphgerm.de>
 *
 * ----------------------------------------------------------------------
 * 
 *  This file is part of MovSim.
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
 *  along with MovSim.  If not, see <http://www.gnu.org/licenses/>.
 *  
 * ----------------------------------------------------------------------
 */
package org.movsim.input.model;

import java.util.ArrayList;



public interface SimulationInput {

    double getTimestep();

    double getMaxSimulationTime();

    boolean isWithFixedSeed();

    int getRandomSeed();
    
    ArrayList<RoadInput> getRoadInput();
    
    // quick hack: only one single main road
    RoadInput getSingleRoadInput();
    
}