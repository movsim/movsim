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

import org.movsim.autogen.Parking;
import org.movsim.simulator.SimulationTimeStep;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.utilities.Units;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default sink: just removes vehicles that have reached the end of a road segment.
 */
public class TrafficSink implements SimulationTimeStep {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(TrafficSink.class);
    
    // For sinks roadSegment is the source road
    protected RoadSegment roadSegment;
    // measure actual outflow 
    private static final double MEASURING_INTERVAL_S = 60.0;
    private int vehiclesRemovedInInterval;
    private double measuredOutflow;
    private double measuredTime;
    private double dQ;
    private int totalVehiclesRemoved;
    private double totalVehicleTravelDistance;
    private double totalVehicleTravelTime;
    private double totalVehicleFuelUsedLiters;

    private TrafficSourceMicro reEntranceTrafficSource;
    private double timeDelayReentrance;

    /**
     * Constructor.
     * 
     * @param roadSegment
     */
    public TrafficSink(RoadSegment roadSegment) {
        // for TrafficSinks and similar roadSegment is the source road
        setRoadSegment(roadSegment);
        measuredTime = 0;
        measuredOutflow = 0;
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
     * Returns the difference between the source road's inflow and the outflow measured at this sink,
     * averaged over <code>MEASURING_INTERVAL</code> seconds.
     * 
     * @return difference in flow
     */
    public double dQ() {
        return dQ;
    }

    /**
     * Returns the total travel distance of all vehicles that have been removed by this traffic sink.
     * @return total travel distance
     */
    public final double totalVehicleTravelDistance() {
        return totalVehicleTravelDistance;
    }

    /**
     * Returns the total travel time of all vehicles that have been removed by this traffic sink.
     * @return total travel time
     */
    public final double totalVehicleTravelTime() {
        return totalVehicleTravelTime;
    }

    /**
     * Returns the total fuel used by all vehicles that have been removed by this traffic sink.
     * @return total fuel used
     */
    public final double totalFuelUsedLiters() {
        return totalVehicleFuelUsedLiters;
    }
    
    void recordRemovedVehicle(Vehicle vehicle) {
        totalVehicleTravelDistance += vehicle.totalTravelDistance();
        totalVehicleTravelTime += vehicle.totalTravelTime();
        totalVehicleFuelUsedLiters += vehicle.totalFuelUsedLiters();
        ++totalVehiclesRemoved;
    }

    /**
     * Removes any vehicles that have gone past the end of the source road.
     */
    @Override
    public void timeStep(double dt, double simulationTime, long iterationCount) {
        if (reEntranceTrafficSource != null) {
            addVehiclesToSource(simulationTime, sourceRoad().getVehiclesPastEnd());
        }
        vehiclesRemovedInInterval += sourceRoad().removeVehiclesPastEnd();
        measuredTime += dt;
        if (measuredTime > MEASURING_INTERVAL_S) {
            measuredOutflow = vehiclesRemovedInInterval / MEASURING_INTERVAL_S; // vehicles per second
            vehiclesRemovedInInterval = 0;
            measuredTime = 0.0;
            LOG.debug("sink in roadSegment with id={} has measured outflow of {} over all lanes ", 
                    sourceRoad().id(), measuredOutflow*Units.INVS_TO_INVH);
        }
    }

    private void addVehiclesToSource(double simulationTime, Iterable<Vehicle> vehiclesPastEnd) {
        for (Vehicle vehicle : vehiclesPastEnd) {
            long reEntranceTime = (long) (simulationTime + timeDelayReentrance);
            reEntranceTrafficSource.addVehicleToQueue(reEntranceTime, vehicle);
        }
    }

    public void setupParkingLot(Parking parking, long timeOffsetMillis, TrafficSourceMicro trafficSource) {
        this.reEntranceTrafficSource = trafficSource;
        this.timeDelayReentrance = parking.getTimeDelay();
    }
}
