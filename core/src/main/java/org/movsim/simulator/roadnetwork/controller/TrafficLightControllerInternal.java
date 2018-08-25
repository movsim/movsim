package org.movsim.simulator.roadnetwork.controller;

import org.movsim.autogen.ControllerGroup;
import org.movsim.autogen.Phase;
import org.movsim.autogen.TrafficLightCondition;
import org.movsim.autogen.TrafficLightState;
import org.movsim.simulator.roadnetwork.LaneSegment;
import org.movsim.simulator.vehicles.Vehicle;

class TrafficLightControllerInternal extends TrafficLightController {

    private double currentPhaseDuration;

    private final double conditionRange;

    TrafficLightControllerInternal(ControllerGroup controllerGroup) {
        super(controllerGroup);
        this.conditionRange = controllerGroup.getRange();
    }

    @Override
    public void timeStep(double dt, double simulationTime, long iterationCount) {
        currentPhaseDuration += dt;
        determinePhase();
        updateTrafficLights();
        super.timeStep(dt, simulationTime, iterationCount);
    }

    @Override
    public void nextPhase() {
        super.nextPhase();
        currentPhaseDuration = 0; // reset
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
                if (vehicleIsInFrontOfLightAndDriving(trafficLights.get(state.getType()))) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isTriggerConditionFullfilled(Phase phase) {
        for (TrafficLightState state : phase.getTrafficLightState()) {
            if (state.getCondition() == TrafficLightCondition.REQUEST) {
                if (vehicleIsInFrontOfLight(trafficLights.get(state.getType()))) {
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
        for (TrafficLightState trafficLightState : actualPhase.getTrafficLightState()) {
            trafficLights.get(trafficLightState.getType()).setState(trafficLightState.getStatus());
        }
    }

}
