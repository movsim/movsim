package org.movsim.simulator.impl;

import java.util.HashMap;
import java.util.Map;

import org.movsim.simulator.roadSection.RoadSection;

public class RoadNetwork {

    private Map<Long, RoadSection> roadNetwork;
    
    public RoadNetwork(){
        roadNetwork = new HashMap<Long, RoadSection>();
    }

    public void add(final RoadSection roadSection) {
        // the key 
        final long roadId = roadSection.getId();
        roadNetwork.put(roadId, roadSection);
    }
    
    
    public double getRoadLength(long roadId) {
        return roadNetwork.get(roadId).getRoadLength();
    }

    public long getFromId(long roadId) {
        return roadNetwork.get(roadId).getFromId();
    }

    public long getToId(long roadId) {
        return roadNetwork.get(roadId).getToId();
    }
    
    
}
