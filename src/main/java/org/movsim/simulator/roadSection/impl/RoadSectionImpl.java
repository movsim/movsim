/**
 * Copyright (C) 2010, 2011 by Arne Kesting <movsim@akesting.de>, 
 *                             Martin Treiber <treibi@mtreiber.de>,
 *                             Ralph Germ <germ@ralphgerm.de>,
 *                             Martin Budden <mjbudden@gmail.com>
 *
 * ----------------------------------------------------------------------
 * 
 *  This file is part of 
 *  
 *  MovSim - the multi-model open-source vehicular-traffic simulator 
 *
 *  MovSim is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  MovSim is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with MovSim.  If not, see <http://www.gnu.org/licenses/> or
 *  <http://www.movsim.org>.
 *  
 * ----------------------------------------------------------------------
 */
package org.movsim.simulator.roadSection.impl;

import java.util.ArrayList;
import java.util.List;

import org.movsim.input.InputData;
import org.movsim.input.model.RoadInput;
import org.movsim.input.model.SimulationInput;
import org.movsim.input.model.simulation.ICMacroData;
import org.movsim.input.model.simulation.ICMicroData;
import org.movsim.input.model.simulation.SimpleRampData;
import org.movsim.input.model.simulation.TrafficLightData;
import org.movsim.simulator.Constants;
import org.movsim.simulator.roadSection.FlowConservingBottlenecks;
import org.movsim.simulator.roadSection.InitialConditionsMacro;
import org.movsim.simulator.roadSection.Onramp;
import org.movsim.simulator.roadSection.RoadSection;
import org.movsim.simulator.roadSection.RoadSectionGUI;
import org.movsim.simulator.roadSection.SpeedLimits;
import org.movsim.simulator.roadSection.TrafficLight;
import org.movsim.simulator.roadSection.UpstreamBoundary;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.simulator.vehicles.VehicleContainer;
import org.movsim.simulator.vehicles.VehicleGenerator;
import org.movsim.simulator.vehicles.VehiclePrototype;
import org.movsim.simulator.vehicles.impl.VehicleContainerImpl;
import org.movsim.simulator.vehicles.impl.VehicleGeneratorImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RoadSectionImpl implements RoadSection, RoadSectionGUI {
    final static Logger logger = LoggerFactory.getLogger(RoadSectionImpl.class);

    private final double roadLength;
    
    private final int nLanes;

    private double dt;
    
    private boolean isWithGUI;

    private VehicleContainer vehContainer;

    private VehicleGenerator vehGenerator;

    private UpstreamBoundary upstreamBoundary;

    private FlowConservingBottlenecks flowConsBottlenecks;

    private SpeedLimits speedlimits;

    private List<TrafficLight> trafficLights;

    private List<Onramp> simpleOnramps = null;
    


    public RoadSectionImpl(boolean isWithGUI, InputData inputData) {
        logger.info("Cstr. RoadSectionImpl");
        
        this.isWithGUI = isWithGUI;
        final SimulationInput simInput = inputData.getSimulationInput();
        this.dt = simInput.getTimestep();
        this.roadLength = simInput.getSingleRoadInput().getRoadLength();
        this.nLanes = simInput.getSingleRoadInput().getLanes();
        
       

        initialize(inputData);
        
        // TODO cross-check --> testing for correct dt setup  .... concept between Simulator, VehGenerator and this roadSection 
        if (Math.abs(dt - vehGenerator.requiredTimestep()) > Constants.SMALL_VALUE) {
            this.dt = vehGenerator.requiredTimestep();
            logger.info("model requires specific integration timestep. sets to dt={}", dt);
        }

    }

    private void initialize(InputData inputData) {
        vehContainer = new VehicleContainerImpl();
        
        vehGenerator = new VehicleGeneratorImpl(isWithGUI, inputData);
        
        final RoadInput roadInput = inputData.getSimulationInput().getSingleRoadInput();
        upstreamBoundary = new UpstreamBoundaryImpl(vehGenerator, vehContainer, roadInput.getUpstreamBoundaryData(), inputData.getProjectName());
        
        flowConsBottlenecks = new FlowConservingBottlenecksImpl(roadInput.getFlowConsBottleneckInputData());
        speedlimits = new SpeedLimitsImpl(roadInput.getSpeedLimitInputData());
        
        initialConditions(inputData.getSimulationInput());
        initTrafficLights(inputData.getSimulationInput());
        initOnramps(inputData);
    }

    public double roadLength() {
        return roadLength;
    }

    public VehicleContainer vehContainer() {
        return vehContainer;
    }

    public void update(int iTime, double time) {

        // check for crashes 
        checkForInconsistencies(iTime, time);
        
        updateRoadConditions(time);

        // vehicle accelerations
        accelerate(iTime, dt, time);

        // vehicle pos/speed 
        updatePositionAndSpeed(iTime, dt, time);

        updateDownstreamBoundary();

        updateUpstreamBoundary(iTime, dt, time);

        updateOnramps(iTime, dt, time);

    }

    private void initialConditions(SimulationInput simInput) {
        List<ICMacroData> icMacroData = simInput.getSingleRoadInput().getIcMacroData();
        if (!icMacroData.isEmpty()) {
            logger.debug("choose macro initial conditions: generate vehicles from macro-density ");

            final InitialConditionsMacro icMacro = new InitialConditionsMacroImpl(icMacroData);
            final double xLocalMin = 0; // if ringroad: set xLocalMin e.g. -SMALL_VAL

            double xLocal = roadLength; // start from behind
            while (xLocal > xLocalMin) {
                VehiclePrototype vehPrototype = vehGenerator.getVehiclePrototype();
                final double rhoLocal = icMacro.rho(xLocal);
                double speedInit = icMacro.vInit(xLocal);
                if (speedInit == 0) {
                    speedInit = vehPrototype.getEquilibriumSpeed(rhoLocal); // equil speed
                }
                final int laneEnter = Constants.MOST_RIGHT_LANE; 
                final Vehicle veh = vehGenerator.createVehicle(vehPrototype);
                vehContainer.add(veh, xLocal, speedInit, laneEnter);
                logger.debug("init conditions macro: rhoLoc={}/km, xLoc={}", 1000 * rhoLocal, xLocal);

                xLocal -= 1 / rhoLocal;

            }
        } else {
            logger.debug(("choose micro initial conditions"));
            List<ICMicroData> icSingle = simInput.getSingleRoadInput().getIcMicroData();
            for (ICMicroData ic : icSingle) {
                // TODO counter !!
                final double posInit = ic.getX();
                final double speedInit = ic.getSpeed();
                final String vehType = ic.getLabel();
                final int laneInit = ic.getInitLane();
                logger.info("set vehicle with label = {}", vehType);
                final Vehicle veh = (vehType.isEmpty()) ? vehGenerator.createVehicle() : vehGenerator.createVehicle(vehType);
                
                vehContainer.add(veh, posInit, speedInit, laneInit);
            }
        }
    }

    private void initTrafficLights(SimulationInput simInput) {
        trafficLights = new ArrayList<TrafficLight>();
        List<TrafficLightData> trafficLightData = simInput.getSingleRoadInput().getTrafficLightData();
        for (TrafficLightData tlData : trafficLightData) {
            trafficLights.add(new TrafficLightImpl(tlData));
        }
    }

    private void initOnramps(InputData inputData) {
        simpleOnramps = new ArrayList<Onramp>();
        final List<SimpleRampData> onrampData = inputData.getSimulationInput().getSingleRoadInput().getSimpleRamps();
        final String projectName = inputData.getProjectName();
        int rampIndex=1;
        for (SimpleRampData onrmp : onrampData) {
            simpleOnramps.add(new OnrampImpl(onrmp, vehGenerator, vehContainer, projectName, rampIndex));
            rampIndex++;
        }
    }

    private void updateDownstreamBoundary() {
        vehContainer.removeVehiclesDownstream(roadLength);
    }

    private void updateUpstreamBoundary(int itime, double dt, double time) {
        upstreamBoundary.update(itime, dt, time);
    }
    
    
    private void checkForInconsistencies(int iTime, double time){
        // crash test
        List<Vehicle> vehicles = vehContainer.getVehicles();
        for (int i=0, N=vehicles.size(); i<N; i++) {
            final Vehicle egoVeh = vehicles.get(i);
            final Vehicle vehFront = vehContainer.getLeader(egoVeh);
            final double netDistance = egoVeh.netDistance(vehFront); 
            if ( netDistance < 0 ) {
                logger.error("#########################################################");
                logger.error("Crash of Vehicle i = {} at x = {}m", i, egoVeh.position());
                logger.error("  with veh in front at x = {} on lane = {}", vehFront.position(), egoVeh.getIntLane());
                logger.error("net distance  = {}", netDistance);
                logger.error("container.size = {}", vehicles.size());
                final StringBuilder msg = new StringBuilder("\n");
                for (int j = Math.max(0, i - 8), M=vehicles.size(); j <= Math.min(i + 8, M - 1); j++) {
                    final Vehicle veh = vehicles.get(j);
                    msg.append(String.format("veh j = %d , pos=%6.2f, speed=%4.2f, accModel=%4.3f, length=%3.1f, lane=%3.1f, targetLane=%1d, id=%d%n", 
                            j, veh.position(), veh.speed(), veh.accModel(), veh.length(), veh.getLane(), veh.getIntLane(), veh.id()));
                } // of for
                logger.error(msg.toString());
                if(!isWithGUI){
                    logger.error(" !!! exit after crash !!! ");
                    System.exit(-99);
                }
            }
        }
    }

    private void accelerate(int iTime, double dt, double time) {
        List<Vehicle> vehicles = vehContainer.getVehicles();
        for (int i = 0; i < vehicles.size(); i++) {
            Vehicle veh = vehicles.get(i);
            final double x = veh.position();
            final double alphaT = flowConsBottlenecks.alphaT(x);
            final double alphaV0 = flowConsBottlenecks.alphaV0(x);
// logger.debug("i={}, x_pos={}", i, x);
// logger.debug("alphaT={}, alphaV0={}", alphaT, alphaV0);
            veh.calcAcceleration(dt, vehContainer, alphaT, alphaV0);
        }
    }

    private void updatePositionAndSpeed(int iTime, double dt, double time) {
        for (Vehicle veh : vehContainer.getVehicles()) {
            veh.updatePostionAndSpeed(dt);
        }
    }

    // traffic lights haben eigene Phasen-Dynamik !
    private void updateRoadConditions(double time) {
        if (!trafficLights.isEmpty()) {
            // first update traffic light status
            for(TrafficLight trafficLight : trafficLights){
                trafficLight.update(time);
            }
            // second update vehicle status approaching traffic lights
            for (Vehicle veh : vehContainer.getVehicles()) {
                for (TrafficLight trafficLight : trafficLights) {
                    veh.updateTrafficLight(time, trafficLight);
                }
            }
        }

        // set speedlimits
        if (!speedlimits.isEmpty()) {
            for (Vehicle veh : vehContainer.getVehicles()) {
                final double pos = veh.position();
                veh.setSpeedlimit(speedlimits.calcSpeedLimit(pos));
            }
        }
    }

    private void updateOnramps(int itime, double dt, double time) {
        if (simpleOnramps.isEmpty())
            return;
        for (Onramp onramp : simpleOnramps) {
            onramp.update(itime, dt, time);
        }
    }

    public double firstRampFlow() {
        // TODO Auto-generated method stub
        return 0;
    }

    public double upstreamInflow() {
        // TODO Auto-generated method stub
        return 0;
    }

    public double getTimestep() {
        return dt;
    }
    
    public List<TrafficLight> getTrafficLights(){
        return trafficLights;
    }
    
    public int nLanes(){
        return nLanes;
    }

}
