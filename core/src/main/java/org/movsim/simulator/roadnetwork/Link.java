/*
 * Copyright (C) 2010, 2011, 2012 by Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden
 *                                   <movsim.org@gmail.com>
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

package org.movsim.simulator.roadnetwork;

/**
 * <p>
 * Simple connection between road segments, where each lane has an unambiguous predecessor and successor. Allows merging and forking of road
 * segments.
 * </p>
 * <p>
 * There are one or more source road segments and one or more sink road segments. Each lane in a source road segment is normally paired with
 * lane in a sink road segment. It is, however, possible to have unpaired lanes in either the source road segment (for example when the road
 * narrows, or at the end of an on-ramp) or in the sink road segment (for example when the road widens, or at the start of an off-ramp).
 * </p>
 * <p>
 * For complex connections between road segments, for example an urban road junction, or a roundabout, use a <code>Junction</code>.
 * </p>
 * 
 */
public class Link {

    /**
     * Private constructor, this class has only static functions and so should not be instantiated.
     */
    private Link() {
    }

    /**
     * Adds a lane pair. Joins a lane in the source road segment to its corresponding lane in the sink road segment.
     * 
     * @param fromLane
     * @param fromRoadsegment
     * @param toLane
     * @param toRoadSegment
     */
    public static void addLanePair(int fromLane, RoadSegment fromRoadsegment, int toLane, RoadSegment toRoadSegment) {
        // toRoadSegment.setSourceRoadSegmentForLane(fromRoadsegment, toLane);
        // toRoadSegment.setSourceLaneForLane(fromLane, toLane);
        // fromRoadsegment.setSinkRoadSegmentForLane(toRoadSegment, fromLane);
        // fromRoadsegment.setSinkLaneForLane(toLane, fromLane);

        toRoadSegment.setSourceLaneSegmentForLane(fromRoadsegment.laneSegment(fromLane), toLane);
        fromRoadsegment.setSinkLaneSegmentForLane(toRoadSegment.laneSegment(toLane), fromLane);
    }

    /**
     * Convenience function to join two road segments together, end to end.
     * 
     * @param sourceRoad
     * @param sinkRoad
     * @return sinkRoad, for convenience
     */
    public static RoadSegment addJoin(RoadSegment sourceRoad, RoadSegment sinkRoad) {
        final int limit = Math.min(sourceRoad.trafficLaneMax(), sinkRoad.trafficLaneMax());
        for (int lane = 1; lane <= limit; ++lane) {
            addLanePair(lane, sourceRoad, lane, sinkRoad);
        }
//        final int offset = sinkRoad.trafficLaneMin() - sourceRoad.trafficLaneMin();
//        assert sourceRoad.laneCount() + offset == sinkRoad.laneCount();
//        if (offset < 0) {
//            final int limit = sourceRoad.laneCount() + offset;
//            for (int i = 0; i < limit; ++i) {
//                addLanePair(i - offset, sourceRoad, i, sinkRoad);
//            }
//        } else {
//            final int laneCount = sourceRoad.laneCount();
//            for (int i = 0; i < laneCount; ++i) {
//                addLanePair(i, sourceRoad, i + offset, sinkRoad);
//            }
//        }
        return sinkRoad;
    }


    /**
     * Convenience function to add a merge of two road segments into a single road segments.
     * 
     * @param fromRoad1
     * @param fromRoad2
     * @param toRoad
     */
    public static void addMerge(RoadSegment fromRoad1, RoadSegment fromRoad2, RoadSegment toRoad) {
        assert fromRoad1.laneCount() + fromRoad2.laneCount() == toRoad.laneCount();

        final int laneCount1 = fromRoad1.laneCount();
        for (int i = 0; i < laneCount1; ++i) {
            addLanePair(i, fromRoad1, i, toRoad);
        }
        final int laneCount2 = fromRoad2.laneCount();
        for (int i = 0; i < laneCount2; ++i) {
            addLanePair(i, fromRoad1, i + laneCount1, toRoad);
        }
    }

    /**
     * Convenience function to add a fork from one road segment into two road segments.
     * 
     * @param fromRoad
     * @param toRoad1
     * @param toRoad2
     */
    public static void addFork(RoadSegment fromRoad, RoadSegment toRoad1, RoadSegment toRoad2) {
        assert fromRoad.laneCount() == toRoad1.laneCount() + toRoad2.laneCount();

        final int laneCount1 = toRoad1.laneCount();
        for (int i = 0; i < laneCount1; ++i) {
            addLanePair(i, fromRoad, i, toRoad1);
        }
        final int laneCount2 = toRoad2.laneCount();
        for (int i = 0; i < laneCount2; ++i) {
            addLanePair(i + laneCount1, fromRoad, i, toRoad2);
        }
    }
}
