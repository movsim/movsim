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
import java.util.Map;

import org.jdom.Element;
import org.movsim.input.XmlElementNames;
import org.movsim.input.XmlUtils;

public class TrafficLightsInput {

    private List<TrafficLightData> trafficLightData;

    private int nDtSample = 0;

    private boolean withLogging = false;

    /**
     * Instantiates a new traffic lights input.
     * 
     * @param elem
     *            the elem
     */
    public TrafficLightsInput(Element elem) {

        trafficLightData = new ArrayList<TrafficLightData>();

        if (elem != null) {
            this.nDtSample = Integer.parseInt(elem.getAttributeValue("n_dt"));
            this.withLogging = Boolean.parseBoolean(elem.getAttributeValue("logging"));

            @SuppressWarnings("unchecked")
            final List<Element> trafficLightElems = elem.getChildren(XmlElementNames.RoadTrafficLight);
            for (final Element trafficLightElem : trafficLightElems) {
                final Map<String, String> map = XmlUtils.putAttributesInHash(trafficLightElem);
                trafficLightData.add(new TrafficLightData(map));
            }

            Collections.sort(trafficLightData, new Comparator<TrafficLightData>() {
                @Override
                public int compare(TrafficLightData o1, TrafficLightData o2) {
                    final Double pos1 = new Double((o1).getX());
                    final Double pos2 = new Double((o2).getX());
                    return pos1.compareTo(pos2); // sort with increasing x
                }
            });
        }
    }

    public List<TrafficLightData> getTrafficLightData() {
        return trafficLightData;
    }

    public int getnDtSample() {
        return nDtSample;
    }

    public boolean isWithLogging() {
        return withLogging;
    }

}
