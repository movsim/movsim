/*
 * Copyright (C) 2010, 2011, 2012 by Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden
 *                                   <movsim.org@gmail.com>
 * -----------------------------------------------------------------------------------------
 * 
 * This file is part of
 * 
 * MovSim - the multi-model open-source vehicular-traffic simulator.
 * 
 * MovSim is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MovSim is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MovSim. If not, see <http://www.gnu.org/licenses/>
 * or <http://www.movsim.org>.
 * 
 * -----------------------------------------------------------------------------------------
 */
package org.movsim.input;

/**
 * The Interface XmlElementNames.
 */
public interface XmlElementNames {

    public final String Root = "SCENARIO";

    public final String Consumption = "FUEL_CONSUMPTION";
    public final String ConsumptionModel = "CONSUMPTION_MODEL";
    public final String ConsumptionCarData = "CAR";
    public final String ConsumptionEngineData = "ENGINE";
    public final String ConsumptionEngineGears = "GEAR_RATIOS";
    public final String ConsumptionEngineGear = "GEAR_RATIO";

    public final String DriverVehicleUnits = "VEHICLES";
    public final String DriverVehicleUnit = "VEHICLE";
    public final String VehicleMemory = "MEMORY";
    public final String VehicleNoise = "NOISE";
    public final String VehicleLongitudinalModel = "LONGITUDINAL_MODEL";
    public final String VehicleLaneChangeModel = "LANE_CHANGE_MODEL";
    public final String VehicleLaneChangeModelMobil = "MOBIL";

    public final String Simulation = "SIMULATION";
    public final String TrafficComposition = "TRAFFIC_COMPOSITION";
    public final String Road = "ROAD";
    public final String RoadTrafficLight = "TRAFFIC_LIGHT";
    public final String RoadTrafficLights = "TRAFFIC_LIGHTS";
    public final String RoadVehicleType = "VEHICLE_TYPE";
    public final String RoadInitialConditions = "INITIAL_CONDITIONS";
    public final String RoadInitialConditionsIcMicro = "IC_MICRO";
    public final String RoadInitialConditionsIcMacro = "IC_MACRO";
    public final String RoadTrafficSource = "TRAFFIC_SOURCE";
    public final String RoadTrafficSink = "TRAFFIC_SINK";
    public final String RoadFlowConservingInhomogeneities = "FLOW_CONSERVING_INHOMOGENEITIES";
    public final String RoadInhomogeneity = "INHOMOGENEITY";
    public final String RoadSpeedLimits = "SPEED_LIMITS";
    public final String RoadSpeedLimit = "SPEED_LIMIT";
    public final String RoadRamps = "SIMPLE_RAMPS";
    public final String RoadSimpleRamp = "SIMPLE_RAMP";
    public final String RoadInflow = "INFLOW";
    public final String RoadOutput = "OUTPUT";
    public final String OutputFloatingCarData = "FLOATING_CAR_DATA";
    public final String OutputSpatioTemporal = "SPATIOTEMPORAL";
    public final String OutputDetectors = "DETECTORS";
    public final String OutputTrafficlightRecorder = "TRAFFIC_LIGHT_RECORDER";
    public final String OutputTrajectories = "TRAJECTORIES";
    public final String OutputTravelTimes = "TRAVELTIMES";

}