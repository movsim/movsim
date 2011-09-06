/**
 * Copyright (C) 2010, 2011 by Arne Kesting, Martin Treiber,
 *                             Ralph Germ, Martin Budden
 *                             <info@movsim.org>
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
import org.movsim.input.XmlElementNames;
import org.movsim.input.impl.XmlUtils;
import org.movsim.input.model.simulation.InflowDataPoint;
import org.movsim.input.model.simulation.RampData;
import org.movsim.input.model.simulation.UpstreamBoundaryData;

// TODO: Auto-generated Javadoc
// TODO: concept of real onramp with lane changes not yet implemented
/**
 * The Class RampDataImpl.
 */
public class RampDataImpl implements RampData {

    /** The center position. */
    private final double rampStartPosition;

    /** The road length. */
    private final double roadLength;
    
    /** The ramp length. */
    private final double rampMergingLength;

    /** The with logging. */
    private final boolean withLogging;
    
    private final UpstreamBoundaryData upstreamData;

    /**
     * Instantiates a new ramp data impl.
     * 
     * @param elem
     *            the elem
     */
    @SuppressWarnings("unchecked")
    public RampDataImpl(Element elem) {
        this.rampStartPosition = Double.parseDouble(elem.getAttributeValue("x"));
        this.roadLength = Double.parseDouble(elem.getAttributeValue("length"));
        this.rampMergingLength = Double.parseDouble(elem.getAttributeValue("merge_length"));
        this.withLogging = Boolean.parseBoolean(elem.getAttributeValue("logging"));

        final Element upInflowElem = elem.getChild(XmlElementNames.RoadTrafficSource);
        upstreamData = new UpstreamBoundaryDataImpl(upInflowElem);
        

    }

    

 

    @Override
    public double getRampStartPosition() {
        return rampStartPosition;
    }

    @Override
    public double getRampMergingLength() {
        return rampMergingLength;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.simulation.RampData#getRoadLength()
     */
    @Override
    public double getRoadLength() {
        return roadLength;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.simulation.RampData#withLogging()
     */
    @Override
    public boolean withLogging() {
        return withLogging;
    }

    @Override
    public UpstreamBoundaryData getUpstreamBoundaryData() {
        return upstreamData;
    }

}
