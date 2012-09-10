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
package org.movsim.output.traveltime;

import org.movsim.simulator.SimulationTimeStep;
import org.movsim.simulator.roadnetwork.RoadNetwork;
import org.movsim.simulator.roadnetwork.Route;

public class TravelTimeOnRoute implements SimulationTimeStep {
    
    private final Route route;

    /** configures update interval. Initial value = 100 */
    private long updateIntervalCount = 100;
    
    private final RoadNetwork roadNetwork;
    
    private final FileTravelTime fileWriter;

    public TravelTimeOnRoute(RoadNetwork roadNetwork, Route route, boolean writeOutput) {
        this.roadNetwork = roadNetwork;
        this.route = route;
        
        fileWriter = writeOutput ? new FileTravelTime(route) : null; 
    }

    @Override
    public void timeStep(double dt, double simulationTime, long iterationCount) {

        final double instantaneousTravelTime = roadNetwork.instantaneousTravelTime(route);
        
        if(fileWriter != null){
            fileWriter.write(simulationTime, instantaneousTravelTime);
        }
//        final boolean doNotificationUpdate = (iterationCount % updateIntervalCount == 0);
//        for (final TravelTimeRoute route : traveltimeRoutes) {
//            route.update(simulationTime, iterationCount, roadNetwork);
//            if (doNotificationUpdate) {
//                route.calcEMA(simulationTime);
//            }
//        }
//
//        if (doNotificationUpdate) {
//            notifyObservers(simulationTime);
//        }
    }

//    public List<List<XYDataPoint>> getTravelTimeEmas() {
//        final List<List<XYDataPoint>> listOfEmas = new LinkedList<List<XYDataPoint>>();
//        for (final TravelTimeRoute route : traveltimeRoutes) {
//            listOfEmas.add(route.getEmaPoints());
//        }
//        return listOfEmas;
//    }
//
//    public List<List<XYDataPoint>> getTravelTimeDataRoutes() {
//        final List<List<XYDataPoint>> listOfRoutes = new LinkedList<List<XYDataPoint>>();
//        for (final TravelTimeRoute route : traveltimeRoutes) {
//            listOfRoutes.add(route.getDataPoints());
//        }
//        return listOfRoutes;
//    }
//
//    public void setUpdateInterval(long updateIntervalCount) {
//        this.updateIntervalCount = updateIntervalCount;
//    }
//
//    public List<Double> getTravelTimesEMA(double time, double tauEMA) {
//        final int N_DATA = 10; // cut-off parameter
//        final ExponentialMovingAverage ema = new ExponentialMovingAverage(tauEMA);
//
//        final List<Double> ttEMAs = new LinkedList<Double>();
//        for (final TravelTimeRoute route : traveltimeRoutes) {
//            final List<XYDataPoint> routeTravelTimes = route.getDataPoints();
//            final int size = routeTravelTimes.size();
//            ttEMAs.add(ema.calcEMA(time, routeTravelTimes.subList(Math.max(0, size - N_DATA), size)));
//        }
//        return ttEMAs;
//    }

}
