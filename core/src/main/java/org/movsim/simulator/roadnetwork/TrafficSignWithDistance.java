package org.movsim.simulator.roadnetwork;

public interface TrafficSignWithDistance {

    <T extends TrafficSign> T trafficSign();

    double distance();

}
