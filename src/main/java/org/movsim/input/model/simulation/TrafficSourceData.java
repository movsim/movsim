/**
 * Copyright (C) 2010, 2011 by Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden <info@movsim.org>
 * ----------------------------------------------------------------------
 * 
 * This file is part of
 * 
 * MovSim - the multi-model open-source vehicular-traffic simulator
 * 
 * MovSim is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * MovSim is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with MovSim. If not, see <http://www.gnu.org/licenses/> or
 * <http://www.movsim.org>.
 * 
 * ----------------------------------------------------------------------
 */
package org.movsim.input.model.simulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.jdom.Element;
import org.movsim.input.XmlElementNames;
import org.movsim.input.XmlUtils;

public class TrafficSourceData {

    /** The inflow time series. */
    private final List<InflowDataPoint> inflowTimeSeries = new ArrayList<InflowDataPoint>();

    /** The with logging. */
    private final boolean withLogging;

    /**
     * Instantiates a new upstream boundary data impl.
     * 
     * @param elem
     *            the elem
     */
    @SuppressWarnings("unchecked")
    public TrafficSourceData(Element elem) {
        if (elem == null) {
            withLogging = false;
        }
        else{
            withLogging = Boolean.parseBoolean(elem.getAttributeValue("logging"));
            final List<Element> upInflowElems = elem.getChildren(XmlElementNames.RoadInflow);
            parseAndSortInflowElements(upInflowElems);
        }
    }

    /**
     * Parses the and sort inflow elements.
     * 
     * @param upInflowElems
     *            the up inflow elems
     */
    private void parseAndSortInflowElements(List<Element> upInflowElems) {
        for (final Element upInflowElem : upInflowElems) {
            final Map<String, String> inflowMap = XmlUtils.putAttributesInHash(upInflowElem);
            inflowTimeSeries.add(new InflowDataPoint(inflowMap));
        }
        Collections.sort(inflowTimeSeries, new Comparator<InflowDataPoint>() {
            @Override
            public int compare(InflowDataPoint o1, InflowDataPoint o2) {
                final Double pos1 = new Double((o1).getTime());
                final Double pos2 = new Double((o2).getTime());
                return pos1.compareTo(pos2); // sort with increasing t
            }
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.simulation.UpstreamBoundaryData#getInflowTimeSeries ()
     */
    public List<InflowDataPoint> getInflowTimeSeries() {
        return inflowTimeSeries;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.simulation.UpstreamBoundaryData#withLogging()
     */
    public boolean withLogging() {
        return withLogging;
    }
}
