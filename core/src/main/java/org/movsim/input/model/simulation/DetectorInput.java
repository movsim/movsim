/*
 * Copyright (C) 2010, 2011, 2012 by Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden
 *                                   <movsim.org@gmail.com>
 * -----------------------------------------------------------------------------------------
 * 
 * This file is part of
 * 
 * MovSim - the multi-model open-source vehicular-traffic simulator.
 * 
 * MovSim is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MovSim is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MovSim. If not, see <http://www.gnu.org/licenses/>
 * or <http://www.movsim.org>.
 * 
 * -----------------------------------------------------------------------------------------
 */
package org.movsim.input.model.simulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.jdom.Element;

public class DetectorInput {

    /** The positions of detectors. */
    private List<Double> positions;

    private double dtSample;

    private boolean withLogging;
    
    private boolean loggingLanes;

    private final boolean isInitialized;

    /**
     * Instantiates a new detector input.
     * 
     * @param elem
     *            the elem
     */
    public DetectorInput(Element elem) {

        if (elem == null) {
            isInitialized = false;
            return;
        }

        parseElement(elem);
        isInitialized = true;

    }

    /**
     * Parses the element.
     * 
     * @param elem
     *            the elem
     */
    @SuppressWarnings("unchecked")
    private void parseElement(Element elem) {

        this.dtSample = Double.parseDouble(elem.getAttributeValue("dt"));
        this.withLogging = Boolean.parseBoolean(elem.getAttributeValue("logging"));
        this.loggingLanes = Boolean.parseBoolean(elem.getAttributeValue("logging_lanes"));        

        // Detector
        positions = new ArrayList<Double>();

        final List<Element> crossElems = elem.getChildren("CROSS_SECTION");
        if (crossElems != null) {
            for (final Element crossElem : crossElems) {
                positions.add(Double.parseDouble(crossElem.getAttributeValue("x")));
            }
        }

        Collections.sort(positions, new Comparator<Double>() {
            @Override
            public int compare(Double o1, Double o2) {
                final Double pos1 = new Double((o1).doubleValue());
                final Double pos2 = new Double((o2).doubleValue());
                return pos1.compareTo(pos2); // sort with increasing x
            }
        });

        // -----------------------------------------------------------
    }

    public List<Double> getPositions() {
        return positions;
    }

    public double getSampleInterval() {
        return dtSample;
    }

    public boolean isWithDetectors() {
        return isInitialized;
    }

    public boolean isWithLogging() {
        return withLogging;
    }

    public boolean isLoggingLanes() {
        return loggingLanes;
    }

}
