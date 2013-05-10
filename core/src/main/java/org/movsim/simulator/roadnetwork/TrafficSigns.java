package org.movsim.simulator.roadnetwork;

import org.movsim.simulator.roadnetwork.TrafficSign.TrafficSignType;

public interface TrafficSigns {

    public <T extends TrafficSign> T getNextTrafficSign(TrafficSignType type, double position, int lane);

    public void add(TrafficSign trafficSign);

    public boolean hasTrafficSign(TrafficSignType type);
}
