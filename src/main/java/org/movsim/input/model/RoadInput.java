package org.movsim.input.model;

import java.util.List;

import org.movsim.input.model.simulation.FlowConservingBottleneckDataPoint;
import org.movsim.input.model.simulation.HeterogeneityInputData;
import org.movsim.input.model.simulation.ICMacroData;
import org.movsim.input.model.simulation.ICMicroData;
import org.movsim.input.model.simulation.RampData;
import org.movsim.input.model.simulation.SimpleRampData;
import org.movsim.input.model.simulation.SpeedLimitDataPoint;
import org.movsim.input.model.simulation.TrafficLightData;
import org.movsim.input.model.simulation.UpstreamBoundaryData;

public interface RoadInput {
    double getRoadLength();
    
    int getLanes();
    
    int getId();

    boolean isWithWriteFundamentalDiagrams();
    
    List<HeterogeneityInputData> getHeterogeneityInputData();

    List<ICMacroData> getIcMacroData();

    List<ICMicroData> getIcMicroData();

    UpstreamBoundaryData getUpstreamBoundaryData();

    List<FlowConservingBottleneckDataPoint> getFlowConsBottleneckInputData();
    
    List<SpeedLimitDataPoint> getSpeedLimitInputData();

    List<RampData> getRamps();
    
    List<SimpleRampData> getSimpleRamps();

    List<TrafficLightData> getTrafficLightData();

    

}
