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

package org.movsim.simulator.roadnetwork;

import org.movsim.simulator.SimulationTimeStep;

/**
 * Default sink: just removes vehicles that have reached the end of a road segment.
 */
public class TrafficSink implements SimulationTimeStep {

    // For sinks roadSegment is the source road
    protected RoadSegment roadSegment;
    private static final double MEASURING_INTERVAL = 60.0; // seconds
    private double measuredOutflow;
    private double dQ;
    private double measuredTime;

    /**
     * Constructor.
     * 
     * @param roadSegment
     */
    public TrafficSink(RoadSegment roadSegment) {
        // for TrafficSinks and similar roadSegment is the source road
        setRoadSegment(roadSegment);
    }

    protected final void setRoadSegment(RoadSegment roadSegment) {
        // a source has its road segment set once and only once, by the road segment
        // in its setSource method
        assert this.roadSegment == null;
        assert roadSegment != null;
        // assert roadSegment.source() == this || type != Type.SOURCE;

        this.roadSegment = roadSegment;
    }

    /**
     * Returns this traffic source's source road segment.
     * 
     * @return this traffic source's source road segment
     */
    public final RoadSegment sourceRoad() {
        return roadSegment;
    }

    /**
     * Returns the outflow, averaged over the measuring interval.
     * 
     * @return measured outflow
     * 
     */
    public double measuredOutflow() {
        return measuredOutflow;
    }

    /**
     * Returns the difference between the source road's inflow and the outflow measured at this sink, averaged over
     * <code>MEASURING_INTERVAL</code> seconds.
     * 
     * @return difference in flow
     */
    public double dQ() {
        return dQ;
    }

    /**
     * Removes any vehicles that have gone past the end of the source road.
     */
    @Override
    public void timeStep(double dt, double simulationTime, long iterationCount) {
        final RoadSegment sourceRoad = sourceRoad();
        // remove any vehicles that have gone past the end of the road segment
        sourceRoad.removeVehiclesPastEnd();
        measuredTime += dt;
        if (measuredTime > MEASURING_INTERVAL) {
            measuredOutflow = sourceRoad.removedVehicleCount() / MEASURING_INTERVAL; // vehicles per second
            sourceRoad.clearVehicleRemovedCount();
            measuredTime = 0.0;
            // dQ = 0.0;
            // if (sourceRoad.source() != null) {
            // dQ = (measuredOutflow - sourceRoad.source().inflow());
            // }

            //            System.out.println("Sink (R" + roadSegment.roadId() +",S" + id + ") outflow: " + (int)(measuredOutflow * 3600));//$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
            //            System.out.println("Sink R" + roadSegment.roadId() + " outflow: " + (int)(measuredOutflow * 3600));//$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
            // if (roadSegment.source().type() == TrafficFlowBase.Type.SOURCE) {
            //                System.out.println("  flow:" + (int)(roadSegment.source().inFlow() * 3600));//$NON-NLS-1$
            //                System.out.println("    dQ:" + (int)(dQ * 3600));//$NON-NLS-1$
            // }
        }
    }
}
