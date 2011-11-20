/**
 * Copyright (C) 2010, 2011 by Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden <info@movsim.org>
 * ---------------------------------------------------------------------- This file is part of MovSim - the multi-model
 * open-source vehicular-traffic simulator MovSim is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version. MovSim is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License along with MovSim. If not, see
 * <http://www.gnu.org/licenses/> or <http://www.movsim.org>.
 * ----------------------------------------------------------------------
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
import org.movsim.input.model.simulation.SpeedLimitDataPoint;
import org.movsim.input.model.simulation.TrafficCompositionInputData;
import org.movsim.input.model.simulation.TrafficLightsInput;
import org.movsim.input.model.simulation.TrafficSinkData;
import org.movsim.input.model.simulation.TrafficSourceData;
import org.movsim.input.model.simulation.impl.DetectorInputImpl;
import org.movsim.input.model.simulation.impl.FlowConservingBottleneckDataPointImpl;
import org.movsim.input.model.simulation.impl.ICMacroDataImpl;
import org.movsim.input.model.simulation.impl.ICMicroDataImpl;
import org.movsim.input.model.simulation.impl.SimpleRampDataImpl;
import org.movsim.input.model.simulation.impl.SpeedLimitDataPointImpl;
import org.movsim.input.model.simulation.impl.TrafficCompositionDataImpl;
import org.movsim.input.model.simulation.impl.TrafficLightsInputImpl;
import org.movsim.input.model.simulation.impl.TrafficSinkDataImpl;
import org.movsim.input.model.simulation.impl.TrafficSourceDataImpl;

public class RoadInput {

    /** The id. */
    private long id;

    /** The road length. */
    private double roadLength;

    /** The lanes. */
    private int lanes;
    
    /** The ramp length. */
    // TODO this is a property of an onramp/offramp and not of an mainroad 
    private double rampMergingLength;
  
    /** The center position. */
    // TODO this is a property of an onramp/offramp and not of an mainroad
    private double rampStartPosition;

    /** The is with write fundamental diagrams. */
    private boolean isWithWriteFundamentalDiagrams;

    /** The heterogeneity input data. */
    private List<TrafficCompositionInputData> trafficCompositionInputData;

    /** The ic macro data. */
    private List<ICMacroData> icMacroData;

    /** The ic micro data. */
    private List<ICMicroData> icMicroData;

    /** The upstream boundary data. */
    private TrafficSourceData trafficSourceData;
    
    private TrafficSinkData trafficSinkData;

    /** The flow cons bottleneck input data. */
    private List<FlowConservingBottleneckDataPoint> flowConsBottleneckInputData;

    /** The speed limit input data. */
    private List<SpeedLimitDataPoint> speedLimitInputData;

    /** The simple ramps. */
    private List<SimpleRampData> simpleRamps;

    private TrafficLightsInput trafficLightsInput;

    /** The detector input. */
    private DetectorInput detectorInput;

    /**
     * Instantiates a new road input impl.
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

        id = Long.parseLong(elem.getAttributeValue("id"));
//        roadLength = Double.parseDouble(elem.getAttributeValue("length"));
//        lanes = Integer.parseInt(elem.getAttributeValue("lanes"));

//        rampStartPosition = Double.parseDouble(elem.getAttributeValue("x"));
//        rampMergingLength = Double.parseDouble(elem.getAttributeValue("merge_length"));
        // -----------------------------------------------------------

        // heterogeneity element with vehicle types
        trafficCompositionInputData = new ArrayList<TrafficCompositionInputData>();
        
        final Element heterogenElem = elem.getChild(XmlElementNames.TrafficComposition);
        // optional for specific road
        if (heterogenElem != null) {
            isWithWriteFundamentalDiagrams = heterogenElem.getAttributeValue("write_fund_diagrams").equals("true") ? true
                    : false;
            final List<Element> vehTypeElems = elem.getChild(XmlElementNames.TrafficComposition).getChildren(
                    XmlElementNames.RoadVehicleType);
            for (final Element vehTypeElem : vehTypeElems) {
                final Map<String, String> map = XmlUtils.putAttributesInHash(vehTypeElem);
                trafficCompositionInputData.add(new TrafficCompositionDataImpl(map));
            }
        }
        
        
        // -----------------------------------------------------------

        // Initial Conditions Micro
        final List<Element> icMicroElems = elem.getChild(XmlElementNames.RoadInitialConditions).getChildren(
                XmlElementNames.RoadInitialConditionsIcMicro);
        icMicroData = new ArrayList<ICMicroData>();
        for (final Element icMicroElem : icMicroElems) {
            final Map<String, String> map = XmlUtils.putAttributesInHash(icMicroElem);
            icMicroData.add(new ICMicroDataImpl(map));
        }

        Collections.sort(icMicroData, new Comparator<ICMicroData>() {
            @Override
            public int compare(ICMicroData o1, ICMicroData o2) {
                final Double pos1 = new Double((o1).getX());
                final Double pos2 = new Double((o2).getX());
                return pos2.compareTo(pos1); // sort with DECREASING x because
                                             // of FC veh counting
            }
        });

        // -----------------------------------------------------------

        // Initial Conditions Macro
        final List<Element> icMacroElems = elem.getChild(XmlElementNames.RoadInitialConditions).getChildren(
                XmlElementNames.RoadInitialConditionsIcMacro);
        icMacroData = new ArrayList<ICMacroData>();
        for (final Element icMacroElem : icMacroElems) {
            final Map<String, String> map = XmlUtils.putAttributesInHash(icMacroElem);
            icMacroData.add(new ICMacroDataImpl(map));
        }

        Collections.sort(icMacroData, new Comparator<ICMacroData>() {
            @Override
            public int compare(ICMacroData o1, ICMacroData o2) {
                final Double pos1 = new Double((o1).getX());
                final Double pos2 = new Double((o2).getX());
                return pos1.compareTo(pos2); // sort with increasing x
            }
        });

        // -----------------------------------------------------------

        // TRAFFIC_SOURCE
        final Element roadSourceElem = elem.getChild(XmlElementNames.RoadTrafficSource);
        trafficSourceData = new TrafficSourceDataImpl(roadSourceElem);

        // -----------------------------------------------------------

        // TRAFFIC_SINK
        final Element roadSinkElem = elem.getChild(XmlElementNames.RoadTrafficSink);
        trafficSinkData = new TrafficSinkDataImpl(roadSinkElem);

        // -----------------------------------------------------------

        // FlowConservingBottlenecks
        flowConsBottleneckInputData = new ArrayList<FlowConservingBottleneckDataPoint>();
        final Element flowConsBottlenecksElement = elem.getChild(XmlElementNames.RoadFlowConservingInhomogeneities);
        if (flowConsBottlenecksElement != null) {
            final List<Element> flowConsElems = flowConsBottlenecksElement
                    .getChildren(XmlElementNames.RoadInhomogeneity);
            for (final Element flowConsElem : flowConsElems) {
                final Map<String, String> map = XmlUtils.putAttributesInHash(flowConsElem);
                flowConsBottleneckInputData.add(new FlowConservingBottleneckDataPointImpl(map));
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

        // -----------------------------------------------------------

        // speed limits
        speedLimitInputData = new ArrayList<SpeedLimitDataPoint>();
        final Element speedLimitsElement = elem.getChild(XmlElementNames.RoadSpeedLimits);
        if (speedLimitsElement != null) {
            final List<Element> speedLimitElems = speedLimitsElement.getChildren(XmlElementNames.RoadSpeedLimit);
            for (final Element speedLimitElem : speedLimitElems) {
                final Map<String, String> map = XmlUtils.putAttributesInHash(speedLimitElem);
                speedLimitInputData.add(new SpeedLimitDataPointImpl(map));
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

        // -----------------------------------------------------------

        // non-physical ramps implementing a drop-down mechanism without
        // lane-changing decisions
        simpleRamps = new ArrayList<SimpleRampData>();
       
        final Element rampsElement = elem.getChild(XmlElementNames.RoadRamps);
        if (rampsElement != null) {
            final List<Element> simpleRampElems = rampsElement.getChildren(XmlElementNames.RoadSimpleRamp);
            for (final Element simpleRampElem : simpleRampElems) {
                simpleRamps.add(new SimpleRampDataImpl(simpleRampElem));
            }

            Collections.sort(simpleRamps, new Comparator<SimpleRampData>() {
                @Override
                public int compare(SimpleRampData o1, SimpleRampData o2) {
                    final Double pos1 = new Double((o1).getRampStartPosition());
                    final Double pos2 = new Double((o2).getRampStartPosition());
                    return pos1.compareTo(pos2); // sort with increasing x
                }
            });

            // -----------------------------------------------------------
            // physical ramps
            //ramps = new ArrayList<RampData>();
//            final List<Element> rampElems = rampsElement.getChildren(XmlElementNames.RoadRamp);
//            for (final Element rampElem : rampElems) {
//                ramps.add(new RampDataImpl(rampElem));
//            }
//
//            Collections.sort(ramps, new Comparator<RampData>() {
//                @Override
//                public int compare(RampData o1, RampData o2) {
//                    final Double pos1 = new Double((o1).getRampStartPosition());
//                    final Double pos2 = new Double((o2).getRampStartPosition());
//                    return pos1.compareTo(pos2); // sort with increasing x
//                }
//            });
        }

        // -----------------------------------------------------------

        // Trafficlights

        final Element trafficLightsElement = elem.getChild(XmlElementNames.RoadTrafficLights);
        trafficLightsInput = new TrafficLightsInputImpl(trafficLightsElement);

        // -----------------------------------------------------------

        detectorInput = new DetectorInputImpl(elem.getChild(XmlElementNames.OutputDetectors));

        // -----------------------------------------------------------

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.impl.SimulationInput#getRoadLength()
     */
    public double getRoadLength() {
        return roadLength;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.input.model.impl.SimulationInput#getHeterogeneityInputData()
     */
    public List<TrafficCompositionInputData> getTrafficCompositionInputData() {
        return trafficCompositionInputData;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.impl.SimulationInput#getIcMacroData()
     */
    public List<ICMacroData> getIcMacroData() {
        return icMacroData;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.impl.SimulationInput#getIcMicroData()
     */
    public List<ICMicroData> getIcMicroData() {
        return icMicroData;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.RoadInput#getUpstreamBoundaryData()
     */
    public TrafficSourceData getTrafficSourceData() {
        return trafficSourceData;
    }
    
    public TrafficSinkData getTrafficSinkData() {
        return trafficSinkData;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.input.model.impl.SimulationInput#getFlowConsBottleneckInputData
     * ()
     */
    public List<FlowConservingBottleneckDataPoint> getFlowConsBottleneckInputData() {
        return flowConsBottleneckInputData;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.RoadInput#getSpeedLimitInputData()
     */
    public List<SpeedLimitDataPoint> getSpeedLimitInputData() {
        return speedLimitInputData;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.impl.SimulationInput#getLanes()
     */
    public int getLanes() {
        return lanes;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.RoadInput#getId()
     */
    public long getId() {
        return id;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.RoadInput#isWithWriteFundamentalDiagrams()
     */
    public boolean isWithWriteFundamentalDiagrams() {
        return isWithWriteFundamentalDiagrams;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.RoadInput#getSimpleRamps()
     */
    public List<SimpleRampData> getSimpleRamps() {
        return simpleRamps;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.impl.OutputInput#getDetectorInput()
     */
    public DetectorInput getDetectorInput() {
        return detectorInput;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.RoadInput#getTrafficLightsInput()
     */
    public TrafficLightsInput getTrafficLightsInput() {
        return trafficLightsInput;
    }

    public double getRampMergingLength() {
        return rampMergingLength;
    }

    public double getRampStartPosition() {
        return rampStartPosition;
    }

    

}
