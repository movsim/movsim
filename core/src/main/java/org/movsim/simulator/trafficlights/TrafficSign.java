package org.movsim.simulator.trafficlights;

import org.movsim.simulator.roadnetwork.RoadSegment;

public interface TrafficSign {

    double position();

    RoadSegment roadSegment();

    // TODO lane specifics

}
