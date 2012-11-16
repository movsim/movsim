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
import org.movsim.input.model.output.ConsumptionOnRouteInput;
import org.movsim.input.model.output.FloatingCarInput;
import org.movsim.input.model.output.SpatioTemporalInput;
import org.movsim.input.model.output.TrajectoriesInput;
import org.movsim.input.model.output.TravelTimeOnRouteInput;

public class OutputInput {

    private final List<FloatingCarInput> floatingCarInputs = new ArrayList<FloatingCarInput>();

    private final List<SpatioTemporalInput> spatioTemporalInput = new ArrayList<SpatioTemporalInput>();

    private final List<TrajectoriesInput> trajectoriesInput = new ArrayList<TrajectoriesInput>();

    private final List<TravelTimeOnRouteInput> travelTimeOnRouteInput = new ArrayList<TravelTimeOnRouteInput>();
    
    private final List<ConsumptionOnRouteInput> consumptionOnRouteInput = new ArrayList<ConsumptionOnRouteInput>();

    /**
     * Instantiates a new output input.
     * 
     * @param roadInputMap
     * 
     * @param elem
     *            the elem
     */
    public OutputInput(Map<String, RoadInput> roadInputMap, Element elem) {
        if (elem == null) {
            return;
        }

        if (elem.getChild(XmlElementNames.OutputFloatingCarData) != null) {
            @SuppressWarnings("unchecked")
            List<Element> elements = elem.getChildren(XmlElementNames.OutputFloatingCarData);
            for (Element element : elements) {
                floatingCarInputs.add(new FloatingCarInput(element));
            }
        }

        if (elem.getChild(XmlElementNames.OutputSpatioTemporal) != null) {
            @SuppressWarnings("unchecked")
            List<Element> elements = elem.getChildren(XmlElementNames.OutputSpatioTemporal);
            for (Element element : elements) {
                spatioTemporalInput.add(new SpatioTemporalInput(element));
            }
        }

        if (elem.getChild(XmlElementNames.OutputTrajectories) != null) {
            @SuppressWarnings("unchecked")
            List<Element> elements = elem.getChildren(XmlElementNames.OutputTrajectories);
            for (Element element : elements) {
                trajectoriesInput.add(new TrajectoriesInput(element));
            }
        }

        if (elem.getChild(XmlElementNames.OutputTravelTimes) != null) {
            @SuppressWarnings("unchecked")
            List<Element> elements = elem.getChildren(XmlElementNames.OutputTravelTimes);
            for (Element element : elements) {
                travelTimeOnRouteInput.add(new TravelTimeOnRouteInput(element));
            }
        }
        
        if (elem.getChild(XmlElementNames.OutputFuel) != null) {
            @SuppressWarnings("unchecked")
            List<Element> elements = elem.getChildren(XmlElementNames.OutputFuel);
            for (Element element : elements) {
                consumptionOnRouteInput.add(new ConsumptionOnRouteInput(element));
            }
        }
    }

    public List<FloatingCarInput> getFloatingCarInputs() {
        return floatingCarInputs;
    }

    public List<SpatioTemporalInput> getSpatioTemporalInput() {
        return spatioTemporalInput;
    }

    public List<TrajectoriesInput> getTrajectoriesInput() {
        return trajectoriesInput;
    }
    
    public List<ConsumptionOnRouteInput> getFuelInput() {
        return consumptionOnRouteInput;
    }

    public List<TravelTimeOnRouteInput> getTravelTimesInput() {
        return travelTimeOnRouteInput;
    }

}
