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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.jdom.Element;
import org.movsim.input.impl.XmlUtils;
import org.movsim.input.model.simulation.InflowDataPoint;
import org.movsim.input.model.simulation.RampData;


// TODO: concept of real onramp with lane changes not yet implemented
public class RampDataImpl implements RampData {

    private List<InflowDataPoint> inflowTimeSeries;
	private double centerPosition;
	private double rampLength;
	private double roadLength;
	
	
	private boolean withLogging;

	@SuppressWarnings("unchecked")
    public RampDataImpl(Element elem){
	    this.centerPosition = Double.parseDouble(elem.getAttributeValue("x_center"));
        this.rampLength = Double.parseDouble(elem.getAttributeValue("length"));
        this.roadLength = Double.parseDouble(elem.getAttributeValue("x_max"));
        this.withLogging = Boolean.parseBoolean(elem.getAttributeValue("with_logging"));
        
        final List<Element> inflowElems = elem.getChildren("INFLOW");
	    parseAndSortInflowElements(inflowElems);
	    
	}

    private void parseAndSortInflowElements(List<Element> inflowElems) {
        inflowTimeSeries = new ArrayList<InflowDataPoint>();
        for (Element inflowElem : inflowElems) {
            final Map<String, String> map = XmlUtils.putAttributesInHash(inflowElem);
            inflowTimeSeries.add(new InflowDataPointImpl(map));
        }
        Collections.sort(inflowTimeSeries, new Comparator<InflowDataPoint>() {
            public int compare(InflowDataPoint o1, InflowDataPoint o2) {
                Double pos1 = new Double((o1).getTime());
                Double pos2 = new Double((o2).getTime());
                return pos1.compareTo(pos2); // sort with increasing t 
            }
        });
    }

    /* (non-Javadoc)
     * @see org.movsim.input.model.simulation.impl.RampData#getInflowTimeSeries()
     */
    public List<InflowDataPoint> getInflowTimeSeries() {
        return inflowTimeSeries;
    }

    /* (non-Javadoc)
     * @see org.movsim.input.model.simulation.impl.RampData#getCenterPosition()
     */
    public double getCenterPosition() {
        return centerPosition;
    }

    /* (non-Javadoc)
     * @see org.movsim.input.model.simulation.impl.RampData#getRampLength()
     */
    public double getRampLength() {
        return rampLength;
    }
    
    public double getRoadLength() {
        return roadLength;
    }

    public boolean withLogging(){
        return withLogging;
    }
  
}
