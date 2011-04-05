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
import java.util.List;

import org.jdom.Element;
import org.movsim.input.model.output.FloatingCarInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FloatingCarInputImpl implements FloatingCarInput {
    
	final static Logger logger = LoggerFactory.getLogger(FloatingCarInputImpl.class);
	
	private int nDt;
	
    private int dn; 
    private double percOut; 
    
    private List<Integer> floatingCars;
    
    private final boolean isWithFC;
    
    public FloatingCarInputImpl(Element elem){
        
    	if(elem == null){
    	    isWithFC = false; // not initialized
    	    return;
    	}
    	
        this.nDt =Integer.parseInt(elem.getAttributeValue("n_dt"));

        this.dn = Integer.parseInt(elem.getAttributeValue("dn"));
        if(dn!=0){
            logger.error("dn = {} not yet implemented. exit.", dn);
            System.exit(-1);
        }
        this.percOut = Double.parseDouble(elem.getAttributeValue("perc_out"));
        if(percOut>0){
        	logger.error("perc_out = {} (>0) not yet implemented. exit.", percOut);
        	System.exit(-1);
        }
    	

        floatingCars = new ArrayList<Integer>();
        List<Element> fcElems = elem.getChildren("FC");
        if(fcElems != null){
        	for(Element fcElem : fcElems){
        		final int iFC = Integer.parseInt(fcElem.getAttributeValue("number"));
        		floatingCars.add(new Integer(iFC));
        	}
        }
        
        isWithFC = (dn!=0 || !floatingCars.isEmpty() || percOut >0);
    }

    /* (non-Javadoc)
     * @see org.movsim.input.model.output.impl.FloatingCarInput#getDn()
     */
    public int getDn() {
        return dn;
    }

    /* (non-Javadoc)
     * @see org.movsim.input.model.output.impl.FloatingCarInput#getDt()
     */
    public int getNDt() {
        return nDt;
    }
    
    public double getPercOut() {
        return percOut;
    }
    
    /* (non-Javadoc)
     * @see org.movsim.input.model.output.impl.FloatingCarInput#isWithFCD()
     */
    public boolean isWithFCD(){
        return isWithFC;
    }

	public List<Integer> getFloatingCars() {
		return floatingCars;
	}

}
