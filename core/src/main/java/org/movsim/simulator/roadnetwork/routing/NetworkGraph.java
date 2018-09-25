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
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.movsim.input.ProjectMetaData;
import org.movsim.simulator.roadnetwork.LaneSegment;
import org.movsim.simulator.roadnetwork.Node;
import org.movsim.simulator.roadnetwork.RoadNetwork;
import org.movsim.simulator.roadnetwork.RoadSegment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

final class NetworkGraph {

    private static final Logger LOG = LoggerFactory.getLogger(NetworkGraph.class);

    private static long vertexId = 0;

    private NetworkGraph() {
        // private constructor
    }

    public static WeightedGraph<Long, RoadSegment> create(RoadNetwork roadNetwork) {
        DefaultDirectedWeightedGraph<Long, RoadSegment> graph = new DefaultDirectedWeightedGraph<>(RoadSegment.class);
        HashMap<RoadSegment, Node> connections = Maps.newLinkedHashMap();
        for (final RoadSegment roadSegment : roadNetwork) {
            connections.clear();
            connections.put(roadSegment, roadSegment.getDestinationNode());
            for (LaneSegment laneSegment : roadSegment.laneSegments()) {
                if (laneSegment.sinkLaneSegment() != null) {
                    RoadSegment successor = laneSegment.sinkLaneSegment().roadSegment();
                    connections.put(successor, successor.getOriginNode());
                    for (LaneSegment laneSegmentSuccessor : successor.laneSegments()) {
                        if (laneSegmentSuccessor.sourceLaneSegment() != null) {
                            RoadSegment predecessor = laneSegmentSuccessor.sourceLaneSegment().roadSegment();
                            connections.put(predecessor, predecessor.getDestinationNode());
                        }
                    }
                }
            }
            createOrUpdateNode(connections);

            connections.clear();
            connections.put(roadSegment, roadSegment.getOriginNode());
            for (LaneSegment laneSegment : roadSegment.laneSegments()) {
                if (laneSegment.sourceLaneSegment() != null) {
                    RoadSegment predecessor = laneSegment.sourceLaneSegment().roadSegment();
                    connections.put(predecessor, predecessor.getDestinationNode());
                    for (LaneSegment laneSegmentPredecessor : predecessor.laneSegments()) {
                        if (laneSegmentPredecessor.sinkLaneSegment() != null) {
                            RoadSegment successor = laneSegmentPredecessor.sinkLaneSegment().roadSegment();
                            connections.put(successor, successor.getOriginNode());
                        }
                    }
                }
            }
            createOrUpdateNode(connections);
        }
        LOG.info("created graph with {} edges and {} nodes", graph.edgeSet().size(), graph.vertexSet().size());
        for (RoadSegment roadSegment : roadNetwork) {
            long fromVertex = roadSegment.getOriginNode().getId();
            long toVertex = roadSegment.getDestinationNode().getId();
            graph.addVertex(fromVertex);
            graph.addVertex(toVertex);
            graph.addEdge(fromVertex, toVertex, roadSegment);
            graph.setEdgeWeight(roadSegment, roadSegment.roadLength());
            LOG.info("weight={}, roadSegment={}", graph.getEdgeWeight(roadSegment), roadSegment);
        }

        if (ProjectMetaData.getInstance().isWriteDotFile()) {
            exportToFile(graph);
        }
        return graph;
    }

    private static void createOrUpdateNode(HashMap<RoadSegment, Node> connections) {
        Preconditions.checkArgument(connections.size() > 0);
        showConnections(connections);
        long nodeId = determineNodeId(connections);
        if (nodeId == Long.MAX_VALUE) {
            nodeId = vertexId++;
        }
        for (Node nodeType : connections.values()) {
            nodeType.setId(nodeId);
        }
    }

    private static void showConnections(HashMap<RoadSegment, Node> connections) {
        LOG.info("connections at node:");
        for (Map.Entry<RoadSegment, Node> entry : connections.entrySet()) {
            LOG.info("roadSegment={}, node={}", entry.getKey(), entry.getValue());
        }
    }

    private static long determineNodeId(HashMap<RoadSegment, Node> nodes) {
        long nodeId = Long.MAX_VALUE;
        for (Map.Entry<RoadSegment, Node> entry : nodes.entrySet()) {
            Node nodeType = entry.getValue();
            if (nodeType.hasId()) {
                if (nodeId != Long.MAX_VALUE && nodeId != nodeType.getId()) {
                    throw new IllegalArgumentException(
                            "nodeId=" + nodeId + " contradicts node=" + nodeType.toString() + " of roadSegment=" + entry
                                    .getKey().userId() + " already set with value=" + nodeType.getId());
                }
                nodeId = nodeType.getId();
            }
        }
        return nodeId;
    }

    private static void exportToFile(DefaultDirectedWeightedGraph<Long, RoadSegment> graph) {
        String fileName = ProjectMetaData.getInstance().getProjectName() + GraphExporter.FILE_ENDING_DOT;
        GraphExporter.exportDOT(graph, fileName);
        LOG.info("export graph to file={}", fileName);
    }

}
