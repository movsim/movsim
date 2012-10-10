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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Element;
import org.movsim.input.XmlElementNames;
import org.movsim.input.XmlUtils;
import org.movsim.input.model.output.RoutesInput;
import org.movsim.input.model.simulation.VehicleTypeInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The SimulationInput.
 */
public class SimulationInput {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(SimulationInput.class);

    private final double timestep;

    /** The duration of the simulation. */
    private final double duration;

    private boolean withFixedSeed;

    private boolean withCrashExit;

    private final int randomSeed;

    private final List<VehicleTypeInput> vehicleTypeInputs;

    private boolean isWithWriteFundamentalDiagrams;

    Map<String, RoadInput> roadInputMap;

    private final OutputInput outputInput;

    private RoutesInput routesInput;

    /**
     * Instantiates a new simulation input.
     * 
     * @param elem
     *            the elem
     */
    public SimulationInput(Element elem) {
        timestep = Double.parseDouble(elem.getAttributeValue("dt"));
        duration = Double.parseDouble(elem.getAttributeValue("duration"));
        randomSeed = Integer.parseInt(elem.getAttributeValue("seed"));
        if (elem.getAttributeValue("fixed_seed").equalsIgnoreCase("true")) {
            withFixedSeed = true;
        } else {
            withFixedSeed = false;
        }
        if (elem.getAttributeValue("crash_exit").equalsIgnoreCase("true")) {
            withCrashExit = true;
        } else {
            withCrashExit = false;
        }

        // default heterogeneity element with vehicle types
        vehicleTypeInputs = new ArrayList<VehicleTypeInput>();
        final Element heterogenElem = elem.getChild(XmlElementNames.TrafficComposition);

        // optional for specific road
        if (heterogenElem != null) {
            @SuppressWarnings("unchecked")
            final List<Element> vehTypeElems = elem.getChild(XmlElementNames.TrafficComposition).getChildren(
                    XmlElementNames.RoadVehicleType);
            for (final Element vehTypeElem : vehTypeElems) {
                final Map<String, String> map = XmlUtils.putAttributesInHash(vehTypeElem);
                vehicleTypeInputs.add(new VehicleTypeInput(map));
            }
        }

        // -----------------------------------------------------------

        // quick hack: for road segment a mapping to ids is needed
        @SuppressWarnings("unchecked")
        final List<Element> roadElems = elem.getChildren(XmlElementNames.Road);
        final List<RoadInput> roadInputList = new ArrayList<RoadInput>();
        for (final Element roadElem : roadElems) {
            roadInputList.add(new RoadInput(roadElem));
        }

        roadInputMap = new HashMap<String, RoadInput>();
        for (final RoadInput roadInputData : roadInputList) {
            roadInputMap.put(roadInputData.getId(), roadInputData);
        }

        // Routes

        if (elem.getChild(XmlElementNames.OutputRoutes) != null) {
            routesInput = new RoutesInput(roadInputMap, elem.getChild(XmlElementNames.OutputRoutes));
        }

        // -------------------------------------------------------
        // Output
        outputInput = new OutputInput(roadInputMap, elem.getChild(XmlElementNames.Output));

    }

    /**
     * @param xodrFilename
     * @return
     */
    private static boolean validateOpenDriveFileName(String xodrFilename) {
        final int i = xodrFilename.lastIndexOf(".xodr");
        if (i < 0) {
            System.out
                    .println("Please provide OpenDRIVE file with ending \".xodr\" as argument with option -n, exit. ");
            return false;
        }
        return true;
    }

    public double getTimestep() {
        return timestep;
    }

    /**
     * returns the simulation duration (in seconds). May be <0 if simulation runs infinitely
     */
    public double getSimulationDuration() {
        return duration;
    }

    public boolean isWithFixedSeed() {
        return withFixedSeed;
    }

    public int getRandomSeed() {
        return randomSeed;
    }

    public Map<String, RoadInput> getRoadInput() {
        return roadInputMap;
    }

    public RoadInput getSingleRoadInput() {
        // Quick hack: assume only one single main road !!!
        return roadInputMap.get(0);
    }

    public OutputInput getOutputInput() {
        return outputInput;
    }

    public boolean isWithCrashExit() {
        return withCrashExit;
    }

    public List<VehicleTypeInput> getVehicleTypeInput() {
        return vehicleTypeInputs;
    }

    public boolean isWithWriteFundamentalDiagrams() {
        return isWithWriteFundamentalDiagrams;
    }

    public RoutesInput getRoutesInput() {
        return routesInput;
    }
}
