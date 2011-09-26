package org.movsim.output.impl;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.movsim.input.model.output.TravelTimesInput;
import org.movsim.input.model.output.impl.TravelTimeRouteInput;
import org.movsim.output.TravelTimes;
import org.movsim.simulator.roadSection.RoadSection;

public class TravelTimesImpl implements TravelTimes{

    private List<TravelTimeRoute> routes;
    
    private final Map<Long,RoadSection> roadSectionsMap;
    
    public TravelTimesImpl(final TravelTimesInput travelTimesInput, final Map<Long,RoadSection> roadSectionsMap){
        this.roadSectionsMap = roadSectionsMap;
        routes = new LinkedList<TravelTimeRoute>();
        for(final TravelTimeRouteInput routeInput : travelTimesInput.getRoutes()){
            routes.add(new TravelTimeRoute(routeInput));
            
        }
    }
    
    
    public void update(long iterationCount, double time, double timestep){
        for(TravelTimeRoute route : routes){
            route.update(iterationCount, time, timestep, roadSectionsMap);
        }
    }
    
    
}
