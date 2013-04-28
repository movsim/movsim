package org.movsim.simulator.trafficlights;

import org.movsim.network.autogen.opendrive.OpenDRIVE;
import org.movsim.network.autogen.opendrive.OpenDRIVE.Controller;
import org.movsim.network.autogen.opendrive.OpenDRIVE.Road.Signals.Signal;

import com.google.common.base.Preconditions;

public class TrafficLightLocation {

    private final Signal signal;
    private final Controller controller;
    private final double position;
    private final String signalType;

    private TrafficLight trafficLight;

    public TrafficLightLocation(Signal signal, Controller controller) {
        this.controller = Preconditions.checkNotNull(controller);
        this.signal = Preconditions.checkNotNull(signal);
        Preconditions.checkArgument(signal.isSetId(), "id not set");
        Preconditions.checkArgument(!signal.getId().isEmpty(), "empty id!");
        Preconditions.checkArgument(signal.isSetS(), "signal.s not set");
        this.position = signal.getS();
        this.signalType = Preconditions.checkNotNull(checkTypesAndExtractSignalType());
    }

    private String checkTypesAndExtractSignalType() {
        String signalType = null;
        for (OpenDRIVE.Controller.Control control : controller.getControl()) {
            if (!control.isSetType()) {
                throw new IllegalArgumentException("controller.control.type must be set in xodr for signal="
                        + signalId());
            }
            if (control.getSignalId().equals(signalId())) {
                signalType = control.getType();
            }
        }
        return signalType;
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
     * Returns the signal type assigned in the controller.control xodr input. This type links from a 'physical' signal to a 'logical'
     * representation of the trafficlight state.
     * 
     * @return the signal-type id
     */
    public String signalType() {
        return signalType;
    }

    public String controllerId() {
        return controller.getId();
    }

    Controller getController() {
        return controller;
    }

    public TrafficLight getTrafficLight() {
        Preconditions.checkNotNull(trafficLight,
                "trafficLight not set. check the movsim input for referencing controller=" + controllerId());
        return trafficLight;
    }

    public void setTrafficLight(TrafficLight trafficLight) {
        Preconditions.checkArgument(this.trafficLight == null, "trafficLight already set:" + this.toString());
        this.trafficLight = Preconditions.checkNotNull(trafficLight);
    }

    @Override
    public String toString() {
        return "TrafficLightLocation [controllerId = " + controllerId() + ", signalId = " + signalId()
                + ", position = " + position + ", trafficLight=" + trafficLight + "]";
    }

}
