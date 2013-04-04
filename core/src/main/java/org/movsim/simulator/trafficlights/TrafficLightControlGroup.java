package org.movsim.simulator.trafficlights;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.movsim.autogen.ControllerGroup;
import org.movsim.autogen.Phase;
import org.movsim.autogen.TrafficLightCondition;
import org.movsim.autogen.TrafficLightState;
import org.movsim.simulator.SimulationTimeStep;
import org.movsim.simulator.roadnetwork.LaneSegment;
import org.movsim.simulator.vehicles.Vehicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

class TrafficLightControlGroup implements SimulationTimeStep, TriggerCallback {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(TrafficLightControlGroup.class);

    private final List<Phase> phases;

    private int currentPhaseIndex = 0;

    private double currentPhaseDuration;

    private final double conditionRange;

    private final Map<String, TrafficLight> trafficLights = new HashMap<>();

    TrafficLightControlGroup(ControllerGroup controllerGroup) {
        Preconditions.checkNotNull(controllerGroup);
        this.conditionRange = controllerGroup.getRange();
        phases = controllerGroup.getPhase();
        createTrafficlights();
    }

    private void createTrafficlights() {
        for (Phase phase : phases) {
            for(TrafficLightState trafficlightState : phase.getTrafficLightState()){
                String id = trafficlightState.getId();
                if (!trafficLights.containsKey(id)) {
                    trafficLights.put(id, new TrafficLight(id, this));
                }
                trafficLights.get(id).addPossibleState(trafficlightState.getStatus());
            }
        }
    }

    @Override
    public void timeStep(double dt, double simulationTime, long iterationCount) {
        currentPhaseDuration += dt;
        determinePhase();
        updateTrafficLights();
        if (recordDataCallback != null) {
            recordDataCallback.recordData(simulationTime, iterationCount, trafficLights.values());
        }
    }

    @Override
    public void nextPhase() {
        LOG.debug("triggered next phase for controller group.");
        currentPhaseDuration = 0; // reset
        setNextPhaseIndex();
    }

    private void determinePhase() {
        Phase phase = phases.get(currentPhaseIndex);
        // first check if all "clear" conditions are fullfilled.
        // then check fixed-time schedule for next phase
        // and last check trigger condition for overriding fixed-time scheduler
        if (isClearConditionsFullfilled(phase)
                && (currentPhaseDuration > phase.getDuration() || isTriggerConditionFullfilled(phase))) {
            nextPhase();
        }
    }

    private boolean isClearConditionsFullfilled(Phase phase) {
        for (TrafficLightState state : phase.getTrafficLightState()) {
            if (state.getCondition() == TrafficLightCondition.CLEAR) {
                if (vehicleIsInFrontOfLightAndDriving(trafficLights.get(state.getId()))) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isTriggerConditionFullfilled(Phase phase) {
        for (TrafficLightState state : phase.getTrafficLightState()) {
            if (state.getCondition() == TrafficLightCondition.REQUEST) {
                if (vehicleIsInFrontOfLight(trafficLights.get(state.getId()))) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean vehicleIsInFrontOfLight(TrafficLight trafficLight) {
        for (LaneSegment laneSegment : trafficLight.roadSegment().laneSegments()) {
            Vehicle vehicle = laneSegment.rearVehicle(trafficLight.position());
            if (vehicle != null && (trafficLight.position() - vehicle.getFrontPosition() < conditionRange)) {
                LOG.debug("condition check: vehicle is in front of trafficlight: vehPos={}, trafficlightPos={}",
                        vehicle.getFrontPosition(), trafficLight.position());
                return true;
            }
        }
        return false;
    }

    private boolean vehicleIsInFrontOfLightAndDriving(TrafficLight trafficLight) {
        for (LaneSegment laneSegment : trafficLight.roadSegment().laneSegments()) {
            Vehicle vehicle = laneSegment.rearVehicle(trafficLight.position());
            if (vehicle != null && (trafficLight.position() - vehicle.getFrontPosition() < conditionRange)
                    && vehicle.getSpeed() > 0) {
                LOG.debug("condition check: vehicle is in front of trafficlight: vehPos={}, trafficlightPos={}",
                        vehicle.getFrontPosition(), trafficLight.position());
                return true;
            }
        }
        return false;
    }

    private void updateTrafficLights() {
        Phase actualPhase = phases.get(currentPhaseIndex);
        for(TrafficLightState trafficLightState : actualPhase.getTrafficLightState()){
            trafficLights.get(trafficLightState.getId()).setState(trafficLightState.getStatus());
        }
    }

    Iterable<TrafficLight> trafficLights() {
        return ImmutableList.copyOf(trafficLights.values().iterator());
    }

    private void setNextPhaseIndex() {
        if (currentPhaseIndex == phases.size() - 1) {
            currentPhaseIndex = 0;
            return;
        }
        currentPhaseIndex++;
    }

    public interface RecordDataCallback {
        /**
         * Callback to allow the application to process or record the traffic light data.
         * 
         * @param simulationTime
         *            the current logical time in the simulation
         * @param iterationCount
         * @param trafficLights
         */
        public void recordData(double simulationTime, long iterationCount, Iterable<TrafficLight> trafficLights);
    }

    private RecordDataCallback recordDataCallback;

    public void setRecorder(RecordDataCallback recordDataCallback) {
        this.recordDataCallback = recordDataCallback;
    }

}
