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

import org.jgrapht.DirectedGraph;
import org.jgrapht.ext.ComponentAttributeProvider;
import org.jgrapht.ext.DOTExporter;
import org.jgrapht.ext.IntegerNameProvider;
import org.movsim.simulator.roadnetwork.RoadSegment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.Map;

class GraphExporter {

    private static final Logger LOG = LoggerFactory.getLogger(GraphExporter.class);

    static final String FILE_ENDING_DOT = ".dot";

    static void exportDOT(DirectedGraph<Long, RoadSegment> graph, String fileName) {
        StringWriter writer = new StringWriter();

        // Vertex attribute provider
        ComponentAttributeProvider<Long> vertexAttributeProvider = new ComponentAttributeProvider<Long>() {
            @Override
            public Map<String, String> getComponentAttributes(Long v) {
                Map<String, String> map = new LinkedHashMap<>();
                map.put("label", "nodeId=" + v.toString());
                return map;
            }
        };

        // Edge attribute provider
        ComponentAttributeProvider<RoadSegment> edgeAttributeProvider = new ComponentAttributeProvider<RoadSegment>() {
            @Override
            public Map<String, String> getComponentAttributes(RoadSegment e) {
                Map<String, String> map = new LinkedHashMap<>();
                map.put("label", "edge=" + e.userId() + ", length=" + e.roadLength() + ", lanes=" + e.laneCount());
                return map;
            }
        };

        DOTExporter<Long, RoadSegment> exporter = new DOTExporter<>(new IntegerNameProvider<Long>(), null, null,
                vertexAttributeProvider, edgeAttributeProvider);
        exporter.export(writer, graph);

        try (FileWriter fileWriter = new FileWriter(new File(fileName))) {
            fileWriter.write(writer.toString());
            fileWriter.flush();
        } catch (IOException e) {
            LOG.error("error while writing", e);
        }
    }
}
