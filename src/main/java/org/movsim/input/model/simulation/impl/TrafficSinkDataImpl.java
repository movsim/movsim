package org.movsim.input.model.simulation.impl;

import org.jdom.Element;
import org.movsim.input.model.simulation.TrafficSinkData;

public class TrafficSinkDataImpl implements TrafficSinkData{

    /** The with logging. */
    private final boolean withLogging;
    

    private final int sourceId;
    
    public TrafficSinkDataImpl(Element elem){
        sourceId = Integer.parseInt(elem.getAttributeValue("id"));
        withLogging = Boolean.parseBoolean(elem.getAttributeValue("logging"));
    }
    
    @Override
    public boolean withLogging() {
        return withLogging;
    }
    
   
    @Override
    public int getSourceId() {
        return sourceId;
    }
    
}
