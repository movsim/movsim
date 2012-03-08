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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.jdom.Element;
import org.movsim.input.XmlElementNames;
import org.movsim.input.XmlUtils;
import org.movsim.input.model.simulation.DetectorInput;
import org.movsim.input.model.simulation.FlowConservingBottleneckDataPoint;
import org.movsim.input.model.simulation.ICMacroData;
import org.movsim.input.model.simulation.ICMicroData;
import org.movsim.input.model.simulation.SimpleRampData;
import org.movsim.input.model.simulation.SlopeDataPoint;
import org.movsim.input.model.simulation.SpeedLimitDataPoint;
import org.movsim.input.model.simulation.TrafficCompositionInputData;
import org.movsim.input.model.simulation.TrafficLightsInput;
import org.movsim.input.model.simulation.TrafficSinkData;
import org.movsim.input.model.simulation.TrafficSourceData;

public class RoadInput {

    private String id;

    private boolean isWithWriteFundamentalDiagrams;

    private List<TrafficCompositionInputData> trafficCompositionInputData;

    private List<ICMacroData> icMacroData;

    private List<ICMicroData> icMicroData;

    private TrafficSourceData trafficSourceData;

    private TrafficSinkData trafficSinkData;

    private List<FlowConservingBottleneckDataPoint> flowConsBottleneckInputData;

    private List<SpeedLimitDataPoint> speedLimitInputData;

    private List<SimpleRampData> simpleRamps;

    private TrafficLightsInput trafficLightsInput;

    private DetectorInput detectorInput;

    private List<SlopeDataPoint> slopesInputData;

    /**
     * Instantiates a new road input.
     * 
     * @param elem
     *            the elem
     */
    public RoadInput(Element elem) {
        parseRoadElement(elem);
    }

    /**
     * Parses the road element.
     * 
     * @param elem
     *            the elem
     */
    @SuppressWarnings("unchecked")
    private void parseRoadElement(Element elem) {

        id = elem.getAttributeValue("id");

        // heterogeneity element with vehicle types
        trafficCompositionInputData = new ArrayList<TrafficCompositionInputData>();

        final Element heterogenElem = elem.getChild(XmlElementNames.TrafficComposition);
        // optional for specific road
        if (heterogenElem != null) {
            final List<Element> vehTypeElems = elem.getChild(XmlElementNames.TrafficComposition).getChildren(
                    XmlElementNames.RoadVehicleType);
            for (final Element vehTypeElem : vehTypeElems) {
                final Map<String, String> map = XmlUtils.putAttributesInHash(vehTypeElem);
                trafficCompositionInputData.add(new TrafficCompositionInputData(map));
            }
        }

        // Initial Conditions Micro
        final List<Element> icMicroElems = elem.getChild(XmlElementNames.RoadInitialConditions) == null ? new ArrayList<Element>()
                : elem.getChild(XmlElementNames.RoadInitialConditions).getChildren(
                        XmlElementNames.RoadInitialConditionsIcMicro);
        icMicroData = new ArrayList<ICMicroData>();
        for (final Element icMicroElem : icMicroElems) {
            final Map<String, String> map = XmlUtils.putAttributesInHash(icMicroElem);
            icMicroData.add(new ICMicroData(map));
        }
        Collections.sort(icMicroData, new Comparator<ICMicroData>() {
            @Override
            public int compare(ICMicroData o1, ICMicroData o2) {
                final Double pos1 = new Double((o1).getX());
                final Double pos2 = new Double((o2).getX());
                return pos2.compareTo(pos1); // sort with DECREASING x because of FC veh counting
            }
        });

        // Initial Conditions Macro
        final List<Element> icMacroElems = elem.getChild(XmlElementNames.RoadInitialConditions) == null ? new ArrayList<Element>()
                : elem.getChild(XmlElementNames.RoadInitialConditions).getChildren(
                        XmlElementNames.RoadInitialConditionsIcMacro);
        icMacroData = new ArrayList<ICMacroData>();
        for (final Element icMacroElem : icMacroElems) {
            final Map<String, String> map = XmlUtils.putAttributesInHash(icMacroElem);
            icMacroData.add(new ICMacroData(map));
        }
        Collections.sort(icMacroData, new Comparator<ICMacroData>() {
            @Override
            public int compare(ICMacroData o1, ICMacroData o2) {
                final Double pos1 = new Double((o1).getX());
                final Double pos2 = new Double((o2).getX());
                return pos1.compareTo(pos2); // sort with increasing x
            }
        });

        // TRAFFIC_SOURCE
        final Element roadSourceElem = elem.getChild(XmlElementNames.RoadTrafficSource);
        trafficSourceData = new TrafficSourceData(roadSourceElem);

        // TRAFFIC_SINK
        final Element roadSinkElem = elem.getChild(XmlElementNames.RoadTrafficSink);
        trafficSinkData = new TrafficSinkData(roadSinkElem);

        // FlowConservingBottlenecks
        flowConsBottleneckInputData = new ArrayList<FlowConservingBottleneckDataPoint>();
        final Element flowConsBottlenecksElement = elem.getChild(XmlElementNames.RoadFlowConservingInhomogeneities);
        if (flowConsBottlenecksElement != null) {
            final List<Element> flowConsElems = flowConsBottlenecksElement
                    .getChildren(XmlElementNames.RoadInhomogeneity);
            for (final Element flowConsElem : flowConsElems) {
                final Map<String, String> map = XmlUtils.putAttributesInHash(flowConsElem);
                flowConsBottleneckInputData.add(new FlowConservingBottleneckDataPoint(map));
            }
            Collections.sort(flowConsBottleneckInputData, new Comparator<FlowConservingBottleneckDataPoint>() {
                @Override
                public int compare(FlowConservingBottleneckDataPoint o1, FlowConservingBottleneckDataPoint o2) {
                    final Double pos1 = new Double((o1).getPosition());
                    final Double pos2 = new Double((o2).getPosition());
                    return pos1.compareTo(pos2); // sort with increasing x
                }
            });
        }

        // speed limits
        speedLimitInputData = new ArrayList<SpeedLimitDataPoint>();
        final Element speedLimitsElement = elem.getChild(XmlElementNames.RoadSpeedLimits);
        if (speedLimitsElement != null) {
            final List<Element> speedLimitElems = speedLimitsElement.getChildren(XmlElementNames.RoadSpeedLimit);
            for (final Element speedLimitElem : speedLimitElems) {
                final Map<String, String> map = XmlUtils.putAttributesInHash(speedLimitElem);
                speedLimitInputData.add(new SpeedLimitDataPoint(map));
            }
            Collections.sort(speedLimitInputData, new Comparator<SpeedLimitDataPoint>() {
                @Override
                public int compare(SpeedLimitDataPoint o1, SpeedLimitDataPoint o2) {
                    final Double pos1 = new Double((o1).getPosition());
                    final Double pos2 = new Double((o2).getPosition());
                    return pos1.compareTo(pos2); // sort with increasing x
                }
            });
        }

        // slopes
        slopesInputData = new ArrayList<SlopeDataPoint>();
        final Element slopesElement = elem.getChild(XmlElementNames.RoadSlopes);
        if (slopesElement != null) {
            final List<Element> slopeElems = slopesElement.getChildren(XmlElementNames.RoadSlope);
            for (final Element slopeElem : slopeElems) {
                final Map<String, String> map = XmlUtils.putAttributesInHash(slopeElem);
                slopesInputData.add(new SlopeDataPoint(map));
            }
            Collections.sort(slopesInputData, new Comparator<SlopeDataPoint>() {
                @Override
                public int compare(SlopeDataPoint o1, SlopeDataPoint o2) {
                    final Double pos1 = new Double((o1).getPosition());
                    final Double pos2 = new Double((o2).getPosition());
                    return pos1.compareTo(pos2); // sort with increasing x
                }
            });
        }

        // non-physical ramps implementing a drop-down mechanism without
        // lane-changing decisions
        simpleRamps = new ArrayList<SimpleRampData>();

        final Element rampsElement = elem.getChild(XmlElementNames.RoadRamps);
        if (rampsElement != null) {
            final List<Element> simpleRampElems = rampsElement.getChildren(XmlElementNames.RoadSimpleRamp);
            for (final Element simpleRampElem : simpleRampElems) {
                simpleRamps.add(new SimpleRampData(simpleRampElem));
            }
            Collections.sort(simpleRamps, new Comparator<SimpleRampData>() {
                @Override
                public int compare(SimpleRampData o1, SimpleRampData o2) {
                    final Double pos1 = new Double((o1).getRampStartPosition());
                    final Double pos2 = new Double((o2).getRampStartPosition());
                    return pos1.compareTo(pos2); // sort with increasing x
                }
            });
        }

        // Trafficlights
        final Element trafficLightsElement = elem.getChild(XmlElementNames.RoadTrafficLights);
        trafficLightsInput = new TrafficLightsInput(trafficLightsElement);

        // Detectors
        detectorInput = new DetectorInput(elem.getChild(XmlElementNames.OutputDetectors));
    }

    public List<TrafficCompositionInputData> getTrafficCompositionInputData() {
        return trafficCompositionInputData;
    }

    public List<ICMacroData> getIcMacroData() {
        return icMacroData;
    }

    public List<ICMicroData> getIcMicroData() {
        return icMicroData;
    }

    public TrafficSourceData getTrafficSourceData() {
        return trafficSourceData;
    }

    public TrafficSinkData getTrafficSinkData() {
        return trafficSinkData;
    }

    public List<FlowConservingBottleneckDataPoint> getFlowConsBottleneckInputData() {
        return flowConsBottleneckInputData;
    }

    public List<SpeedLimitDataPoint> getSpeedLimitInputData() {
        return speedLimitInputData;
    }

    public List<SlopeDataPoint> getSlopesInputData() {
        return slopesInputData;
    }

    public String getId() {
        return id;
    }

    public boolean isWithWriteFundamentalDiagrams() {
        return isWithWriteFundamentalDiagrams;
    }

    public List<SimpleRampData> getSimpleRamps() {
        return simpleRamps;
    }

    public DetectorInput getDetectorInput() {
        return detectorInput;
    }

    public TrafficLightsInput getTrafficLightsInput() {
        return trafficLightsInput;
    }

}
