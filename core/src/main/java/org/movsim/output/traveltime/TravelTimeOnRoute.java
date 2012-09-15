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

import org.movsim.input.model.output.TravelTimeOnRouteInput;
import org.movsim.simulator.SimulationTimeStep;
import org.movsim.simulator.roadnetwork.RoadNetwork;
import org.movsim.simulator.roadnetwork.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TravelTimeOnRoute implements SimulationTimeStep {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(TravelTimeOnRoute.class);

    private static final double TAU_EMA = 30;

    private final Route route;

    /** configures update interval. Initial value = 100 */
    private long updateIntervalCount = 100;

    private final RoadNetwork roadNetwork;

    private final FileTravelTime fileWriter;

    private double instantaneousTravelTime;

    private double meanSpeed;

    private double instTravelTimeEMA;

    private double beta;

    public TravelTimeOnRoute(double simulationTimestep, TravelTimeOnRouteInput input, RoadNetwork roadNetwork,
            Route route, boolean writeOutput) {
        this.roadNetwork = roadNetwork;
        this.route = route;
        this.beta = Math.exp(-simulationTimestep / TAU_EMA);
        fileWriter = writeOutput ? new FileTravelTime(input.getDt(), route) : null;
    }

    @Override
    public void timeStep(double dt, double simulationTime, long iterationCount) {

        instantaneousTravelTime = roadNetwork.instantaneousTravelTime(route);

        meanSpeed = route.getLength() / instantaneousTravelTime;

        instTravelTimeEMA = calcEMA(instantaneousTravelTime, instTravelTimeEMA);

        if (fileWriter != null) {
            fileWriter.write(simulationTime, this);
        }
    }

    private double calcEMA(double xNew, double xEMA) {
        return (1-beta) * xNew + beta * xEMA;
    }

    public double getInstantaneousTravelTime() {
        return instantaneousTravelTime;
    }

    public double getMeanSpeed() {
        return meanSpeed;
    }
    
    public double getInstantaneousTravelTimeEMA(){
        return instTravelTimeEMA;
    }

}
