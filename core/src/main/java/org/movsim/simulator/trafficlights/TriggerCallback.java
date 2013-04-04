package org.movsim.simulator.trafficlights;

public interface TriggerCallback {

    /**
     * Triggers (interactively) the next phase of the controller group.
     */
    void nextPhase();

}
