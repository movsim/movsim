package org.movsim.simulator.roadSection.impl;

import java.util.LinkedList;
import java.util.List;

import org.movsim.input.InputData;
import org.movsim.input.model.SimulationInput;
import org.movsim.input.model.simulation.RampData;
import org.movsim.simulator.roadSection.FlowConservingBottlenecks;
import org.movsim.simulator.roadSection.UpstreamBoundary;
import org.movsim.simulator.vehicles.Moveable;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.simulator.vehicles.VehicleContainer;
import org.movsim.simulator.vehicles.VehicleGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractRoadSection {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(AbstractRoadSection.class);
    
    /** The road length. */
    protected final double roadLength;

    /** The n lanes. */
    protected final int nLanes;

    /** The dt. */
    protected double dt;

    /** The id. */
    protected final long id;
    
   // protected final boolean withCrashExit;
    
    protected final boolean instantaneousFileOutput;

    /** The veh generator. */
    protected final VehicleGenerator vehGenerator;
    
    /** The veh container list (for each lane). */
    protected List<VehicleContainer> vehContainers;
    
    protected List<Vehicle> stagedVehicles;

    /** The upstream boundary. */
    protected UpstreamBoundary upstreamBoundary;
    

    /** The flow cons bottlenecks. */
    protected FlowConservingBottlenecks flowConsBottlenecks;
    
    
    // TODO same constructor for onramp and mainroad (and offramp) 
    public AbstractRoadSection(final InputData inputData, final VehicleGenerator vehGenerator){
        this.vehGenerator = vehGenerator;
        final SimulationInput simInput = inputData.getSimulationInput();
        this.dt = simInput.getTimestep();
       // this.withCrashExit = simInput.isWithCrashExit();
        this.roadLength = simInput.getSingleRoadInput().getRoadLength();
        this.nLanes = simInput.getSingleRoadInput().getLanes();
        this.id = simInput.getSingleRoadInput().getId();
        this.instantaneousFileOutput = inputData.getProjectMetaData().isInstantaneousFileOutput();
        init();
    }

    
    // onramp
    public AbstractRoadSection(final RampData rampData, final VehicleGenerator vehGenerator){
        this.vehGenerator = vehGenerator;
        this.roadLength = rampData.getRoadLength();
        this.nLanes = 1;
        this.id = rampData.getId();
        this.instantaneousFileOutput = false;  // TODO
        init();
    }
    
    
    
    // offramp
    public AbstractRoadSection(final RampData rampData){
        this.vehGenerator = null;
        this.roadLength = rampData.getRoadLength();
        this.nLanes = 1;
        this.id = rampData.getId();
        this.instantaneousFileOutput = false;  
        init();
    }

    
    
    
    private void init(){
        stagedVehicles = new LinkedList<Vehicle>();
    }
    
    
    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.roadSection.RoadSection#roadLength()
     */
  
    public double getRoadLength() {
        return roadLength;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.roadSection.RoadSection#id()
     */
    public long getId() {
        return id;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.roadSection.RoadSection#getTimestep()
     */
    public double getTimestep() {
        return dt;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.simulator.roadSection.RoadSection#nLanes()
     */
    public int getNumberOfLanes() {
        return nLanes;
    }
    
    public List<VehicleContainer> getVehContainers() {
        return vehContainers;
    }

    public VehicleContainer getVehContainer(int laneIndex) {
        return vehContainers.get(laneIndex);
    }

    
    

    /**
     * Accelerate.
     * 
     * @param iterationCount
     *            the i time
     * @param dt
     *            the dt
     * @param time
     *            the time
     */
    public void accelerate(long iterationCount, double dt, double time) {
        for (VehicleContainer vehContainerLane : vehContainers) {
            final List<Vehicle> vehiclesOnLane = vehContainerLane.getVehicles();
            for (int i = 0, N = vehiclesOnLane.size(); i < N; i++) {
                final Vehicle veh = vehiclesOnLane.get(i);
                final double x = veh.getPosition();
                // TODO treat null case
                final double alphaT = (flowConsBottlenecks==null) ? 1 : flowConsBottlenecks.alphaT(x);
                final double alphaV0 = (flowConsBottlenecks==null) ? 1 : flowConsBottlenecks.alphaV0(x);
                // logger.debug("i={}, x_pos={}", i, x);
                // logger.debug("alphaT={}, alphaV0={}", alphaT, alphaV0);
                veh.calcAcceleration(dt, vehContainerLane, alphaT, alphaV0);
            }
        }
    }

    /**
     * Update position and speed.
     * 
     * @param iterationCount
     * @param dt
     *            the dt
     * @param time
     *            the time
     */
    public void updatePositionAndSpeed(long iterationCount, double dt, double time) {
        for (VehicleContainer vehContainerLane : vehContainers) {
            for (final Vehicle veh : vehContainerLane.getVehicles()) {
                veh.updatePostionAndSpeed(dt);
            }
        }
    }
    
    
    // general lane-changing update also for one-lane roads for updating vehicle's lane-changing process 
    public void laneChanging(long iterationCount, double dt, double time) {
        for (final VehicleContainer vehContainerLane : vehContainers) {

            stagedVehicles.clear();

            final List<Vehicle> vehiclesOnLane = vehContainerLane.getVehicles();
            for (final Vehicle veh : vehiclesOnLane) {
                if (veh.considerLaneChanging(dt, vehContainers)) {
                    stagedVehicles.add(veh);
                }
            }

            // assign staged vehicles to new lanes
            // necessary update of new situation *after* lane-changing decisions

            for (final Vehicle veh : stagedVehicles) {
                vehContainers.get(veh.getLane()).removeVehicle(veh);
                vehContainers.get(veh.getTargetLane()).add(veh);
            }

        }
    }

    /**
     * Update upstream boundary.
     * 
     * @param iterationCount
     * @param dt
     *            the dt
     * @param time
     *            the time
     */
    public void updateUpstreamBoundary(long iterationCount, double dt, double time) {
        upstreamBoundary.update(iterationCount, dt, time);
    }
    
    /**
     * Check for inconsistencies.
     * 
     * @param iterationCount
     * @param time
     *            the time
     */
    public void checkForInconsistencies(long iterationCount, double time, boolean isWithCrashExit) {
        // crash test, iterate over all lanes separately
        for (int laneIndex = 0, laneIndexMax = vehContainers.size(); laneIndex < laneIndexMax; laneIndex++) {
            final VehicleContainer vehContainerLane = vehContainers.get(laneIndex);
            final List<Vehicle> vehiclesOnLane = vehContainerLane.getVehicles();
            for (int i = 0, N = vehiclesOnLane.size(); i < N; i++) {
                final Moveable egoVeh = vehiclesOnLane.get(i);
                final Moveable vehFront = vehContainerLane.getLeader(egoVeh);
                final double netDistance = egoVeh.getNetDistance(vehFront);
                if (netDistance < 0) {
                    logger.error("#########################################################");
                    logger.error("Crash of Vehicle i = {} at x = {}m", i, egoVeh.getPosition());
                    if (vehFront != null) {
                        logger.error("with veh in front at x = {} on lane = {}", vehFront.getPosition(), egoVeh.getLane());
                    }
                    logger.error("roadID = {}", getId());
                    logger.error("net distance  = {}", netDistance);
                    logger.error("lane index    = {}", laneIndex);
                    logger.error("container.size = {}", vehiclesOnLane.size());
                    final StringBuilder msg = new StringBuilder("\n");
                    for (int j = Math.max(0, i - 8), M = vehiclesOnLane.size(); j <= Math.min(i + 8, M - 1); j++) {
                        final Moveable veh = vehiclesOnLane.get(j);
                        msg.append(String.format(
                                "veh=%d, pos=%6.2f, speed=%4.2f, accModel=%4.3f, length=%3.1f, lane=%d, id=%d%n", j,
                                veh.getPosition(), veh.getSpeed(), veh.accModel(), veh.getLength(), veh.getLane(), veh.getId()));
                    }
                    logger.error(msg.toString());
                    if (isWithCrashExit) {
                	logger.error(" !!! exit after crash !!! ");
                	System.exit(-99);
                    }
                }
            }
        }
    }
}
