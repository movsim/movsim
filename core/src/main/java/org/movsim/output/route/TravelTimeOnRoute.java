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
package org.movsim.output.route;

import org.movsim.input.model.output.TravelTimeOnRouteInput;
import org.movsim.simulator.roadnetwork.RoadNetwork;
import org.movsim.simulator.roadnetwork.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TravelTimeOnRoute extends OutputOnRouteBase {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(TravelTimeOnRoute.class);

    private static final double TAU_EMA = 30;
    
    private final double beta;

    private final FileTravelTimeOnRoute fileWriter;

    private double instantaneousTravelTime;

    private double meanSpeed;

    private double instTravelTimeEMA;

    public TravelTimeOnRoute(double simulationTimestep, TravelTimeOnRouteInput input, RoadNetwork roadNetwork,
            Route route, boolean writeOutput) {
        super(roadNetwork, route);
        this.beta = Math.exp(-simulationTimestep / TAU_EMA);
        fileWriter = writeOutput ? new FileTravelTimeOnRoute(input.getDt(), route) : null;
    }

    @Override
    public void timeStep(double dt, double simulationTime, long iterationCount) {

        instantaneousTravelTime = roadNetwork.instantaneousTravelTime(route);

        meanSpeed = route.getLength() / instantaneousTravelTime;

        instTravelTimeEMA = calcEMA(beta, instantaneousTravelTime, instTravelTimeEMA);

        if (fileWriter != null) {
            fileWriter.write(simulationTime, this);
        }
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
