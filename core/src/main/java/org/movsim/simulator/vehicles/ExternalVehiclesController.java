package org.movsim.simulator.vehicles;

import generated.ExternalVehicleType;
import generated.MovsimExternalVehicleControl;
import generated.SpeedDataType;
import generated.VehicleUserDataType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.movsim.simulator.roadnetwork.RoadNetwork;
import org.movsim.simulator.roadnetwork.RoadSegment;
import org.movsim.utilities.TimeUtilities;

import com.google.common.base.Preconditions;
import com.google.common.collect.TreeMultimap;

public class ExternalVehiclesController {

    /** time-sorted map of vehicle input data */
    private TreeMultimap<Double, ExternalVehicleType> vehiclesToAdd;
    
    /** time-sorted map of vehicles to remove */
    private TreeMultimap<Long, Vehicle> vehiclesToRemove;
    
    
    private final Map<Vehicle, List<SpeedDataType>> controlledVehicles = new HashMap<>();

    // TODO info for roadSegment not yet stored
    
    private void createVehicles(MovsimExternalVehicleControl input) {
        for (ExternalVehicleType vehicleInput : input.getExternalVehicle()) {
            Preconditions.checkArgument(!vehicleInput.getSpeedData().isEmpty(), "external vehicle needs at least one speed-data entry");
            String time = vehicleInput.getSpeedData().get(0).getTime();
            double timestamp = TimeUtilities.convertToSeconds(time, input.getTimeFormat());
            // TODO timestamp key for sorted mapping to vehicles 
            
            double initialSpeed = vehicleInput.getSpeedData().get(0).getSpeed();
            Vehicle vehicle = new Vehicle(vehicleInput.getPosition(), initialSpeed, vehicleInput.getLane(),
                    vehicleInput.getLength(), vehicleInput.getWidth());
            vehicle.setType(Vehicle.Type.EXTERNAL_CONTROL);

            for (VehicleUserDataType userData : vehicleInput.getVehicleUserData()) {
                vehicle.getUserData().put(userData.getKey(), userData.getValue());
            }

            controlledVehicles.put(vehicle, vehicleInput.getSpeedData()); // time series data
            
        }
    }

    public void setInput(MovsimExternalVehicleControl input) {
        Preconditions.checkNotNull(input);
        createVehicles(input);
    }

    
    public void addRemoveVehicles(double simulationTime, RoadNetwork roadNetwork) {
        if (simulationTime <= 0) {
            for (Vehicle veh : controlledVehicles.keySet()) {
                // just for testing  
                roadNetwork.iterator().next().addVehicle(veh);
            }
        }
    }

    public void setVehicleSpeeds(double simulationTime, RoadSegment roadSegment) {
        //testwise
        for (Vehicle veh : controlledVehicles.keySet()) {
            veh.setSpeed(5);
        }
        
    }

    // TODO time series mit interpolation, analog zu InflowTimeSeries ... etwas generisches!
}
