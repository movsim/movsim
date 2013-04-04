package org.movsim.simulator.trafficlights;

import org.movsim.network.autogen.opendrive.OpenDRIVE.Road.Signals.Signal;

import com.google.common.base.Preconditions;

public class TrafficLightLocation {

    private final Signal signal;

    private TrafficLight trafficLight;

    public TrafficLightLocation(Signal signal) {
        Preconditions.checkNotNull(signal);
        Preconditions.checkArgument(!signal.getId().isEmpty());
        Preconditions.checkArgument(signal.isSetId());
        Preconditions.checkArgument(signal.isSetS());
        this.signal = signal;
    }

    /**
     * Returns the position on the road segment.
     * 
     * @return the position (m)
     */
    public double position() {
        return signal.getS();
    }

    /**
     * Returns the id. This id is defined in the infrastructure configuration file.
     * 
     * @return the label
     */
    public String id() {
        return signal.getId();
    }

    public TrafficLight getTrafficLight() {
        Preconditions.checkNotNull(trafficLight);
        return trafficLight;
    }

    public void setTrafficLight(TrafficLight trafficLight) {
        Preconditions.checkArgument(this.trafficLight == null);
        this.trafficLight = trafficLight;
    }

}
