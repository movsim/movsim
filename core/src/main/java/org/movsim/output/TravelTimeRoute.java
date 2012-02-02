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
package org.movsim.output;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.movsim.simulator.roadnetwork.RoadNetwork;
import org.movsim.simulator.roadnetwork.RoadSegment;
import org.movsim.simulator.roadnetwork.Route;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.utilities.ExponentialMovingAverage;
import org.movsim.utilities.XYDataPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TravelTimeRoute {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(TravelTimeRoute.class);

    private final String startId;
    // private String endId;
    private final double startPosition;
    private final double endPosition;

    private final Map<Vehicle, Double> vehiclesOnRoute;

    private final List<XYDataPoint> dataPoints;

    private static final double tauEMA = 60;
    private static final double betaEMA = Math.exp(-1.0 / tauEMA);
    private static final int N_DATA = 30; // cut-off parameter TODO inconsistent with tauEMA
    private static final ExponentialMovingAverage ema = new ExponentialMovingAverage(tauEMA);

    private double travelTimeEMA = 0;
    private final List<XYDataPoint> emaPoints;

    /**
     * Constructor.
     * 
     * @param travelTimeRouteInput
     */
    public TravelTimeRoute(Route route) {
        final Iterator<RoadSegment> iter = route.iterator();
        this.startId = iter.next().userId();
        // this.endId = startId;
        // while (iter.hasNext()) {
        // this.endId = iter.next().userId();
        // }
        this.startPosition = 0;
        this.endPosition = route.getLength();

        vehiclesOnRoute = new HashMap<Vehicle, Double>();
        dataPoints = new LinkedList<XYDataPoint>();
        emaPoints = new LinkedList<XYDataPoint>();

        logger.info("consider travel times on route {} with startId={}", route.getName(), startId);
        logger.info("with startPos={}, endPos={}", startPosition, endPosition);
    }

    public void calcEMA(double time) {
        final int size = dataPoints.size();
        final double emaValue = ema.calcEMA(time, dataPoints.subList(Math.max(0, size - N_DATA), size));
        emaPoints.add(new XYDataPoint(time, emaValue));
    }

    public List<XYDataPoint> getEmaPoints() {
        return emaPoints;
    }

    /**
     * Update
     * 
     * @param simulationTime
     *            current simulation time, seconds
     * @param iterationCount
     *            the number of iterations that have been executed
     * @param roadNetwork
     */
    public void update(double simulationTime, long iterationCount, RoadNetwork roadNetwork) {

        // dataPoints.clear();
        // check first start_position
        // TODO catch error if road id not available

        checkNewVehicles(simulationTime, roadNetwork.findByUserId(startId));

        // check end_position
        final double averageNewTT = checkPassedVehicles(simulationTime);

        travelTimeEMA = betaEMA * travelTimeEMA + (1 - betaEMA) * averageNewTT;
    }

    public double getTravelTimeEMA() {
        return travelTimeEMA;
    }

    public List<XYDataPoint> getDataPoints() {
        return dataPoints;
    }

    private void checkNewVehicles(final double timeStartOfRoute, final RoadSegment roadSegment) {
        // for(final LaneSegment laneSegment : roadSegment){
        for (final Vehicle veh : roadSegment) {
            // if(veh.getPosition() > 100 && veh.getPosition()<1000){
            // System.out.printf("veh: pos=%.4f, posOld=%.4f\n", veh.getPosition(), veh.getPositionOld());
            // }
            if (veh.getFrontPositionOld() < startPosition && veh.getFrontPosition() > startPosition) {
                vehiclesOnRoute.put(veh, timeStartOfRoute);
                // System.out.printf("veh at x=%.2f put to travel time route, roadId=%d\n", veh.getPosition(), veh.getRoadId());
            }
        }
        // }
    }

    private double checkPassedVehicles(final double timeEndOfRoute) {
        final double ttAverage = 0;
        final List<Vehicle> stagedVehicles = new LinkedList<Vehicle>();
        for (final Map.Entry<Vehicle, Double> entry : vehiclesOnRoute.entrySet()) {
            final Vehicle vehicle = entry.getKey();
            final double startTime = entry.getValue();
            // System.out.printf("consider vehicle ... roadId=%d, pos=%.4f\n", veh.getRoadId(), veh.getPosition());
            // FIXME roadIds are from the old concept
            // if (vehicle.getRoadId() == endId && vehicle.getMidPosition() > endPosition) {
            // final double travelTimeOnRoute = timeEndOfRoute - startTime;
            // dataPoints.add(new XYDataPoint(timeEndOfRoute, travelTimeOnRoute));
            // // System.out.printf("vehicle with finished traveltime route: startTime=%.4f, endTime=%.4f, tt=%.4f\n", startTime,
            // // timeEndOfRoute,travelTimeOnRoute);
            // stagedVehicles.add(vehicle);
            // ttAverage += travelTimeOnRoute;
            // }
        }
        for (final Vehicle vehicle : stagedVehicles) {
            vehiclesOnRoute.remove(vehicle);
            // System.out.printf("remove vehicle at x=%.2f from route map", vehicle.getPosition());
        }
        return stagedVehicles.size() == 0 ? 0 : ttAverage / stagedVehicles.size();
    }
}
