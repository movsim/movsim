package org.movsim.simulator.trafficlights;

import org.movsim.network.autogen.opendrive.OpenDRIVE.Road.Signals.Signal;

import com.google.common.base.Preconditions;

public class TrafficLightLocation {

    private final Signal signal;

    public TrafficLightLocation(Signal signal) {
        Preconditions.checkNotNull(signal);
        Preconditions.checkArgument(!signal.getId().isEmpty());
        this.signal = signal;
    }

    /**
     * @return the position
     */
    public double getPosition() {
        return signal.getS();
    }

    /**
     * @return the label
     */
    public String getId() {
        return signal.getId();
    }


}
