package org.movsim.input.model.output.impl;

import java.util.LinkedList;
import java.util.List;

import org.jdom.Element;
import org.movsim.input.model.output.TravelTimesInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TravelTimesInputImpl implements TravelTimesInput {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(TrajectoriesInputImpl.class);

    private final List<TravelTimeRouteInput> routes;
    
    public TravelTimesInputImpl(Element elem){
        routes = new LinkedList<TravelTimeRouteInput>();
        
        final List<Element> routeElems = elem.getChildren("ROUTE");
        if (routeElems != null) {
            for (final Element routeElem : routeElems) {
                routes.add(new TravelTimeRouteInput(routeElem));
            }
        }
        

    }
    
    
    
    @Override
    public List<TravelTimeRouteInput> getRoutes() {
        return routes;
    }

}
