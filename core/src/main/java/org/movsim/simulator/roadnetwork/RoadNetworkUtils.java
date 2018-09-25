package org.movsim.simulator.roadnetwork;

import org.movsim.simulator.roadnetwork.routing.Route;

public final class RoadNetworkUtils {

    private RoadNetworkUtils() {
        throw new IllegalStateException("do not instanciate");
    }

    public enum TravelTimeType {
        GRID,
        MEAN
    }

    public static double instantaneousTravelTime(Route route, TravelTimeType type) {
        final double gridLength = 200;
        switch (type) {
        case GRID:
            return instantaneousTravelTimeOnGrid(route, gridLength);
        case MEAN:
            return instantaneousTravelTimeFromMeanSpeed(route);
        default:
            return 0;
        }
    }

    /**
     * @return the estimated instanteous travel time evaluated on grid-segments.
     */
    public static double instantaneousTravelTimeOnGrid(Route route, double gridLength) {
        double instantaneousTravelTime = 0;
        for (RoadSegment roadSegment : route) {
            instantaneousTravelTime += roadSegment.instantaneousTravelTimeOnGrid(gridLength);
        }
        return instantaneousTravelTime;
    }

    /**
     * @return the estimated instanteous travel time based on mean speeds from vehicles.
     */
    public static double instantaneousTravelTimeFromMeanSpeed(Route route) {
        double instantaneousTravelTimeFromMeanSpeed = 0;
        for (RoadSegment roadSegment : route) {
            instantaneousTravelTimeFromMeanSpeed += roadSegment.instantaneousTravelTimeFromMeanSpeed();
        }
        return instantaneousTravelTimeFromMeanSpeed;
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
