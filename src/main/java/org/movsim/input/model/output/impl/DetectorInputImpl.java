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
package org.movsim.input.model.output.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.jdom.Element;
import org.movsim.input.model.output.DetectorInput;



public class DetectorInputImpl implements DetectorInput {

    private List<Double> positions; 
    private double dtSample;
    
    private final boolean isInitialized;
    
    
    public DetectorInputImpl(Element elem){

        if( elem == null){
            isInitialized = false;
            return;
        }
        
        parseElement(elem);
        isInitialized = true;
        
       
    }

    @SuppressWarnings("unchecked")
    private void parseElement(Element elem) {
        
        this.dtSample = Double.parseDouble(elem.getAttributeValue("dt"));
        
        // Detector
        positions = new ArrayList<Double>();
        
        List<Element> crossElems = elem.getChildren("CROSS_SECTION");
        if( crossElems != null){
            for (Element crossElem : crossElems) {
                positions.add(Double.parseDouble(crossElem.getAttributeValue("x")));
            }
        }
        
        
        Collections.sort(positions, new Comparator<Double>() {
            public int compare(Double o1, Double o2) {
                Double pos1 = new Double((o1).doubleValue());
                Double pos2 = new Double((o2).doubleValue());
                return pos1.compareTo(pos2); // sort with increasing x 
            }
        });
        
        // -----------------------------------------------------------
    }

    /* (non-Javadoc)
     * @see org.movsim.input.model.output.impl.DetectorInput#getPositions()
     */
    public List<Double> getPositions() {
        return positions;
    }

    /* (non-Javadoc)
     * @see org.movsim.input.model.output.impl.DetectorInput#getSampleInterval()
     */
    public double getSampleInterval() {
        return dtSample;
    }
    
    /* (non-Javadoc)
     * @see org.movsim.input.model.output.impl.DetectorInput#isWithDetectors()
     */
    public boolean isWithDetectors(){
        return isInitialized;
    }
    
    
}
