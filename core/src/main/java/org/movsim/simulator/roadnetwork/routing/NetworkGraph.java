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

import org.jgrapht.WeightedGraph;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.movsim.simulator.roadnetwork.LaneSegment;
import org.movsim.simulator.roadnetwork.RoadNetwork;
import org.movsim.simulator.roadnetwork.RoadSegment;
import org.movsim.simulator.roadnetwork.RoadSegment.NodeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class NetworkGraph {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(NetworkGraph.class);

    private static long vertexId = 0;

    private NetworkGraph() {
        // private constructor
    }

    public static WeightedGraph<Long, RoadSegment> create(RoadNetwork roadNetwork) {
        DefaultDirectedWeightedGraph<Long, RoadSegment> graph = new DefaultDirectedWeightedGraph<>(RoadSegment.class);
        for (RoadSegment roadSegment : roadNetwork) {
            Long fromVertex = getOrCreateVertex(NodeType.ORIGIN, roadSegment);
            Long toVertex = getOrCreateVertex(NodeType.DESTINATION, roadSegment);
            graph.addVertex(fromVertex);
            graph.addVertex(toVertex);
            graph.addEdge(fromVertex, toVertex, roadSegment);
            LOG.info("edge weight={}", graph.getEdgeWeight(roadSegment));
            graph.setEdgeWeight(roadSegment, roadSegment.roadLength());
            // add vertex to successor links AND to predecessor links of successors
            for (LaneSegment laneSegment : roadSegment.laneSegments()) {
                if (laneSegment.sinkLaneSegment() != null) {
                    RoadSegment successor = laneSegment.sinkLaneSegment().roadSegment();
                    successor.setNode(NodeType.ORIGIN, toVertex);
                    for (LaneSegment laneSegmentSuccessor : successor.laneSegments()) {
                        if (laneSegmentSuccessor.sourceLaneSegment() != null) {
                            RoadSegment predecessor = laneSegmentSuccessor.sourceLaneSegment().roadSegment();
                            predecessor.setNode(NodeType.DESTINATION, toVertex);
                        }
                    }
                }
            }
        }
        LOG.info("created graph with " + graph.edgeSet().size() + " edges and " + graph.vertexSet().size() + " nodes.");
        for (RoadSegment roadSegment : roadNetwork) {
            LOG.info(roadSegment.toString());
        }
        return graph;
    }

    private static long getOrCreateVertex(NodeType nodeType, RoadSegment roadSegment) {
        Long vertex = roadSegment.getNode(nodeType);
        if (vertex == null) {
            vertex = vertexId++;
            roadSegment.setNode(nodeType, vertex);
        }
        return vertex;
    }


}
