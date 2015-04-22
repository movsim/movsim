package org.movsim.simulator.roadnetwork;

import org.movsim.simulator.roadnetwork.routing.Route;

public final class RoadNetworkUtils {

    private RoadNetworkUtils() {
        throw new IllegalStateException("do not instanciate");
    }

    public static double instantaneousTravelTime(Route route) {
        double instantaneousTravelTime = 0;
        for (RoadSegment roadSegment : route) {
            instantaneousTravelTime += roadSegment.instantaneousTravelTime();
        }
        return instantaneousTravelTime;
    }

    /**
     * Returns the number of vehicles on route.
     * 
     * @return the number of vehicles on given route.
     */
    public static int vehicleCount(Route route) {
        int vehicleCount = 0;
        for (final RoadSegment roadSegment : route) {
            vehicleCount += roadSegment.getVehicleCount();
        }
        return vehicleCount;
    }

    public static double totalVehicleTravelDistance(Route route) {
        double totalVehicleTravelDistance = 0.0;
        for (final RoadSegment roadSegment : route) {
            totalVehicleTravelDistance += roadSegment.totalVehicleTravelDistance();
            if (roadSegment.sink() != null) {
                totalVehicleTravelDistance += roadSegment.sink().totalVehicleTravelDistance();
            }
        }
        return totalVehicleTravelDistance;
    }

    public static double instantaneousFuelUsedLiters(Route route) {
        double instantaneousConsumption = 0;
        for (final RoadSegment roadSegment : route) {
            instantaneousConsumption += roadSegment.instantaneousConsumptionLitersPerSecond();
        }
        return instantaneousConsumption;
    }


}
