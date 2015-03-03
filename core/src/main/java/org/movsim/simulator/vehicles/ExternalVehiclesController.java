package org.movsim.simulator.vehicles;

import generated.ExternalVehicleType;
import generated.MovsimExternalVehicleControl;
import generated.SpeedDataType;
import generated.VehicleUserDataType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.movsim.simulator.roadnetwork.RoadSegment;

import com.google.common.base.Preconditions;

public class ExternalVehiclesController {

    private final Map<Vehicle, List<SpeedDataType>> controlledVehicles = new HashMap<>();

    // TODO info for roadSegment not yet stored
    
    private void createVehicles(MovsimExternalVehicleControl input) {
        for (ExternalVehicleType vehicleInput : input.getExternalVehicle()) {
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

    
    public void addRemoveVehicles(double simulationTime, RoadSegment roadSegment) {
        // testwise
        if (simulationTime <= 0) {
            for (Vehicle veh : controlledVehicles.keySet()) {
                roadSegment.addVehicle(veh);
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
