package org.movsim.simulator.impl;

import java.util.HashMap;
import java.util.Map;

import org.movsim.simulator.roadSection.RoadSection;

public class RoadNetworkDeprecated {
    
    // singleton pattern
    
    private static RoadNetworkDeprecated instance = null;

    private Map<Long, RoadSection> roadNetwork;
    
    private RoadNetworkDeprecated(){
        // Exists only to defeat instantiation.
        roadNetwork = new HashMap<Long, RoadSection>();
    }
    
    public static RoadNetworkDeprecated getInstance() {
        if(instance == null) {
           instance = new RoadNetworkDeprecated();
        }
        return instance;
     }


    public void add(final RoadSection roadSection) {
        final long roadId = roadSection.getId();
        getInstance().roadNetwork.put(roadId, roadSection);
    }
    
    
    public double getRoadLength(long roadId) {
        return getInstance().roadNetwork.get(roadId).getRoadLength();
    }

    public long getFromId(long roadId) {
        return getInstance().roadNetwork.get(roadId).getFromId();
    }

    public long getToId(long roadId) {
        return getInstance().roadNetwork.get(roadId).getToId();
    }
    
    public String toString(){
       StringBuilder str = new StringBuilder();
       for (Map.Entry<Long, RoadSection> entry: getInstance().roadNetwork.entrySet()) {
           final long key = entry.getKey();
           str.append("roadId="+key);
           str.append(", roadLength="+getRoadLength(key));
           str.append(", fromId="+getFromId(key));
           str.append(", toId="+getToId(key));
           //str.append(", isPeriodBoundaryConditions="+isPeriodBoundary(key));
           str.append("\n");
       }
       return str.toString();
    }

    public boolean isPeriodBoundary(long roadId) {
        return false; // TODO
        //return (getInstance().getToId(roadId) == getInstance().getFromId(roadId));
    }
    
}
