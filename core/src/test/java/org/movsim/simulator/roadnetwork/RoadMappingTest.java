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

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.movsim.roadmappings.RoadMapping;

/**
 * Test module for the RoadMapping class.
 * 
 */
@SuppressWarnings("static-method")
public class RoadMappingTest {
    private static final double delta = 0.00001;

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

    @Test
    public void testRoadMappingBaseInt() {
        final int LANE_COUNT = 3;
        final RoadMapping roadMapping = new RoadMappingConcrete(LANE_COUNT);
        assertEquals(LANE_COUNT, roadMapping.laneCount());
    }

    @Test
    public void testRoadMappingBaseIntDouble() {
        final int LANE_COUNT = 3;
        final double roadLength = 56.4;
        final RoadMapping roadMapping = new RoadMappingConcrete(LANE_COUNT, roadLength);
        assertEquals(LANE_COUNT, roadMapping.laneCount());
        assertEquals(roadLength, roadMapping.roadLength(), delta);
    }

    @Test
    public void testMapDouble() {
        // fail("Not yet implemented");
    }

    @Test
    public void testMapDoubleInt() {
        // fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.movsim.roadmappings.RoadMapping#roadLength()}
     */
    @Test
    public void testRoadLength() {
        final int LANE_COUNT = 3;
        final double roadLength = 56.4;
        final RoadMapping roadMapping = new RoadMappingConcrete(LANE_COUNT, roadLength);
        assertEquals(roadLength, roadMapping.roadLength(), delta);
    }

    @Test
    public void testRoadWidth() {
        final int LANE_COUNT = 3;
        RoadMapping roadMapping = new RoadMappingConcrete(LANE_COUNT);
        assertEquals(roadMapping.laneWidth() * LANE_COUNT, roadMapping.roadWidth(), delta);
        roadMapping = new RoadMappingConcrete(LANE_COUNT + 1);
        assertEquals(roadMapping.laneWidth() * roadMapping.laneCount(), roadMapping.roadWidth(), delta);
    }

    /**
     * Test method for {@link org.movsim.simulator.roadnetwork.RoadMapping#laneWidth()}
     */
    //@Test
    //public void testLaneWidth() {
    //   final int LANE_COUNT = 3;
    //   final RoadMapping roadMapping = new RoadMappingConcrete(LANE_COUNT);
    //   assertEquals(RoadMapping.DEFAULT_LANE_WIDTH, roadMapping.laneWidth(), delta);
    //}

    /**
     * Test method for {@link org.movsim.roadmappings.RoadMapping#laneCount()}
     */
    @Test
    public void testLaneCount() {
        final int LANE_COUNT = 3;
        RoadMapping roadMapping = new RoadMappingConcrete(LANE_COUNT);
        assertEquals(LANE_COUNT, roadMapping.laneCount());
        final double roadLength = 56.4;
        roadMapping = new RoadMappingConcrete(LANE_COUNT, roadLength);
        assertEquals(LANE_COUNT, roadMapping.laneCount());
    }

    /**
     * Test method for {@link org.movsim.roadmappings.RoadMapping#laneOffset(int)}
     */
    @Test
    public void testLaneOffset() {
        RoadMapping roadMapping = new RoadMappingConcrete(0);
        final double laneWidth = roadMapping.laneWidth();

        roadMapping = new RoadMappingConcrete(1);
        assertEquals(0.0, roadMapping.laneOffset(Lanes.NONE), delta);
        assertEquals(0.0, roadMapping.laneOffset(Lanes.LANE1), delta);

        roadMapping = new RoadMappingConcrete(3);
        assertEquals(0.0, roadMapping.laneOffset(Lanes.NONE), delta);
        assertEquals(-laneWidth, roadMapping.laneOffset(Lanes.LANE1), delta);
        assertEquals(0.0, roadMapping.laneOffset(Lanes.LANE2), delta);
        assertEquals(+laneWidth, roadMapping.laneOffset(Lanes.LANE3), delta);

        roadMapping = new RoadMappingConcrete(5);
        assertEquals(0.0, roadMapping.laneOffset(Lanes.NONE), delta);
        assertEquals(-2 * laneWidth, roadMapping.laneOffset(Lanes.LANE1), delta);
        assertEquals(-laneWidth, roadMapping.laneOffset(Lanes.LANE2), delta);
        assertEquals(0.0, roadMapping.laneOffset(Lanes.LANE3), delta);
        assertEquals(laneWidth, roadMapping.laneOffset(Lanes.LANE4), delta);
        assertEquals(2 * laneWidth, roadMapping.laneOffset(Lanes.LANE5), delta);

        roadMapping = new RoadMappingConcrete(2);
        assertEquals(0.0, roadMapping.laneOffset(Lanes.NONE), delta);
        assertEquals(-0.5 * laneWidth, roadMapping.laneOffset(Lanes.LANE1), delta);
        assertEquals(0.5 * laneWidth, roadMapping.laneOffset(Lanes.LANE2), delta);

        roadMapping = new RoadMappingConcrete(4);
        assertEquals(0.0, roadMapping.laneOffset(Lanes.NONE), delta);
        assertEquals(-1.5 * laneWidth, roadMapping.laneOffset(Lanes.LANE1), delta);
        assertEquals(-0.5 * laneWidth, roadMapping.laneOffset(Lanes.LANE2), delta);
        assertEquals(0.5 * laneWidth, roadMapping.laneOffset(Lanes.LANE3), delta);
        assertEquals(1.5 * laneWidth, roadMapping.laneOffset(Lanes.LANE4), delta);
    }

    @Test
    public void testMapPosThetaDoubleDouble() {
        // fail("Not yet implemented");
    }

    @Test
    public void testMapVehicle() {
        //fail("Not yet implemented"); //$NON-NLS-1$
    }

    @Test
    public void testMapFloatPosThetaDoubleDouble() {
        //fail("Not yet implemented"); //$NON-NLS-1$
    }

    @Test
    public void testMapFloatVehicle() {
        //fail("Not yet implemented"); //$NON-NLS-1$
    }
}
