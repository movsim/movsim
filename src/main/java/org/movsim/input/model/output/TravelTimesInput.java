package org.movsim.input.model.output;

import java.util.LinkedList;
import java.util.List;

import org.jdom.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TravelTimesInput {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(TravelTimesInput.class);

    private final List<TravelTimeRouteInput> routes;
    
    public TravelTimesInput(Element elem){
        routes = new LinkedList<TravelTimeRouteInput>();
        
        final List<Element> routeElems = elem.getChildren("ROUTE");
        if (routeElems != null) {
            for (final Element routeElem : routeElems) {
                routes.add(new TravelTimeRouteInput(routeElem));
            }
        }
        

    }
    
    public List<TravelTimeRouteInput> getRoutes() {
        return routes;
    }

}
