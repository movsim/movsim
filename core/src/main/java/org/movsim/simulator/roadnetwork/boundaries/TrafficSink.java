/*
 * Copyright (C) 2010, 2011, 2012 by Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden
 * <movsim.org@gmail.com>
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

package org.movsim.simulator.roadnetwork.boundaries;

import com.google.common.base.Preconditions;
import org.movsim.simulator.SimulationTimeStep;
import org.movsim.simulator.roadnetwork.RoadSegment;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.utilities.Units;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default sink: just removes vehicles that have reached the end of a road segment.
 */
public class TrafficSink implements SimulationTimeStep {

    private static final Logger LOG = LoggerFactory.getLogger(TrafficSink.class);

    protected final RoadSegment roadSegment;

    // measure actual outflow
    private static final double MEASURING_INTERVAL_S = 60.0;
    private int vehiclesRemovedInInterval;
    private double measuredOutflow;// vehicles per second
    private double measuredTime;
    private double dQ;
    private int totalVehiclesRemoved;
    private double totalVehicleTravelDistance;
    private double totalVehicleTravelTime;
    private double totalVehicleFuelUsedLiters;

    RecordDataCallback recordDataCallback;
    private double simulationTime;

    public interface RecordDataCallback {
        /**
         * Callback to allow the application to process or record the traffic sink data.
         *
         * @param vehicle
         * @param totalVehiclesRemoved
         */
        void recordData(double simulationTime, int totalVehiclesRemoved, Vehicle vehicle);
    }

    /**
     * Constructor.
     *
     * @param roadSegment
     */
    public TrafficSink(RoadSegment roadSegment) {
        this.roadSegment = Preconditions.checkNotNull(roadSegment);
        measuredTime = 0;
        measuredOutflow = 0;
        totalVehiclesRemoved = 0;
    }

    /**
     * Sets the traffic sink recorder.
     *
     * @param recordDataCallback
     */
    public void setRecorder(RecordDataCallback recordDataCallback) {
        this.recordDataCallback = Preconditions.checkNotNull(recordDataCallback);
    }

    /**
     * Returns the outflow, averaged over the measuring interval.
     *
     * @return measured outflow
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
     *
     * @return total travel distance
     */
    public final double totalVehicleTravelDistance() {
        return totalVehicleTravelDistance;
    }

    /**
     * Returns the total travel time of all vehicles that have been removed by this traffic sink.
     *
     * @return total travel time
     */
    public final double totalVehicleTravelTime() {
        return totalVehicleTravelTime;
    }

    /**
     * Returns the total fuel used by all vehicles that have been removed by this traffic sink.
     *
     * @return total fuel used
     */
    public final double totalFuelUsedLiters() {
        return totalVehicleFuelUsedLiters;
    }

    public final int totalVehiclesRemoved() {
        return totalVehiclesRemoved;
    }

    public void recordRemovedVehicle(Vehicle vehicle) {
        totalVehicleTravelDistance += vehicle.totalTravelDistance();
        totalVehicleTravelTime += vehicle.totalTravelTime();
        totalVehicleFuelUsedLiters += vehicle.getEnergyModel().totalFuelUsedLiters();
        ++totalVehiclesRemoved;
        if (recordDataCallback != null) {
            recordDataCallback.recordData(simulationTime, totalVehiclesRemoved, vehicle);
        }
    }

    /**
     * Removes any vehicles that have gone past the end of the source road.
     */
    @Override
    public void timeStep(double dt, double simulationTime, long iterationCount) {
        this.simulationTime = simulationTime;
        vehiclesRemovedInInterval += roadSegment.removeVehiclesPastEnd();
        measuredTime += dt;

        if (measuredTime > MEASURING_INTERVAL_S) {
            measuredOutflow = vehiclesRemovedInInterval / MEASURING_INTERVAL_S;
            vehiclesRemovedInInterval = 0;
            measuredTime = 0.0;
            LOG.debug("sink in roadSegment with userId={} has measured outflow of {} over all lanes ",
                    roadSegment.userId(), measuredOutflow * Units.INVS_TO_INVH);
        }
    }

}
