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
package org.movsim.input.model.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.jdom.Element;
import org.movsim.input.impl.XmlUtils;
import org.movsim.input.model.RoadInput;
import org.movsim.input.model.simulation.FlowConservingBottleneckDataPoint;
import org.movsim.input.model.simulation.HeterogeneityInputData;
import org.movsim.input.model.simulation.ICMacroData;
import org.movsim.input.model.simulation.ICMicroData;
import org.movsim.input.model.simulation.RampData;
import org.movsim.input.model.simulation.SimpleRampData;
import org.movsim.input.model.simulation.SpeedLimitDataPoint;
import org.movsim.input.model.simulation.TrafficLightData;
import org.movsim.input.model.simulation.UpstreamBoundaryData;
import org.movsim.input.model.simulation.impl.FlowConservingBottleneckDataPointImpl;
import org.movsim.input.model.simulation.impl.HeterogeneityInputDataImpl;
import org.movsim.input.model.simulation.impl.ICMacroDataImpl;
import org.movsim.input.model.simulation.impl.ICMicroDataImpl;
import org.movsim.input.model.simulation.impl.RampDataImpl;
import org.movsim.input.model.simulation.impl.SimpleRampDataImpl;
import org.movsim.input.model.simulation.impl.SpeedLimitDataPointImpl;
import org.movsim.input.model.simulation.impl.TrafficLightDataImpl;
import org.movsim.input.model.simulation.impl.UpstreamBoundaryDataImpl;

// TODO: Auto-generated Javadoc
/**
 * The Class RoadInputImpl.
 */
public class RoadInputImpl implements RoadInput {

    private long id;

    private double roadLength;
    private int lanes;

    private boolean isWithWriteFundamentalDiagrams;

    private List<HeterogeneityInputData> heterogeneityInputData;

    private List<ICMacroData> icMacroData;
    private List<ICMicroData> icMicroData;

    private UpstreamBoundaryData upstreamBoundaryData;

    private List<FlowConservingBottleneckDataPoint> flowConsBottleneckInputData;
    private List<SpeedLimitDataPoint> speedLimitInputData;

    private List<SimpleRampData> simpleRamps;
    private List<RampData> ramps;

    private List<TrafficLightData> trafficLightData;

    /**
     * Instantiates a new road input impl.
     * 
     * @param elem
     *            the elem
     */
    public RoadInputImpl(Element elem) {
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
        roadLength = Double.parseDouble(elem.getAttributeValue("x_max"));
        lanes = Integer.parseInt(elem.getAttributeValue("lanes"));

        // -----------------------------------------------------------

        // heterogeneity element with vehicle types

        final Element heterogenElem = elem.getChild("TRAFFIC_COMPOSITION");
        isWithWriteFundamentalDiagrams = heterogenElem.getAttributeValue("write_fund_diagrams").equals("true") ? true
                : false;
        final List<Element> vehTypeElems = elem.getChild("TRAFFIC_COMPOSITION").getChildren("VEHICLE_TYPE");
        heterogeneityInputData = new ArrayList<HeterogeneityInputData>();
        for (final Element vehTypeElem : vehTypeElems) {
            final Map<String, String> map = XmlUtils.putAttributesInHash(vehTypeElem);
            heterogeneityInputData.add(new HeterogeneityInputDataImpl(map));
        }

        // -----------------------------------------------------------

        // Initial Conditions Micro
        final List<Element> icMicroElems = elem.getChild("INITIAL_CONDITIONS").getChildren("IC_MICRO");
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
        final List<Element> icMacroElems = elem.getChild("INITIAL_CONDITIONS").getChildren("IC_MACRO");
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
        final Element upInflowElem = elem.getChild("TRAFFIC_SOURCE");
        upstreamBoundaryData = new UpstreamBoundaryDataImpl(upInflowElem);

        // -----------------------------------------------------------

        // TRAFFIC_SINK
        final Element downInflowElem = elem.getChild("TRAFFIC_SINK");
        // nothing to do (not yet implementend)

        // -----------------------------------------------------------

        // FlowConservingBottlenecks
        flowConsBottleneckInputData = new ArrayList<FlowConservingBottleneckDataPoint>();
        final List<Element> flowConsElems = elem.getChild("FLOW_CONSERVING_INHOMOGENEITIES").getChildren(
                "INHOMOGENEITY");
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

        // -----------------------------------------------------------

        // speed limits
        speedLimitInputData = new ArrayList<SpeedLimitDataPoint>();
        final List<Element> speedLimitElems = elem.getChild("SPEED_LIMITS").getChildren("LIMIT");
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

        // -----------------------------------------------------------

        // non-physical ramps implementing a drop-down mechanism without
        // lane-changing decisions
        simpleRamps = new ArrayList<SimpleRampData>();
        final List<Element> simpleRampElems = elem.getChild("RAMPS").getChildren("SIMPLE_RAMP");
        for (final Element simpleRampElem : simpleRampElems) {
            simpleRamps.add(new SimpleRampDataImpl(simpleRampElem));
        }

        Collections.sort(simpleRamps, new Comparator<SimpleRampData>() {
            @Override
            public int compare(SimpleRampData o1, SimpleRampData o2) {
                final Double pos1 = new Double((o1).getCenterPosition());
                final Double pos2 = new Double((o2).getCenterPosition());
                return pos1.compareTo(pos2); // sort with increasing x
            }
        });

        // -----------------------------------------------------------
        // physical ramps
        ramps = new ArrayList<RampData>();
        final List<Element> rampElems = elem.getChild("RAMPS").getChildren("RAMP");
        for (final Element rampElem : rampElems) {
            ramps.add(new RampDataImpl(rampElem));
        }

        Collections.sort(ramps, new Comparator<RampData>() {
            @Override
            public int compare(RampData o1, RampData o2) {
                final Double pos1 = new Double((o1).getCenterPosition());
                final Double pos2 = new Double((o2).getCenterPosition());
                return pos1.compareTo(pos2); // sort with increasing x
            }
        });

        // -----------------------------------------------------------

        // Trafficlights
        trafficLightData = new ArrayList<TrafficLightData>();
        final List<Element> trafficLigthElems = elem.getChild("TRAFFICLIGHTS").getChildren("TRAFFICLIGHT");
        for (final Element trafficLightElem : trafficLigthElems) {
            final Map<String, String> map = XmlUtils.putAttributesInHash(trafficLightElem);
            trafficLightData.add(new TrafficLightDataImpl(map));
        }

        Collections.sort(trafficLightData, new Comparator<TrafficLightData>() {
            @Override
            public int compare(TrafficLightData o1, TrafficLightData o2) {
                final Double pos1 = new Double((o1).getX());
                final Double pos2 = new Double((o2).getX());
                return pos1.compareTo(pos2); // sort with increasing x
            }
        });

        // -----------------------------------------------------------

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.impl.SimulationInput#getRoadLength()
     */
    @Override
    public double getRoadLength() {
        return roadLength;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.input.model.impl.SimulationInput#getHeterogeneityInputData()
     */
    @Override
    public List<HeterogeneityInputData> getHeterogeneityInputData() {
        return heterogeneityInputData;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.impl.SimulationInput#getIcMacroData()
     */
    @Override
    public List<ICMacroData> getIcMacroData() {
        return icMacroData;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.impl.SimulationInput#getIcMicroData()
     */
    @Override
    public List<ICMicroData> getIcMicroData() {
        return icMicroData;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.RoadInput#getUpstreamBoundaryData()
     */
    @Override
    public UpstreamBoundaryData getUpstreamBoundaryData() {
        return upstreamBoundaryData;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.input.model.impl.SimulationInput#getFlowConsBottleneckInputData
     * ()
     */
    @Override
    public List<FlowConservingBottleneckDataPoint> getFlowConsBottleneckInputData() {
        return flowConsBottleneckInputData;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.RoadInput#getSpeedLimitInputData()
     */
    @Override
    public List<SpeedLimitDataPoint> getSpeedLimitInputData() {
        return speedLimitInputData;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.impl.SimulationInput#getRamps()
     */
    @Override
    public List<RampData> getRamps() {
        return ramps;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.impl.SimulationInput#getTrafficLightData()
     */
    @Override
    public List<TrafficLightData> getTrafficLightData() {
        return trafficLightData;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.impl.SimulationInput#getLanes()
     */
    @Override
    public int getLanes() {
        return lanes;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.RoadInput#getId()
     */
    @Override
    public long getId() {
        return id;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.RoadInput#isWithWriteFundamentalDiagrams()
     */
    @Override
    public boolean isWithWriteFundamentalDiagrams() {
        return isWithWriteFundamentalDiagrams;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.RoadInput#getSimpleRamps()
     */
    @Override
    public List<SimpleRampData> getSimpleRamps() {
        return simpleRamps;
    }

}
