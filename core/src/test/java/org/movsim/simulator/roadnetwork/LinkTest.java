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

package org.movsim.simulator.roadnetwork;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.movsim.roadmappings.RoadMapping;

/**
 * Test module for the Link class.
 */
@SuppressWarnings("static-method")
public class LinkTest {

    /**
     * Test method for
     * {@link org.movsim.simulator.roadnetwork.Link#addLanePair(int, org.movsim.simulator.roadnetwork.RoadSegment, int, org.movsim.simulator.roadnetwork.RoadSegment)}
     */
    @Test
    public final void testAddLanePair() {
        final int laneCount = 1;
        final double roadLength = 1000.0;
        final RoadMapping m = RoadMappingConcrete.create(laneCount, roadLength);
        final RoadSegment r1 = new RoadSegment(roadLength, laneCount, m, RoadSegmentDirection.FORWARD);
        final RoadSegment r2 = new RoadSegment(roadLength, laneCount, m, RoadSegmentDirection.FORWARD);

        Link.addLanePair(Lanes.LANE1, r1, Lanes.LANE1, r2);

        assertEquals(r2, r1.sinkRoadSegment(Lanes.LANE1));
        assertEquals(Lanes.LANE1, r1.sinkLane(Lanes.LANE1));
        assertEquals(r1, r2.sourceRoadSegment(Lanes.LANE1));
        assertEquals(Lanes.LANE1, r2.sourceLane(Lanes.LANE1));
    }

    /**
     * Test method for
     * {@link org.movsim.simulator.roadnetwork.Link#addJoin(org.movsim.simulator.roadnetwork.RoadSegment, org.movsim.simulator.roadnetwork.RoadSegment)}
     */
    @Test
    public final void testAddJoin() {
        final int laneCount = 2;
        final RoadSegment r1 = new RoadSegment(1000.0, laneCount);
        final RoadSegment r2 = new RoadSegment(1000.0, laneCount);

        Link.addJoin(r1, r2);

        assertEquals(r2, r1.sinkRoadSegment(Lanes.LANE1));
        assertEquals(r2, r1.sinkRoadSegment(Lanes.LANE2));
        assertEquals(Lanes.LANE1, r1.sinkLane(Lanes.LANE1));
        assertEquals(Lanes.LANE2, r1.sinkLane(Lanes.LANE2));
        assertEquals(r1, r2.sourceRoadSegment(Lanes.LANE1));
        assertEquals(r1, r2.sourceRoadSegment(Lanes.LANE2));
        assertEquals(Lanes.LANE1, r2.sourceLane(Lanes.LANE1));
        assertEquals(Lanes.LANE2, r2.sourceLane(Lanes.LANE2));
    }

    /**
     * Test method for
     * {@link org.movsim.simulator.roadnetwork.Link#addOffsetJoin(int, org.movsim.simulator.roadnetwork.RoadSegment, org.movsim.simulator.roadnetwork.RoadSegment)}
     */
    @Test
    public final void testAddOffsetJoin() {
        final int laneCount = 2;
        final int offset = 1;
        final RoadMapping m1 = RoadMappingConcrete.create(laneCount, 1000.0);
        final RoadSegment r1 = new RoadSegment(m1.roadLength(), laneCount, m1, RoadSegmentDirection.FORWARD);
        final RoadMapping m2 = RoadMappingConcrete.create(laneCount + offset, 1000.0);
        final RoadSegment r2 = new RoadSegment(m2.roadLength(), laneCount + offset, m2, RoadSegmentDirection.FORWARD);
        r2.setLaneType(Lanes.LANE3, Lanes.Type.ENTRANCE);
        final RoadMapping m3 = RoadMappingConcrete.create(laneCount, 1000.0);
        final RoadSegment r3 = new RoadSegment(m3.roadLength(), laneCount, m3, RoadSegmentDirection.FORWARD);
        Link.addJoin(r1, r2);

        assertEquals(r2, r1.sinkRoadSegment(Lanes.LANE1));
        assertEquals(r2, r1.sinkRoadSegment(Lanes.LANE2));
        assertEquals(Lanes.LANE1, r1.sinkLane(Lanes.LANE1));
        assertEquals(Lanes.LANE2, r1.sinkLane(Lanes.LANE2));
        assertEquals(r1, r2.sourceRoadSegment(Lanes.LANE1));
        assertEquals(r1, r2.sourceRoadSegment(Lanes.LANE2));
        assertEquals(Lanes.LANE1, r2.sourceLane(Lanes.LANE1));
        assertEquals(Lanes.LANE2, r2.sourceLane(Lanes.LANE2));

        Link.addJoin(r2, r3);
        assertEquals(r3, r2.sinkRoadSegment(Lanes.LANE1));
        assertEquals(r3, r2.sinkRoadSegment(Lanes.LANE2));
        assertEquals(Lanes.LANE1, r2.sinkLane(Lanes.LANE1));
        assertEquals(Lanes.LANE2, r2.sinkLane(Lanes.LANE2));
        assertEquals(r2, r3.sourceRoadSegment(Lanes.LANE1));
        assertEquals(r2, r3.sourceRoadSegment(Lanes.LANE2));
        assertEquals(Lanes.LANE1, r3.sourceLane(Lanes.LANE1));
        assertEquals(Lanes.LANE2, r3.sourceLane(Lanes.LANE2));
    }

    /**
     * Test method for
     * {@link org.movsim.simulator.roadnetwork.Link#addMerge(org.movsim.simulator.roadnetwork.RoadSegment, org.movsim.simulator.roadnetwork.RoadSegment, org.movsim.simulator.roadnetwork.RoadSegment)}
     */
    @Test
    public final void testAddMerge() {
        final int laneCount = 2;
        final int exitLaneCount = 1;
        final RoadSegment r0 = new RoadSegment(300.0, laneCount + exitLaneCount);
        final RoadSegment r1 = new RoadSegment(400.0, laneCount);
        r0.setLaneType(Lanes.LANE3, Lanes.Type.EXIT);// so Lane3 is exit laneIndex of r1
        // join r0 and r1 so vehicles move from r0 to r1
        // lane3 of r0 joins to lane2 of r1
        // lane2 of r0 joins to lane1 of r1
        // lane1 of r0 has no successor
        Link.addJoin(r0, r1);
        assertEquals(null, r0.sinkRoadSegment(Lanes.LANE3));
        assertEquals(r1, r0.sinkRoadSegment(Lanes.LANE2));
        assertEquals(r1, r0.sinkRoadSegment(Lanes.LANE1));
        assertEquals(Lanes.NONE, r0.sinkLane(Lanes.LANE3));
        assertEquals(Lanes.LANE2, r0.sinkLane(Lanes.LANE2));
        assertEquals(Lanes.LANE1, r0.sinkLane(Lanes.LANE1));
        assertEquals(r0, r1.sourceRoadSegment(Lanes.LANE1));
        assertEquals(r0, r1.sourceRoadSegment(Lanes.LANE2));
        assertEquals(Lanes.LANE1, r1.sourceLane(Lanes.LANE1));
        assertEquals(Lanes.LANE2, r1.sourceLane(Lanes.LANE2));
    }

    /**
     * Test method for
     * {@link org.movsim.simulator.roadnetwork.Link#addFork(org.movsim.simulator.roadnetwork.RoadSegment, org.movsim.simulator.roadnetwork.RoadSegment, org.movsim.simulator.roadnetwork.RoadSegment)}
     */
    @Test
    public final void testAddFork() {
        //fail("Not yet implemented"); //$NON-NLS-1$
    }
}
