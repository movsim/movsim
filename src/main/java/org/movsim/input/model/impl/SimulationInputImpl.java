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
package org.movsim.input.model.impl;

import java.util.ArrayList;
import java.util.List;

import org.jdom.Element;
import org.movsim.input.model.RoadInput;
import org.movsim.input.model.SimulationInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SimulationInputImpl implements SimulationInput {
    
    final static Logger logger = LoggerFactory.getLogger(SimulationInputImpl.class);
    
    private double timestep;
    
    private double maxSimTime;
    
    
    private boolean withFixedSeed;
    private int randomSeed;
    
    ArrayList<RoadInput> roadInput;
    
    public SimulationInputImpl(Element elem){
        timestep = Double.parseDouble(elem.getAttributeValue("dt"));
        maxSimTime  = Double.parseDouble(elem.getAttributeValue("t_max"));
        randomSeed  = Integer.parseInt(elem.getAttributeValue("seed"));
        if (elem.getAttributeValue("with_fixed_seed").equalsIgnoreCase("true")) {
            withFixedSeed = true;
        }
        else{
            withFixedSeed = false;
        }
        
        
        
        final List<Element> roadElems = elem.getChildren("ROAD");
        roadInput = new ArrayList<RoadInput>();
        for (Element roadElem : roadElems ) {
            roadInput.add(new RoadInputImpl(roadElem));
        }
        
       
    }
    /* (non-Javadoc)
     * @see org.movsim.input.model.impl.SimulationInput#getTimestep()
     */
    public double getTimestep() {
        return timestep;
    }

    /* (non-Javadoc)
     * @see org.movsim.input.model.impl.SimulationInput#getMaxSimulationTime()
     */
    public double getMaxSimulationTime() {
        return maxSimTime;
    }


    /* (non-Javadoc)
     * @see org.movsim.input.model.impl.SimulationInput#isWithFixedSeed()
     */
    public boolean isWithFixedSeed() {
        return withFixedSeed;
    }


    /* (non-Javadoc)
     * @see org.movsim.input.model.impl.SimulationInput#getRandomSeed()
     */
    public int getRandomSeed() {
        return randomSeed;
    }

    
    public ArrayList<RoadInput> getRoadInput() {
        return roadInput;
    }
    

    // Quick hack: assume only one single main road !!!
    
    public RoadInput getSingleRoadInput() {
        return roadInput.get(0);
    }
    
}
