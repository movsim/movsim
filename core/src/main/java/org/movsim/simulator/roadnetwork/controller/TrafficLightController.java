package org.movsim.simulator.roadnetwork.controller;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.movsim.autogen.ControllerGroup;
import org.movsim.autogen.Phase;
import org.movsim.autogen.TrafficLightState;
import org.movsim.autogen.TrafficLightStatus;
import org.movsim.simulator.SimulationTimeStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public abstract class TrafficLightController implements SimulationTimeStep, TriggerCallback, Iterable<TrafficLight> {

    protected static final Logger LOG = LoggerFactory.getLogger(TrafficLightController.class);

    /**
     * mapping from the 'physical' controller.control.type to the 'logical' trafficlight
     */
    final Map<String, TrafficLight> trafficLights;

    int currentPhaseIndex;

    final String groupId;

    final List<Phase> phases;

    String firstSignalId; // needed for logging

    private TrafficLightRecordDataCallback recordDataCallback;

    TrafficLightController(ControllerGroup controllerGroup) {
        trafficLights = new HashMap<>();
        currentPhaseIndex = 0; // init phase
        Preconditions.checkNotNull(controllerGroup);
        this.groupId = controllerGroup.getId();
        this.phases = ImmutableList.copyOf(controllerGroup.getPhase()); // deep copy
    }

    void add(TrafficLight trafficLight) {
        Preconditions.checkNotNull(trafficLight);
        Preconditions.checkArgument(!trafficLights.containsKey(trafficLight.signalType()),
                "trafficLight=" + trafficLight + " already added.");
        if (trafficLights.isEmpty()) {
            firstSignalId = trafficLight.getController().getControl().get(0).getSignalId();
        }
        trafficLight.setTriggerCallback(this);
        trafficLights.put(trafficLight.signalType(), trafficLight);

        trafficLight.setState(getInitTrafficLightState(trafficLight.signalType()));
        // determine possible states
        // for (Phase phase : phases) {
        // for (TrafficLightState trafficlightState : phase.getTrafficLightState()) {
        // String type = Preconditions.checkNotNull(trafficlightState.getType());
        // if (trafficLight.signalType().equals(type)) {
        // trafficLight.addPossibleState(trafficlightState.getStatus());
        // }
        // }
        // }
    }

    private TrafficLightStatus getInitTrafficLightState(String signalType) {
        for (Phase phase : phases) {
            for (TrafficLightState trafficlightState : phase.getTrafficLightState()) {
                String type = Preconditions.checkNotNull(trafficlightState.getType());
                if (signalType.equals(type)) {
                    return trafficlightState.getStatus();
                }
            }
        }
        throw new IllegalStateException("could bot find signal type=" + signalType + " in controller=" + groupId);
    }

    void setNextPhaseIndex() {
        if (currentPhaseIndex == phases.size() - 1) {
            currentPhaseIndex = 0;
            return;
        }
        currentPhaseIndex++;
    }

    public String groupId() {
        return groupId;
    }

    // for logging
    public String firstSignalId() {
        return Preconditions.checkNotNull(firstSignalId);
    }

    void checkIfAllSignalTypesAdded() {
        for (Phase phase : phases) {
            for (TrafficLightState state : phase.getTrafficLightState()) {
                if (!trafficLights.containsKey(state.getType())) {
                    throw new IllegalArgumentException("signal type in controller" + groupId + " not been registered");
                }
            }
        }
    }

    @Override
    public void nextPhase() {
        LOG.debug("triggered next phase for controller group.");
        setNextPhaseIndex();
    }

    @Override
    public final Iterator<TrafficLight> iterator() {
        return trafficLights.values().iterator();
    }

    @Override
    public void timeStep(double dt, double simulationTime, long iterationCount) {
        if (recordDataCallback != null) {
            recordDataCallback.recordData(simulationTime, iterationCount, trafficLights.values());
        }
    }

    public final void setRecorder(TrafficLightRecordDataCallback recordDataCallback) {
        this.recordDataCallback = Preconditions.checkNotNull(recordDataCallback);
    }

}
