package org.movsim.simulator.roadnetwork;

import java.util.Iterator;

public class RoadNetworkState {

    private final RoadNetwork roadNetwork;

    public RoadNetworkState(RoadNetwork roadNetwork) {
        this.roadNetwork = roadNetwork;
    }

    /**
     * Returns the total travel time of all vehicles on this road network, including those that have exited.
     * 
     * @return the total vehicle travel time
     */
    public double totalVehicleTravelTime() {
        double totalVehicleTravelTime = 0.0;
        Iterator<RoadSegment> iterator = roadNetwork.iterator();
        while (iterator.hasNext()) {
            RoadSegment roadSegment = iterator.next();
            totalVehicleTravelTime += roadSegment.totalVehicleTravelTime();
            if (roadSegment.sink() != null) {
                totalVehicleTravelTime += roadSegment.sink().totalVehicleTravelTime();
            }
        }
        return totalVehicleTravelTime;
    }

    public double totalVehicleTravelTime(Route route) {
        double totalVehicleTravelTime = 0.0;
        for (final RoadSegment roadSegment : route) {
            totalVehicleTravelTime += roadSegment.totalVehicleTravelTime();
            if (roadSegment.sink() != null) {
                totalVehicleTravelTime += roadSegment.sink().totalVehicleTravelTime();
            }
        }
        return totalVehicleTravelTime;
    }

    public double instantaneousTravelTime(Route route) {
        double instantaneousTravelTime = 0;
        for (final RoadSegment roadSegment : route) {
            instantaneousTravelTime += 1; // TODO
        }
        return instantaneousTravelTime;
    }

    /**
     * Returns the total travel distance of all vehicles on this road network, including those that have exited.
     * 
     * @return the total vehicle travel distance
     */
    public double totalVehicleTravelDistance() {
        double totalVehicleTravelDistance = 0.0;
        Iterator<RoadSegment> iterator = roadNetwork.iterator();
        while (iterator.hasNext()) {
            RoadSegment roadSegment = iterator.next();
            totalVehicleTravelDistance += roadSegment.totalVehicleTravelDistance();
            if (roadSegment.sink() != null) {
                totalVehicleTravelDistance += roadSegment.sink().totalVehicleTravelDistance();
            }
        }
        return totalVehicleTravelDistance;
    }

    public double totalVehicleTravelDistance(Route route) {
        double totalVehicleTravelDistance = 0.0;
        for (final RoadSegment roadSegment : route) {
            totalVehicleTravelDistance += roadSegment.totalVehicleTravelDistance();
            if (roadSegment.sink() != null) {
                totalVehicleTravelDistance += roadSegment.sink().totalVehicleTravelDistance();
            }
        }
        return totalVehicleTravelDistance;
    }

    /**
     * Returns the total fuel used by all vehicles on this road network, including those that have exited.
     * 
     * @return the total vehicle fuel used
     */
    public double totalVehicleFuelUsedLiters() {
        double totalVehicleFuelUsedLiters = 0.0;
        Iterator<RoadSegment> iterator = roadNetwork.iterator();
        while (iterator.hasNext()) {
            RoadSegment roadSegment = iterator.next();
            totalVehicleFuelUsedLiters += roadSegment.totalVehicleFuelUsedLiters();
            if (roadSegment.sink() != null) {
                totalVehicleFuelUsedLiters += roadSegment.sink().totalFuelUsedLiters();
            }
        }
        return totalVehicleFuelUsedLiters;
    }

    public double totalVehicleElectricEnergyUsed() {
        double totalVehicleElectricEnergyUsed = 0.0;
        Iterator<RoadSegment> iterator = roadNetwork.iterator();
        while (iterator.hasNext()) {
            RoadSegment roadSegment = iterator.next();
            totalVehicleElectricEnergyUsed += roadSegment.totalVehicleElectricEnergyUsed();
            if (roadSegment.sink() != null) {
                totalVehicleElectricEnergyUsed += roadSegment.sink().totalVehicleElectricEnergyUsed();
            }
        }
        return totalVehicleElectricEnergyUsed;
    }

    public double instantaneousFuelUsedLiters(Route route) {
        double instantaneousConsumption = 0;
        for (final RoadSegment roadSegment : route) {
            instantaneousConsumption += 1; // TODO
        }
        return instantaneousConsumption;
    }

}
