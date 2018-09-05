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
import org.movsim.simulator.roadnetwork.RoadNetworkUtils.TravelTimeType;
import org.movsim.simulator.roadnetwork.routing.Route;
import org.movsim.utilities.ExponentialMovingAverage;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.Map;

public class TravelTimeOnRoute extends OutputOnRouteBase {

    private final double tauEMA;

    private final double beta;

    private final Map<TravelTimeType, TravelTime> travelTimes = new EnumMap<>(TravelTimeType.class);

    public TravelTimeOnRoute(double simulationTimestep, TravelTimes travelTimeInput, RoadNetwork roadNetwork,
            Route route, boolean writeOutput) {
        super(roadNetwork, route);
        this.tauEMA = travelTimeInput.getTauEMA();
        this.beta = Math.exp(-simulationTimestep / tauEMA);
        for (TravelTimeType type : TravelTimeType.values()) {
            FileTravelTimeOnRoute writer = writeOutput ?
                    new FileTravelTimeOnRoute(travelTimeInput.getDt(), route, type.toString().toLowerCase()) :
                    null;
            travelTimes.put(type, new TravelTime(writer));
        }
    }

    @Override
    public void timeStep(double dt, double simulationTime, long iterationCount) {
        int numberOfVehicles = Math.max(0, RoadNetworkUtils.vehicleCount(route) - roadNetwork.obstacleCount(route));
        for (TravelTimeType type : TravelTimeType.values()) {
            TravelTime tt = travelTimes.get(type);
            tt.numberOfVehicles = numberOfVehicles;
            tt.instantaneousTravelTime = RoadNetworkUtils.instantaneousTravelTime(route, type);
            tt.totalTravelTime += dt * numberOfVehicles;
            tt.meanSpeed = route.getLength() / tt.instantaneousTravelTime;
            tt.instTravelTimeEMA = (simulationTime == 0) ?
                    tt.instantaneousTravelTime :
                    ExponentialMovingAverage.calc(tt.instantaneousTravelTime, tt.instTravelTimeEMA, beta);
            if (tt.fileWriter != null) {
                tt.fileWriter.write(simulationTime, tt);
            }
        }
    }

    static final class TravelTime {
        private double instantaneousTravelTime;
        private double totalTravelTime;
        private double meanSpeed;
        private double instTravelTimeEMA;
        private int numberOfVehicles;

        private final FileTravelTimeOnRoute fileWriter;

        public TravelTime(@Nullable FileTravelTimeOnRoute fileWriter) {
            this.fileWriter = fileWriter;
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

}
