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
package org.movsim.output.route;

import org.movsim.autogen.TravelTimes;
import org.movsim.simulator.roadnetwork.RoadNetwork;
import org.movsim.simulator.roadnetwork.RoadNetworkUtils;
import org.movsim.simulator.roadnetwork.routing.Route;
import org.movsim.utilities.ExponentialMovingAverage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TravelTimeOnRoute extends OutputOnRouteBase {

    /** The Constant LOG. */
    final static Logger LOG = LoggerFactory.getLogger(TravelTimeOnRoute.class);

    private final double tauEMA;

    private final double beta;

    private final FileTravelTimeOnRoute fileWriter;

    private double instantaneousTravelTime;

    private double totalTravelTime;

    private double meanSpeed;

    private double instTravelTimeEMA;

    private int numberOfVehicles;

    public TravelTimeOnRoute(double simulationTimestep, TravelTimes travelTimeInput, RoadNetwork roadNetwork,
            Route route, boolean writeOutput) {
        super(roadNetwork, route);
        this.tauEMA = travelTimeInput.getTauEMA();
        this.beta = Math.exp(-simulationTimestep / tauEMA);
        fileWriter = writeOutput ? new FileTravelTimeOnRoute(travelTimeInput.getDt(), route) : null;
        totalTravelTime = 0;
    }

    @Override
    public void timeStep(double dt, double simulationTime, long iterationCount) {

        numberOfVehicles = Math.max(0, RoadNetworkUtils.vehicleCount(route) - roadNetwork.obstacleCount(route));

        instantaneousTravelTime = RoadNetworkUtils.instantaneousTravelTime(route);

        // TODO check quantity
        // totalTravelTime += numberOfVehicles * instantaneousTravelTime;
        // totalTravelTime += instantaneousTravelTime;
        totalTravelTime += dt * numberOfVehicles;

        meanSpeed = route.getLength() / instantaneousTravelTime;

        instTravelTimeEMA = (simulationTime == 0) ? instantaneousTravelTime : ExponentialMovingAverage.calc(
                instantaneousTravelTime, instTravelTimeEMA, beta);

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

    public double getInstantaneousTravelTimeEMA() {
        return instTravelTimeEMA;
    }

    public double getTotalTravelTime() {
        return totalTravelTime;
    }

    public int getNumberOfVehicles() {
        return numberOfVehicles;
    }

}
