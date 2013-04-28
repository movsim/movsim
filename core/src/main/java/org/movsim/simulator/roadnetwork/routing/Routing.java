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

import java.util.List;
import java.util.Map;

import org.jgrapht.WeightedGraph;
import org.jgrapht.alg.DijkstraShortestPath;
import org.movsim.autogen.Routes;
import org.movsim.simulator.roadnetwork.RoadNetwork;
import org.movsim.simulator.roadnetwork.RoadSegment;
import org.movsim.simulator.roadnetwork.RoadSegment.NodeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

public class Routing {

    /** The Constant LOG. */
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

    public Route findRoute(RoadSegment start, RoadSegment destination) throws IllegalStateException {
        return findRoute(start.userId(), destination.userId());
    }

    public Route findRoute(String startRoadId, String destinationRoadId) throws IllegalStateException {
        if (graph == null) {
            graph = NetworkGraph.create(roadNetwork);
        }
        Preconditions.checkArgument(startRoadId != null && !startRoadId.isEmpty());
        Preconditions.checkArgument(destinationRoadId != null && !destinationRoadId.isEmpty());

        Route route = new Route(createRouteName(startRoadId, destinationRoadId));
        RoadSegment startRoadSegment = roadNetwork.findByUserId(startRoadId);
        route.add(startRoadSegment);
        RoadSegment endRoadSegment = roadNetwork.findByUserId(destinationRoadId);

        LOG.info("Shortest path from roadSegment={} to={}", startRoadId, destinationRoadId);
        LOG.info("From node={} to node={}", startRoadSegment.getNode(NodeType.DESTINATION),
                endRoadSegment.getNode(NodeType.DESTINATION));

        List<RoadSegment> path = DijkstraShortestPath.findPathBetween(graph,
                startRoadSegment.getNode(NodeType.DESTINATION), endRoadSegment.getNode(NodeType.DESTINATION));
        if (path == null) {
            throw new IllegalStateException("cannot find route from startRoadId=" + startRoadId
                    + " to destinationRoadId=" + destinationRoadId);
        }

        for (RoadSegment rs : path) {
            route.add(rs);
            LOG.info("roadSegment on path={}", rs);
        }
        return route;
    }

    private static String createRouteName(String startRoadId, String destinationRoadId) {
        StringBuilder sb = new StringBuilder();
        sb.append("from_").append(startRoadId).append("_").append(destinationRoadId);
        return sb.toString();
    }
}
