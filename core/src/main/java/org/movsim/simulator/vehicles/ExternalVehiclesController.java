package org.movsim.simulator.vehicles;

import com.google.common.base.Preconditions;
import org.movsim.scenario.vehicle.autogen.ExternalVehicleType;
import org.movsim.scenario.vehicle.autogen.MovsimExternalVehicleControl;
import org.movsim.scenario.vehicle.autogen.SpeedDataType;
import org.movsim.scenario.vehicle.autogen.VehicleUserDataType;
import org.movsim.simulator.roadnetwork.LaneSegment;
import org.movsim.simulator.roadnetwork.RoadNetwork;
import org.movsim.simulator.roadnetwork.RoadSegment;
import org.movsim.utilities.LinearInterpolatedFunction;
import org.movsim.utilities.TimeUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.Map.Entry;

public class ExternalVehiclesController {

    private static final Logger LOG = LoggerFactory.getLogger(ExternalVehiclesController.class);

    /**
     * time-sorted map of external vehicle input data
     */
    private final SortedMap<Double, List<ExternalVehicleType>> externalVehicleInputsToAdd = new TreeMap<>();

    /**
     * time-sorted map of vehicles to remove
     */
    private final SortedMap<Double, List<Vehicle>> externalVehiclesToRemove = new TreeMap<>();

    private final Map<Vehicle, LinearInterpolatedFunction> controlledVehicles = new HashMap<>();

    private String timeFormat;

    public void setInput(MovsimExternalVehicleControl input) {
        Preconditions.checkNotNull(input);
        this.timeFormat = input.getTimeFormat();
        createExternalVehicleData(input);
    }

    /**
     * sets the speeds of externally controlled vehicles in whole road network
     */
    public void setSpeeds(double simulationTime) {
        for (Entry<Vehicle, LinearInterpolatedFunction> entry : controlledVehicles.entrySet()) {
            double currentSpeed = entry.getValue().value(simulationTime);
            Vehicle vehicle = entry.getKey();
            vehicle.setSpeed(currentSpeed);
        }
    }

    /**
     * adds and removes externally controlled vehicles as configured in the simulation input.
     *
     * @param simulationTime
     * @param roadNetwork
     */
    public void addAndRemoveVehicles(double simulationTime, RoadNetwork roadNetwork) {
        addVehiclesToRoadNetwork(simulationTime, roadNetwork);
        removeVehiclesFromRoadNetwork(simulationTime, roadNetwork);
    }

    private void createExternalVehicleData(MovsimExternalVehicleControl input) {
        for (ExternalVehicleType externalVehicleData : input.getExternalVehicle()) {
            Preconditions.checkArgument(!externalVehicleData.getSpeedData().isEmpty(),
                    "external vehicle needs at least one (time, speed) data entry");
            // no check if (time, speed) input data is sorted in time
            String entryTime = externalVehicleData.getSpeedData().get(0).getTime();
            double entryTimestamp = TimeUtilities.convertToSeconds(entryTime, timeFormat);
            addVehicleInput(entryTimestamp, externalVehicleData);
        }
    }

    private void addVehiclesToRoadNetwork(double simulationTime, RoadNetwork roadNetwork) {
        while (!externalVehicleInputsToAdd.isEmpty() && externalVehicleInputsToAdd.firstKey() <= simulationTime) {
            Double firstKey = externalVehicleInputsToAdd.firstKey();
            List<ExternalVehicleType> externalVehicles = externalVehicleInputsToAdd.remove(firstKey);
            addVehiclesToRoadSegments(externalVehicles, roadNetwork);
            LOG.debug("added {} external vehicles to roadNetwork, removed entries for time={}", externalVehicles.size(),
                    firstKey);
        }
    }

    private void removeVehiclesFromRoadNetwork(double simulationTime, RoadNetwork roadNetwork) {
        while (!externalVehiclesToRemove.isEmpty() && simulationTime >= externalVehiclesToRemove.firstKey()) {
            Double firstKey = externalVehiclesToRemove.firstKey();
            List<Vehicle> externalVehicles = externalVehiclesToRemove.remove(firstKey);
            removeVehiclesFromRoadSegments(externalVehicles, roadNetwork);
        }
    }

    private void removeVehiclesFromRoadSegments(List<Vehicle> externalVehicles, RoadNetwork roadNetwork) {
        for (Vehicle vehicle : externalVehicles) {
            int roadSegmentId = vehicle.roadSegmentId();
            RoadSegment roadSegment = roadNetwork.findById(roadSegmentId);
            LaneSegment laneSegment = roadSegment.laneSegment(vehicle.lane());
            laneSegment.removeVehicle(vehicle);
            LOG.info("removed externally controlled vehicle={} from roadSegment={}", vehicle, roadSegment);
        }
    }

    private boolean addVehicleInput(double timestamp, ExternalVehicleType externalVehicleInput) {
        List<ExternalVehicleType> externalVehicles = externalVehicleInputsToAdd
                .computeIfAbsent(timestamp, it -> new ArrayList<>());
        return externalVehicles.add(externalVehicleInput);
    }

    private boolean addVehicleToRemoveMap(double timestamp, Vehicle vehicle) {
        List<Vehicle> vehiclesToRemove = externalVehiclesToRemove.computeIfAbsent(timestamp, it -> new ArrayList<>());
        return vehiclesToRemove.add(vehicle);
    }

    private void addVehiclesToRoadSegments(List<ExternalVehicleType> vehicleInputs, RoadNetwork roadNetwork) {
        for (ExternalVehicleType vehicleInput : vehicleInputs) {
            Vehicle vehicle = createVehicle(vehicleInput);
            String roadId = vehicleInput.getRoadId();
            RoadSegment roadSegment = roadNetwork.findByUserId(roadId);
            Preconditions
                    .checkNotNull(roadSegment, "cannot find roadSegment with id=" + roadId + " for external vehicle");
            roadSegment.addVehicle(vehicle);
            LinearInterpolatedFunction speedProfile = createSpeedProfile(vehicleInput.getSpeedData());
            controlledVehicles.put(vehicle, speedProfile);
            LOG.info("added externally controlled vehicle={} to roadSegment={}", vehicle, roadSegment);

            // and add vehicle to removal container
            int size = vehicleInput.getSpeedData().size();
            String exitTime = vehicleInput.getSpeedData().get(size - 1).getTime();
            double exitTimestamp = TimeUtilities.convertToSeconds(exitTime, timeFormat);
            addVehicleToRemoveMap(exitTimestamp, vehicle);
            LOG.info("external vehicle={} will be removed at timestamp={}", vehicle, exitTimestamp);
        }
    }

    private LinearInterpolatedFunction createSpeedProfile(List<SpeedDataType> speedData) {
        int size = speedData.size();
        double[] times = new double[size];
        double[] speeds = new double[size];
        for (int i = 0; i < size; i++) {
            SpeedDataType dataPoint = speedData.get(i);
            times[i] = TimeUtilities.convertToSeconds(dataPoint.getTime(), timeFormat);
            speeds[i] = dataPoint.getSpeed();
        }

        return new LinearInterpolatedFunction(times, speeds);
    }

    private Vehicle createVehicle(ExternalVehicleType data) {
        double initialSpeed = data.getSpeedData().get(0).getSpeed();
        Vehicle vehicle = new Vehicle(data.getPosition(), initialSpeed, data.getLane(), data.getLength(),
                data.getWidth());
        vehicle.setType(Vehicle.Type.EXTERNAL_CONTROL);
        for (VehicleUserDataType userData : data.getVehicleUserData()) {
            vehicle.getUserData().put(userData.getKey(), userData.getValue());
        }
        return vehicle;
    }

}
