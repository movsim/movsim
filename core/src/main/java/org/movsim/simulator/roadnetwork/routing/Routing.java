/*
 * Copyright (C) 2010, 2011, 2012 by Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden
 * <movsim.org@gmail.com>
 * -----------------------------------------------------------------------------------------
 * 
 * This file is part of
 * 
 * MovSim - the multi-model open-source vehicular-traffic simulator.
 * 
 * MovSim is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MovSim is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MovSim. If not, see <http://www.gnu.org/licenses/>
 * or <http://www.movsim.org>.
 * 
 * -----------------------------------------------------------------------------------------
 */
package org.movsim.simulator.roadnetwork.routing;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.jgrapht.WeightedGraph;
import org.jgrapht.alg.DijkstraShortestPath;
import org.movsim.autogen.Routes;
import org.movsim.simulator.roadnetwork.RoadNetwork;
import org.movsim.simulator.roadnetwork.RoadSegment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class Routing {

    /**
     * The Constant LOG.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Routing.class);

    private final Map<String, Route> predefinedRoutes;

    private final RoadNetwork roadNetwork;

    // see http://jgrapht.org/ for library documentation
    private WeightedGraph<Long, RoadSegment> graph;

    public Routing(Routes routesInput, RoadNetwork roadNetwork) {
        this.roadNetwork = Preconditions.checkNotNull(roadNetwork);
        predefinedRoutes = Maps.newHashMap();
        if (routesInput != null) {
            createPredefinedRoutes(routesInput);
        }
        graph = NetworkGraph.create(roadNetwork); // lazy init. vs. early failure!!
    }

    private void createPredefinedRoutes(Routes routesInput) {
        for (org.movsim.autogen.Route routeInput : routesInput.getRoute()) {
            Route route = new Route(routeInput.getLabel());
            for (org.movsim.autogen.Road roadInput : routeInput.getRoad()) {
                RoadSegment roadSegment = roadNetwork.findByUserId(roadInput.getId());
                Preconditions.checkNotNull(roadSegment,
                        "cannot create route \"" + route.getName() + "\" with undefinied road=" + roadInput.getId()
                                + " (consider +/- in case of bidirectional roads)");
                route.add(roadSegment);
            }
            Route replaced = predefinedRoutes.put(route.getName(), route);
            if (replaced != null) {
                throw new IllegalArgumentException("route with name=" + route.getName() + " already defined.");
            }
        }
        LOG.info("created {} predefined routes", predefinedRoutes.size());
    }

    /**
     * Returns the {@link Route} for the route's name.
     *
     * @throws IllegalStateException
     */
    public Route get(String name) {
        Preconditions.checkArgument(name != null && !name.isEmpty());
        Route route = predefinedRoutes.get(name);
        if (route == null) {
            throw new IllegalStateException("route with name \"" + name + "\" not defined.");
        }
        return route;
    }

    public boolean hasRoute(String name) {
        return predefinedRoutes.containsKey(name);
    }

    /**
     * @throws IllegalStateException
     */
    public Route findRoute(RoadSegment start, RoadSegment destination) {
        return findRoute(start.userId(), destination.userId());
    }

    /**
     * @throws IllegalStateException
     */
    public Route findRoute(String startRoadId, String destinationRoadId) {
        if (graph == null) {
            graph = NetworkGraph.create(roadNetwork);
        }
        Preconditions.checkArgument(startRoadId != null && !startRoadId.isEmpty());
        Preconditions.checkArgument(destinationRoadId != null && !destinationRoadId.isEmpty());

        RoadSegment startRoadSegment = roadNetwork.findByUserId(startRoadId);
        if (startRoadSegment == null) {
            throw new IllegalArgumentException("cannot find roadSegment=" + startRoadId);
        }
        RoadSegment endRoadSegment = roadNetwork.findByUserId(destinationRoadId);
        if (endRoadSegment == null) {
            throw new IllegalArgumentException("cannot find roadSegment=" + destinationRoadId);
        }

        Route route = new Route(createRouteName(startRoadId, destinationRoadId));
        route.add(startRoadSegment);

        LOG.debug("Shortest path from roadSegment={} to={}", startRoadId, destinationRoadId);
        LOG.debug("From node={} to node={}", startRoadSegment.getDestinationNode().getId(),
                endRoadSegment.getDestinationNode().getId());

        List<RoadSegment> path = DijkstraShortestPath
                .findPathBetween(graph, startRoadSegment.getDestinationNode().getId(),
                        endRoadSegment.getDestinationNode().getId());

        if (path == null) {
            LOG.error("cannot find route from startRoadId={} to destinationRoadId={}", startRoadId, destinationRoadId);
            throw new IllegalStateException(
                    "cannot find route from startRoadId=" + startRoadId + " to destinationRoadId=" + destinationRoadId);
        }

        for (RoadSegment roadSegment : path) {
            route.add(roadSegment);
            LOG.debug("add roadSegment={} to route={}", roadSegment, route.getName());
        }
        return route;
    }

    private static String createRouteName(String startRoadId, String destinationRoadId) {
        StringBuilder sb = new StringBuilder();
        sb.append("from_").append(startRoadId).append("_").append(destinationRoadId);
        return sb.toString();
    }
}
