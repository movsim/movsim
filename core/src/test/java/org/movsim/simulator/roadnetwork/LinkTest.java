/**
 * Copyright (C) 2010, 2011 by Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden
 *                             <movsim.org@gmail.com>
 * ---------------------------------------------------------------------------------------------------------------------
 * 
 *  This file is part of 
 *  
 *  MovSim - the multi-model open-source vehicular-traffic simulator 
 *
 *  MovSim is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 *  version.
 *
 *  MovSim is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 *  warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with MovSim.
 *  If not, see <http://www.gnu.org/licenses/> or <http://www.movsim.org>.
 *  
 * ---------------------------------------------------------------------------------------------------------------------
 */

package org.movsim.simulator.roadnetwork;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Test module for the Link class.
 */
public class LinkTest {

    protected static class RoadMappingConcrete extends RoadMapping {
        public RoadMappingConcrete(int laneCount) {
            super(laneCount, 0, 0);
        }

        public RoadMappingConcrete(int laneCount, double roadLength) {
            this(laneCount);
            this.roadLength = roadLength;
        }

        @Override
        public RoadMappingConcrete.PosTheta map(double roadPos, double delta) {
            return posTheta;
        }
    }

    /**
     * Test method for
     * {@link org.movsim.simulator.roadsegment.traffic.Link#addLanePair(int, org.movsim.simulator.roadsegment.traffic.RoadSegment, int, org.movsim.simulator.roadsegment.traffic.RoadSegment)}
     */
    @Test
    public final void testAddLanePair() {
        final int laneCount = 1;
        final double roadLength = 1000.0;
        final RoadMapping m = new RoadMappingConcrete(laneCount, roadLength);
        final RoadSegment r1 = new RoadSegment(m);
        final RoadSegment r2 = new RoadSegment(m);

        Link.addLanePair(Lane.LANE1, r1, Lane.LANE1, r2);

        assertEquals(r2, r1.sinkRoadSegment(Lane.LANE1));
        assertEquals(Lane.LANE1, r1.sinkLane(Lane.LANE1));
        assertEquals(r1, r2.sourceRoadSegment(Lane.LANE1));
        assertEquals(Lane.LANE1, r2.sourceLane(Lane.LANE1));
    }

    /**
     * Test method for
     * {@link org.movsim.simulator.roadsegment.traffic.Link#addJoin(org.movsim.simulator.roadsegment.traffic.RoadSegment, org.movsim.simulator.roadsegment.traffic.RoadSegment)}
     */
    @Test
    public final void testAddJoin() {
        final int laneCount = 2;
        final RoadSegment r1 = new RoadSegment(1000.0, laneCount);
        final RoadSegment r2 = new RoadSegment(1000.0, laneCount);

        Link.addJoin(r1, r2);

        assertEquals(r2, r1.sinkRoadSegment(Lane.LANE1));
        assertEquals(r2, r1.sinkRoadSegment(Lane.LANE2));
        assertEquals(Lane.LANE1, r1.sinkLane(Lane.LANE1));
        assertEquals(Lane.LANE2, r1.sinkLane(Lane.LANE2));
        assertEquals(r1, r2.sourceRoadSegment(Lane.LANE1));
        assertEquals(r1, r2.sourceRoadSegment(Lane.LANE2));
        assertEquals(Lane.LANE1, r2.sourceLane(Lane.LANE1));
        assertEquals(Lane.LANE2, r2.sourceLane(Lane.LANE2));
    }

    /**
     * Test method for
     * {@link org.movsim.simulator.roadsegment.traffic.Link#addOffsetJoin(int, org.movsim.simulator.roadsegment.traffic.RoadSegment, org.movsim.simulator.roadsegment.traffic.RoadSegment)}
     */
    @Test
    public final void testAddOffsetJoin() {
        final int laneCount = 2;
        final int offset = 1;
        final RoadMapping m1 = new RoadMappingConcrete(laneCount, 1000.0);
        final RoadSegment r1 = new RoadSegment(m1);
        final RoadMapping m2 = new RoadMappingConcrete(laneCount + offset, 1000.0);
        final RoadSegment r2 = new RoadSegment(m2);
        r2.setLaneType(Lane.LANE1, Lane.Type.ENTRANCE);
        final RoadMapping m3 = new RoadMappingConcrete(laneCount, 1000.0);
        final RoadSegment r3 = new RoadSegment(m3);
        Link.addOffsetJoin(r2.trafficLaneMin() - r1.trafficLaneMin(), r1, r2);
        assertEquals(r2, r1.sinkRoadSegment(Lane.LANE1));
        assertEquals(r2, r1.sinkRoadSegment(Lane.LANE2));
        assertEquals(Lane.LANE2, r1.sinkLane(Lane.LANE1));
        assertEquals(Lane.LANE3, r1.sinkLane(Lane.LANE2));
        assertEquals(r1, r2.sourceRoadSegment(Lane.LANE2));
        assertEquals(r1, r2.sourceRoadSegment(Lane.LANE3));
        assertEquals(Lane.LANE1, r2.sourceLane(Lane.LANE2));
        assertEquals(Lane.LANE2, r2.sourceLane(Lane.LANE3));
        Link.addOffsetJoin(r3.trafficLaneMin() - r2.trafficLaneMin(), r2, r3);
        assertEquals(r3, r2.sinkRoadSegment(Lane.LANE2));
        assertEquals(r3, r2.sinkRoadSegment(Lane.LANE3));
        assertEquals(Lane.LANE1, r2.sinkLane(Lane.LANE2));
        assertEquals(Lane.LANE2, r2.sinkLane(Lane.LANE3));
        assertEquals(r2, r3.sourceRoadSegment(Lane.LANE1));
        assertEquals(r2, r3.sourceRoadSegment(Lane.LANE2));
        assertEquals(Lane.LANE2, r3.sourceLane(Lane.LANE1));
        assertEquals(Lane.LANE3, r3.sourceLane(Lane.LANE2));
    }

    /**
     * Test method for
     * {@link org.movsim.simulator.roadsegment.traffic.Link#addMerge(org.movsim.simulator.roadsegment.traffic.RoadSegment, org.movsim.simulator.roadsegment.traffic.RoadSegment, org.movsim.simulator.roadsegment.traffic.RoadSegment)}
     */
    @Test
    public final void testAddMerge() {
        //fail("Not yet implemented"); //$NON-NLS-1$
    }

    /**
     * Test method for
     * {@link org.movsim.simulator.roadsegment.traffic.Link#addFork(org.movsim.simulator.roadsegment.traffic.RoadSegment, org.movsim.simulator.roadsegment.traffic.RoadSegment, org.movsim.simulator.roadsegment.traffic.RoadSegment)}
     */
    @Test
    public final void testAddFork() {
        //fail("Not yet implemented"); //$NON-NLS-1$
    }
}
