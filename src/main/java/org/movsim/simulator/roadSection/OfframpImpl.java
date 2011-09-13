package org.movsim.simulator.roadSection;

import java.util.ArrayList;
import java.util.List;

import org.movsim.input.InputData;
import org.movsim.input.model.simulation.RampData;
import org.movsim.output.LoopDetector;
import org.movsim.simulator.Constants;
import org.movsim.simulator.roadSection.impl.AbstractRoadSection;
import org.movsim.simulator.roadSection.impl.OnrampImpl;
import org.movsim.simulator.vehicles.VehicleContainer;
import org.movsim.simulator.vehicles.VehicleGenerator;
import org.movsim.simulator.vehicles.impl.VehicleContainerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OfframpImpl extends AbstractRoadSection implements RoadSection {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(OfframpImpl.class);
    
    //final static int N_LANES = 1;

    private final double mergeLength;

    /** The x up ramp marks the start of the ramp. */
    private final double xDownRamp;

    /** The x down ramp marks the end of the ramp. */
    // private final double xDownRamp;

    private final double xToMain;
    
    
    public OfframpImpl(final RampData rampData){
        super(rampData);
        
        // vehicles start at initial position
        mergeLength = rampData.getRampMergingLength();
        xToMain = rampData.getRampStartPosition();
        xDownRamp = roadLength - mergeLength; // rampData.getRampStartPosition();

        // create vehicle container for onramp lane
        vehContainers = new ArrayList<VehicleContainer>();
        vehContainers.add(new VehicleContainerImpl(Constants.MOST_RIGHT_LANE));
    }
    
    
    
    @Override
    public void laneChanging(long iterationCount, double dt, double time) {
        // TODO Auto-generated method stub
    }

    @Override
    public void updateRoadConditions(long iterationCount, double time) {
        // TODO Auto-generated method stub
        
    }

    @Override
    // identical to RoadSectionImpl
    public void updateDownstreamBoundary() {
//        for (VehicleContainer vehContainerLane : vehContainers) {
//            vehContainerLane.removeVehiclesDownstream(roadLength);
//        }
    }

    @Override
    public double getRampMergingLength() {
        return mergeLength;
    }

    @Override
    public double getRampPositionToMainroad() {
        return xToMain;
    }

    @Override
    public List<TrafficLight> getTrafficLights() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<LoopDetector> getLoopDetectors() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void updateDetectors(long iterationCount, double dt, double simulationTime) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public List<RoadSection> rampFactory(InputData inputData) {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override 
    public void updateUpstreamBoundary(long iterationCount, double dt, double time) {
        //upstreamBoundary.update(iterationCount, dt, time);
    }
    
    
    
}
