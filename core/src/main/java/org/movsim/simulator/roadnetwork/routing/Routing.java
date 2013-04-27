package org.movsim.simulator.roadnetwork.routing;

import java.util.Map;

import org.movsim.autogen.Routes;
import org.movsim.simulator.roadnetwork.RoadNetwork;
import org.movsim.simulator.roadnetwork.RoadSegment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

public class Routing {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(Routing.class);

    private final Map<String, Route> predefinedRoutes;

    private final RoadNetwork roadNetwork;

    public Routing(Routes routesInput, RoadNetwork roadNetwork) {
        this.roadNetwork = Preconditions.checkNotNull(roadNetwork);
        predefinedRoutes = Maps.newHashMap();
        if (routesInput != null) {
            createPredefinedRoutes(routesInput);
        }
    }

    private void createPredefinedRoutes(Routes routesInput) {
        for (org.movsim.autogen.Route routeInput : routesInput.getRoute()) {
            Route route = new Route(routeInput.getLabel());
            for (org.movsim.autogen.Road roadInput : routeInput.getRoad()) {
                RoadSegment roadSegment = roadNetwork.findByUserId(roadInput.getId());
                Preconditions.checkNotNull(roadSegment, "cannot create route \"" + route.getName()
                        + "\" with undefinied road=" + roadInput.getId());
                route.add(roadSegment);
            }
            Route replaced = predefinedRoutes.put(route.getName(), route);
            if (replaced != null) {
                throw new IllegalArgumentException("route with name=" + route.getName() + " already defined.");
            }
        }
        LOG.info("created " + predefinedRoutes.size() + " predefined routes.");
    }

    /**
     * Returns the {@link Route} for the route's name.
     * 
     * @param name
     * @return
     * @throws IllegalStateException
     */
    public Route get(String name) throws IllegalStateException {
        Preconditions.checkArgument(name!=null && !name.isEmpty());
        Route route = predefinedRoutes.get(name);
        if(route == null){
            throw new IllegalStateException("route with name \"" + name + "\" not defined.");
        }
        return route;
    }

    public boolean hasRoute(String name) {
        return predefinedRoutes.containsKey(name);
    }

    public Route findRoute(RoadSegment start, RoadSegment destination) throws IllegalStateException {
        return findRoute(start.userId(), destination.userId());
    }


    public Route findRoute(String startRoadId, String destinationRoadId) throws IllegalStateException {
        Preconditions.checkArgument(startRoadId!=null && !startRoadId.isEmpty());
        Preconditions.checkArgument(destinationRoadId!=null && !destinationRoadId.isEmpty());
        Route route = new Route(createRouteName(startRoadId, destinationRoadId));
        RoadSegment roadSegment = roadNetwork.findByUserId(startRoadId);
        route.add(roadSegment);

        // TODO find route...

        // throw exception if route cannot be build
        // if (route == null) {
        // throw new IllegalStateException("cannot find route from startRoadId=" + startRoadId
        // + " to destinationRoadId=" + destinationRoadId);
        // }
        return route;
    }

    private static String createRouteName(String startRoadId, String destinationRoadId) {
        StringBuilder sb = new StringBuilder();
        sb.append("from_").append(startRoadId).append("_").append(destinationRoadId);
        return sb.toString();
    }
}
