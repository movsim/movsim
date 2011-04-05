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
import org.movsim.input.model.simulation.UpstreamBoundaryData;


public class UpstreamBoundaryDataImpl implements UpstreamBoundaryData {

    private List<InflowDataPoint> inflowTimeSeries;
    private boolean withLogging;

    @SuppressWarnings("unchecked")
    public UpstreamBoundaryDataImpl(Element elem){
        this.withLogging = Boolean.parseBoolean(elem.getAttributeValue("with_logging"));
        
        final List<Element> upInflowElems = elem.getChildren("INFLOW");
        parseAndSortInflowElements(upInflowElems);
    }
    
    private void parseAndSortInflowElements(List<Element> upInflowElems){
        inflowTimeSeries = new ArrayList<InflowDataPoint>();
        for (Element upInflowElem : upInflowElems) {
            final Map<String, String> inflowMap = XmlUtils.putAttributesInHash(upInflowElem);
            inflowTimeSeries.add(new InflowDataPointImpl(inflowMap));
        }
        Collections.sort(inflowTimeSeries, new Comparator<InflowDataPoint>() {
            public int compare(InflowDataPoint o1, InflowDataPoint o2) {
                Double pos1 = new Double((o1).getTime());
                Double pos2 = new Double((o2).getTime());
                return pos1.compareTo(pos2); // sort with increasing t 
            }
        });
    }
    
    public List<InflowDataPoint> getInflowTimeSeries(){
        return inflowTimeSeries;
    }

    public boolean withLogging() {
        return withLogging;
    }
}
