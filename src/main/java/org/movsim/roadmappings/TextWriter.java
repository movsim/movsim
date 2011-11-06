/*
 * Copyright (C) 2010, 2011  Martin Budden, Ralph Germ, Arne Kesting, and Martin Treiber.
 *
 * This file is part of MovSim.
 *
 * MovSim is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MovSim is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MovSim.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.movsim.roadmappings;

import java.util.Iterator;

import org.movsim.simulator.RoadMapping;
import org.movsim.simulator.RoadNetwork;
import org.movsim.simulator.RoadSegment;
import org.movsim.simulator.TrafficFlowBase;
import org.movsim.simulator.TrafficSource;
import org.movsim.simulator.RoadMapping.PosTheta;

/**
 * Externalizes the road network to a text format. Mainly a debug utility.
 */
@SuppressWarnings({ "nls", "boxing" })
public class TextWriter {
    /**
     * Private constructor, this class has only static functions and so should not be instantiated.
     */
    private TextWriter() {
    }

    private static void write(String string) {
        System.out.println(string);
    }

    /**
     * Externalizes the given road network.
     * @param roadNetwork
     */
    public static void externalize(RoadNetwork roadNetwork) {
        for (final RoadSegment roadSegment : roadNetwork) {
            externalize(roadSegment);
        }
    }

    private static void externalize(RoadSegment roadSegment) {
        write("RoadSegment:");
        write("  id: " + roadSegment.id());
        if (roadSegment.source() != null
                && roadSegment.source().type() == TrafficFlowBase.Type.SOURCE) {
            write("  Source:");
            externalize(roadSegment.source());
        }
        if (roadSegment.sink() != null) {
            write("  Sink:");
            externalize(roadSegment.sink());
        }
        if (roadSegment.roadMapping() != null) {
            externalize(roadSegment.roadMapping());
        }
    }

    private static void externalize(RoadMapping roadMapping) {
        if (roadMapping.getClass().equals(RoadMappingLine.class)) {
            externalize((RoadMappingLine)roadMapping);
        } else if (roadMapping.getClass().equals(RoadMappingPolyLine.class)) {
            externalize((RoadMappingPolyLine)roadMapping);

        } else if (roadMapping.getClass().equals(RoadMappingPolyBezier.class)) {
            externalize((RoadMappingPolyBezier)roadMapping);

        } else {
            write("  RoadMapping:");
            String string = String.format(
                    "    RoadLength: %.0f, RoadWidth: %.0f,  LaneCount: %d\n",
                    roadMapping.roadLength(), roadMapping.roadWidth(), roadMapping.laneCount());
            System.out.print(string);
        }
    }

    private static void externalize(RoadMappingLine roadMapping) {
        write("  RoadMappingStraight:");
        String string = String.format("    RoadLength: %.0f, RoadWidth: %.0f,  LaneCount: %d\n",
                roadMapping.roadLength(), roadMapping.roadWidth(), roadMapping.laneCount());
        System.out.print(string);
        final PosTheta posTheta = roadMapping.startPos();
        System.out.print("    { " + posTheta.x);
        System.out.print(", " + posTheta.y);
        System.out.print(", " + roadMapping.x1);
        System.out.print(", " + roadMapping.y1);
        write(" };");
    }

    private static void externalize(RoadMappingPolyLine roadMappingPoly) {
        write("  RoadMappingPolyLine:");
        String string = String.format("    RoadLength: %.0f, RoadWidth: %.0f,  LaneCount: %d\n",
                roadMappingPoly.roadLength(), roadMappingPoly.roadWidth(),
                roadMappingPoly.laneCount());
        System.out.print(string);
        final PosTheta posTheta = roadMappingPoly.startPos();
        System.out.print("    { " + RoadMappingPolyLine.RELATIVE_POINTS);
        System.out.print(", " + posTheta.x);
        System.out.print(", " + posTheta.y);
        double prevX = posTheta.x;
        double prevY = posTheta.y;
        for (final RoadMappingLine roadMapping : roadMappingPoly.roadMappings) {
            System.out.print(", " + (roadMapping.x1 - prevX));
            System.out.print(", " + (roadMapping.y1 - prevY));
            prevX = roadMapping.x1;
            prevY = roadMapping.y1;
        }
        write(" };");
    }

    private static void externalize(RoadMappingPolyBezier roadMappingPoly) {
        write("  RoadMappingPolyBezier:");
        String string = String.format("    RoadLength: %.0f, RoadWidth: %.0f,  LaneCount: %d\n",
                roadMappingPoly.roadLength(), roadMappingPoly.roadWidth(),
                roadMappingPoly.laneCount());
        System.out.print(string);
        Iterator<RoadMappingBezier> iterator = roadMappingPoly.iterator();
        if (!iterator.hasNext()) {
            return;
        }

        RoadMappingBezier roadMapping = iterator.next();
        System.out.print("    { " + RoadMappingPolyBezier.RELATIVE_POINTS);
        System.out.print(", " + roadMapping.p0x);
        System.out.print(", " + roadMapping.p0y);
        System.out.print(", " + roadMapping.p2x);
        System.out.print(", " + roadMapping.p2y);
        System.out.print(", " + roadMapping.p1x);
        System.out.print(", " + roadMapping.p1y);

        double prevX = roadMapping.p2x;
        double prevY = roadMapping.p2y;
        while (iterator.hasNext()) {
            roadMapping = iterator.next();
            System.out.print(", " + (roadMapping.p2x - prevX));
            System.out.print(", " + (roadMapping.p2y - prevY));
            // System.out.print(", " + roadMapping.controlT());
            prevX = roadMapping.p2x;
            prevY = roadMapping.p2y;
        }
        write(" };");
    }

    private static void externalize(TrafficFlowBase node) {
        write("  TrafficFlowBase");
        write("    id: " + node.id());
        write("    type: " + node.type());
    }

    private static void externalize(TrafficSource source) {
        write("  TrafficSource"); //$NON-NLS-1$
        write("    id: " + source.id()); //$NON-NLS-1$
        write("    type: " + source.type()); //$NON-NLS-1$
    }
}
