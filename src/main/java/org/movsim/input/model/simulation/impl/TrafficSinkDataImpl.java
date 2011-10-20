package org.movsim.input.model.simulation.impl;

import org.jdom.Element;
import org.movsim.input.model.simulation.TrafficSinkData;

public class TrafficSinkDataImpl implements TrafficSinkData{

    /** The with logging. */
    private final boolean withLogging;
    

    private final int sinkId;
    
    public TrafficSinkDataImpl(Element elem){
        sinkId = Integer.parseInt(elem.getAttributeValue("id"));
        withLogging = Boolean.parseBoolean(elem.getAttributeValue("logging"));
    }
    
    @Override
    public boolean withLogging() {
        return withLogging;
    }
    
   
    @Override
    public int getSinkId() {
        return sinkId;
    }
    
}
