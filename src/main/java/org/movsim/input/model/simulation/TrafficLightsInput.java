package org.movsim.input.model.simulation;

import java.util.List;

public interface TrafficLightsInput {

    List<TrafficLightData> getTrafficLightData();
    
    int getnDtSample();
    
    boolean isWithLogging();
    
}
