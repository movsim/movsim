package org.movsim.simulator.trafficlights;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.movsim.autogen.ControllerGroup;
import org.movsim.autogen.Phase;
import org.movsim.autogen.TrafficLightState;
import org.movsim.simulator.SimulationTimeStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

class AbstractTrafficLightController implements SimulationTimeStep, TriggerCallback {
    /** The Constant LOG. */
    static final Logger LOG = LoggerFactory.getLogger(AbstractTrafficLightController.class);

    /** mapping from the 'physical' controller.control.type to the 'logical' trafficlight */
    final Map<String, TrafficLight> trafficLights;

    int currentPhaseIndex;

    final String groupId;
    
    final List<Phase> phases;

    String firstSignalId; // needed for logging

    AbstractTrafficLightController(ControllerGroup controllerGroup) {
        trafficLights = new HashMap<>();
        currentPhaseIndex = 0; // init phase
        Preconditions.checkNotNull(controllerGroup);
        this.groupId = controllerGroup.getId();
        this.phases = ImmutableList.copyOf(controllerGroup.getPhase()); // deep copy
    }

    void add(TrafficLight trafficLight) {
        Preconditions.checkNotNull(trafficLight);
        Preconditions.checkArgument(!trafficLights.containsKey(trafficLight.signalType()), "trafficLight="
                + trafficLight + " already added.");
        if (trafficLights.isEmpty()) {
            firstSignalId = trafficLight.getController().getControl().get(0).getSignalId();
        }
        trafficLight.setTriggerCallback(this);
        trafficLights.put(trafficLight.signalType(), trafficLight);

        // determine possible states
        for (Phase phase : phases) {
            for (TrafficLightState trafficlightState : phase.getTrafficLightState()) {
                String type = Preconditions.checkNotNull(trafficlightState.getType());
                if (trafficLight.signalType().equals(type)) {
                    trafficLight.addPossibleState(trafficlightState.getStatus());
                }
            }
        }
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

    String firstSignalId() {
        return Preconditions.checkNotNull(firstSignalId);
    }

    void checkIfAllSignalTypesAdded() throws IllegalArgumentException {
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

    
    public Iterable<TrafficLight> trafficLights() {
        return ImmutableList.copyOf(trafficLights.values().iterator());
    }

    @Override
    public void timeStep(double dt, double simulationTime, long iterationCount) {
        if (recordDataCallback != null) {
            recordDataCallback.recordData(simulationTime, iterationCount, trafficLights.values());
        }
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
