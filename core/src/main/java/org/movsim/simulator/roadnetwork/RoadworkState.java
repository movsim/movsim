package org.movsim.simulator.roadnetwork;

import java.util.Iterator;


public class RoadworkState {

    private final RoadNetwork roadNetwork;
    
    public RoadworkState(RoadNetwork roadNetwork){
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
        while(iterator.hasNext()) {
            RoadSegment roadSegment = iterator.next(); 
            totalVehicleTravelTime += roadSegment.totalVehicleTravelTime();
            if (roadSegment.sink() != null) {
                totalVehicleTravelTime += roadSegment.sink().totalVehicleTravelTime();
            }
        }
        return totalVehicleTravelTime;
    }

    /**
     * Returns the total travel distance of all vehicles on this road network, including those that have exited.
     * 
     * @return the total vehicle travel distance
     */
    public double totalVehicleTravelDistance() {
        double totalVehicleTravelDistance = 0.0;
        Iterator<RoadSegment> iterator = roadNetwork.iterator();
        while(iterator.hasNext()) {
            RoadSegment roadSegment = iterator.next(); 
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
        while(iterator.hasNext()) {
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
        while(iterator.hasNext()) {
            RoadSegment roadSegment = iterator.next(); 
            totalVehicleElectricEnergyUsed += roadSegment.totalVehicleElectricEnergyUsed();
            if (roadSegment.sink() != null) {
                totalVehicleElectricEnergyUsed += roadSegment.sink().totalVehicleElectricEnergyUsed();
            }
        }
        return totalVehicleElectricEnergyUsed;
    }

}
