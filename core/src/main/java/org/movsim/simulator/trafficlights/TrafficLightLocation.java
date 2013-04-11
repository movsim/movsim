package org.movsim.simulator.trafficlights;

import org.movsim.network.autogen.opendrive.OpenDRIVE.Controller;
import org.movsim.network.autogen.opendrive.OpenDRIVE.Road.Signals.Signal;

import com.google.common.base.Preconditions;

public class TrafficLightLocation {

    private final Signal signal;

    private final String controllerId;

    private TrafficLight trafficLight;

    private final double position;

    private final Controller controller;

    public TrafficLightLocation(Signal signal, Controller controller) {
        this.controller = Preconditions.checkNotNull(controller);
        Preconditions.checkNotNull(signal);
        Preconditions.checkArgument(!signal.getId().isEmpty(), "empty id!");
        Preconditions.checkArgument(!signal.getName().isEmpty(), "empty name!");
        Preconditions.checkArgument(signal.isSetId(), "id not set");
        Preconditions.checkArgument(signal.isSetName(), "name not set");
        Preconditions.checkArgument(signal.isSetS(), "s not set");
        this.signal = signal;
        this.position = signal.getS();
        this.controllerId = controller.getId();
    }

    /**
     * Returns the position on the road segment.
     * 
     * @return the position (m)
     */
    public double position() {
        return position;
    }

    /**
     * Returns the id. This id is defined in the infrastructure configuration file.
     * 
     * @return the label
     */
    public String signalId() {
        return signal.getId();
    }

    /**
     * Returns the name. This name is defined in the infrastructure configuration <signal> element and serves as key for the trafficlight
     * state.
     * 
     * @return the label
     */
    public String signalName() {
        return signal.getName();
    }

    public String controllerId() {
        return controllerId;
    }

    public TrafficLight getTrafficLight() {
        Preconditions.checkNotNull(trafficLight,
                "trafficLight not set. check the movsim input for referencing controller=" + controllerId());
        return trafficLight;
    }

    public void setTrafficLight(TrafficLight trafficLight) {
        Preconditions.checkArgument(this.trafficLight == null);
        this.trafficLight = trafficLight;
    }

    @Override
    public String toString() {
        return "TrafficLightLocation [controllerId = " + controllerId + ", signalId = " + signalId() + ", position = "
                + position + ", trafficLight=" + trafficLight + "]";
    }

    Controller getController() {
        return controller;
    }

}
