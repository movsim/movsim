package org.movsim.output.impl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.movsim.input.model.output.impl.TravelTimeRouteInput;
import org.movsim.output.SimOutput;
import org.movsim.simulator.roadSection.RoadSection;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.simulator.vehicles.VehicleContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TravelTimeRoute {
    
    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(TravelTimeRoute.class);
    

    private final long startId;
    private final long endId;
    private final double startPosition;
    private final double endPosition;
    
    private Map<Vehicle,Double> vehiclesOnRoute;
    
    public TravelTimeRoute(TravelTimeRouteInput travelTimeRouteInput){
        this.startId = travelTimeRouteInput.getStartId();
        this.endId = travelTimeRouteInput.getEndId();
        this.startPosition = travelTimeRouteInput.getStartPosition();
        this.endPosition = travelTimeRouteInput.getEndPosition();
        
        vehiclesOnRoute = new HashMap<Vehicle,Double>();
        
        logger.info("consider travel times on route with startId={}, endId={}", startId, endId);
        logger.info("with startPos={}, endPos={}", startPosition, endPosition);
    }
    
    public void update(long iterationCount, double time, double timestep, final Map<Long,RoadSection> roadSectionsMap){
        // check first start_position
        // TODO catch error if road id not available
        
        checkNewVehicles(time, roadSectionsMap.get(startId));  
        
        
        // check end_position
        checkPassedVehicles(time);
    }
    
    private void checkNewVehicles(final double timeStartOfRoute, final RoadSection roadSection){
        for(final VehicleContainer lane : roadSection.getVehContainers()){
            for(final Vehicle veh : lane.getVehicles()){
//                if(veh.getPosition() > 100 && veh.getPosition()<1000){
//                System.out.printf("veh: pos=%.4f, posOld=%.4f\n", veh.getPosition(), veh.getPositionOld());
//                }
                if(veh.getPositionOld() < startPosition && veh.getPosition() > startPosition){
                    vehiclesOnRoute.put(veh, timeStartOfRoute);
                    //System.out.printf("veh at x=%.2f put to travel time route, roadId=%d\n", veh.getPosition(), veh.getRoadId());
                }
            }
        }
    }
    
    
    private void checkPassedVehicles(final double timeEndOfRoute){
        final List<Vehicle> stagedVehicles = new LinkedList<Vehicle>();
        for (Map.Entry<Vehicle, Double> entry : vehiclesOnRoute.entrySet()) {
            final Vehicle veh = entry.getKey();
            final double startTime = entry.getValue();
            //System.out.printf("consider vehicle ... roadId=%d, pos=%.4f\n", veh.getRoadId(), veh.getPosition());
            if( veh.getRoadId() == endId && veh.getPosition() > endPosition){
                final double travelTimeOnRoute = timeEndOfRoute - startTime;
                if(endId==1){
                    System.out.printf("vehicle with finished traveltime route: startTime=%.4f, endTime=%.4f, tt=%.4f\n", startTime, timeEndOfRoute,travelTimeOnRoute);
                }
                stagedVehicles.add(veh); 
            }
        }
        for(final Vehicle veh : stagedVehicles){
            vehiclesOnRoute.remove(veh);
            //System.out.printf("remove vehicle at x=%.2f from route map", veh.getPosition());
        }

    }
}
