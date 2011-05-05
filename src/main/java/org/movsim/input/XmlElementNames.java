package org.movsim.input;

public interface XmlElementNames {

    
    public final String Root = "SCENARIO";

    public final String DriverVehicleUnits = "VEHICLES";
    
    public final String Output = "OUTPUT";
    public final String OutputFloatingCarData = "FLOATING_CAR_DATA";
    public final String OutputFloatingMacro = "SPATIOTEMPORAL";
    public final String OutputFloatingDetectors = "DETECTORS";
    public final String OutputFloatingTrafficlightRecorder = "TRAFFIC_LIGHT_RECORDER";
    public final String OutputFloatingTrajectories = "TRAJECTORIES";
    
    public final String Simulation = "SIMULATION";
    public final String Road = "ROAD";
    public final String RoadTrafficLight = "TRAFFIC_LIGHT"; 
    public final String RoadTrafficLights = "TRAFFIC_LIGHTS"; 
    public final String RoadTrafficComposition = "TRAFFIC_COMPOSITION"; 
    public final String RoadVehicleType ="VEHICLE_TYPE";
    public final String RoadInitialConditions = "INITIAL_CONDITIONS";
    public final String RoadInitialConditionsIcMicro = "IC_MICRO";
    public final String RoadInitialConditionsIcMacro = "IC_MACRO";
    public final String RoadTrafficSource = "TRAFFIC_SOURCE";
    public final String RoadTrafficSink = "TRAFFIC_SINK";
    public final String RoadFlowConservingInhomogeneities = "FLOW_CONSERVING_INHOMOGENEITIES";
    public final String RoadInhomogeneity = "INHOMOGENEITY";
    public final String RoadSpeedLimits = "SPEED_LIMITS";
    public final String RoadSpeedLimit = "SPEED_LIMIT";
    public final String RoadRamps = "RAMPS";
    public final String RoadRamp = "RAMP";
    public final String RoadSimpleRamp = "SIMPLE_RAMP";
    public final String RoadInflow = "INFLOW";
    
    }