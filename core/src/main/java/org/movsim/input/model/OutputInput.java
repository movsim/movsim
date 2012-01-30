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
package org.movsim.input.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jdom.Element;
import org.movsim.input.XmlElementNames;
import org.movsim.input.model.output.FloatingCarInput;
import org.movsim.input.model.output.RoutesInput;
import org.movsim.input.model.output.SpatioTemporalInput;
import org.movsim.input.model.output.TrajectoriesInput;
import org.movsim.input.model.output.TravelTimesInput;

public class OutputInput {

    private FloatingCarInput floatingCarInput;

    private List<SpatioTemporalInput> spatioTemporalInput;

    private TrajectoriesInput trajectoriesInput;

    private TravelTimesInput travelTimesInput;

    private RoutesInput routesInput;

    /**
     * Instantiates a new output input.
     * 
     * @param roadInputMap
     * 
     * @param elem
     *            the elem
     */
    public OutputInput(Map<String, RoadInput> roadInputMap, Element elem) {

        if (elem.getChild(XmlElementNames.OutputRoutes) != null) {
            routesInput = new RoutesInput(roadInputMap, elem.getChild(XmlElementNames.OutputRoutes));
        }

        floatingCarInput = new FloatingCarInput(elem.getChild(XmlElementNames.OutputFloatingCarData));

        if (elem.getChild(XmlElementNames.OutputSpatioTemporal) != null) {
            spatioTemporalInput = new ArrayList<SpatioTemporalInput>();
            @SuppressWarnings("unchecked")
            List<Element> elements = elem.getChildren(XmlElementNames.OutputSpatioTemporal);
            for (Element element : elements) {
                spatioTemporalInput.add(new SpatioTemporalInput(element));
            }
        }

        trajectoriesInput = new TrajectoriesInput(elem.getChild(XmlElementNames.OutputTrajectories));

        // TODO treat all elements similarly
        if (elem.getChild(XmlElementNames.OutputTravelTimes) != null) {
            travelTimesInput = new TravelTimesInput(elem.getChild(XmlElementNames.OutputTravelTimes));
        }
    }

    public RoutesInput getRoutesInput() {
        return routesInput;
    }

    public FloatingCarInput getFloatingCarInput() {
        return floatingCarInput;
    }

    public List<SpatioTemporalInput> getSpatioTemporalInput() {
        return spatioTemporalInput;
    }

    public TrajectoriesInput getTrajectoriesInput() {
        return trajectoriesInput;
    }

    public TravelTimesInput getTravelTimesInput() {
        return travelTimesInput;
    }

}
