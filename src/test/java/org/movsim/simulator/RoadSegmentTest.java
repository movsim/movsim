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

package org.movsim.simulator;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Iterator;

import org.junit.Test;
import org.movsim.simulator.vehicles.Vehicle;

/**
 * Test module for the RoadSegment class.
 */
public class RoadSegmentTest {
    private double delta = 0.00001;

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

    // private LaneChangeModel laneChangeModel = TrafficSource.newLaneChangeModel(Vehicle.Type.CAR);
//    private static final IDM idm = new IDM(33.0, 0.5, 3.0, 1.5, 2.0, 5.0);

    private Vehicle newVehicle(double rearPosition, double speed, int lane) {
        // Vehicle(type, pos, vel, lane, ldm, lcm, length, width, color);
        //return new Vehicle(Vehicle.Type.NONE, pos, vel, lane, idm, null, 5.0, 2.5, 3);
        return new Vehicle(rearPosition, speed, lane, 5.0, 2.5);
    }

//    private LaneChangeModel newLaneChangeModel() {
//        return new LcmMOBIL(ConstantsLaneChange.GAP_MIN_FRONT_CAR,
//                ConstantsLaneChange.GAP_MIN_REAR_CAR, ConstantsLaneChange.MAX_SAFE_BRAKING_CAR,
//                ConstantsLaneChange.POLITENESS_CAR, ConstantsLaneChange.THRESHOLD_CAR,
//                ConstantsLaneChange.BIAS_INSIDE_LANE_CAR);
//    }

    /**
     * Test method for {@link org.movsim.traffic.RoadSegment#resetNextId()}
     */
    @Test
    public final void testResetNextId() {
        final double roadLength = 1000.0;
        final int laneCount = 1;
        RoadSegment.resetNextId();
        RoadSegment roadSegment = new RoadSegment(roadLength, laneCount);
        assertEquals(RoadSegment.INITIAL_ID, roadSegment.id());
        roadSegment = new RoadSegment(roadLength, laneCount);
        assertEquals(RoadSegment.INITIAL_ID + 1, roadSegment.id());
        RoadSegment.resetNextId();
        roadSegment = new RoadSegment(roadLength, laneCount);
        assertEquals(RoadSegment.INITIAL_ID, roadSegment.id());
    }

    /**
     * Test method for {@link org.movsim.traffic.RoadSegment#RoadSegment(double, int)}
     */
    @Test
    public final void testRoadSegmentDoubleInt() {
        final double roadLength = 1000.0;
        final int laneCount = 3;
        RoadSegment.resetNextId();
        final RoadSegment roadSegment = new RoadSegment(roadLength, laneCount);
        assertEquals(roadLength, roadSegment.roadLength(), delta);
        assertEquals(laneCount, roadSegment.laneCount());
        assertEquals(null, roadSegment.source());
        assertEquals(null, roadSegment.sink());
    }

    /**
     * Test method for
     * {@link org.movsim.traffic.RoadSegment#RoadSegment(org.movsim.traffic.RoadMapping)}
     */
    @Test
    public final void testRoadSegmentRoadMapping() {
        final int laneCount = 1;
        final double roadLength = 1000.0;
        final RoadMapping m = new RoadMappingConcrete(laneCount, roadLength);
        final RoadSegment r = new RoadSegment(m);
        assertEquals(roadLength, r.roadLength(), delta);
        assertEquals(laneCount, r.laneCount());
        assertEquals(null, r.source());
        assertEquals(null, r.sink());
    }

    /**
     * Test method for
     * {@link org.movsim.traffic.RoadSegment#RoadSegment(org.movsim.traffic.RoadMapping, org.movsim.traffic.TrafficSource)}
     */
    @Test
    public final void testRoadSegmentRoadMappingTrafficSource() {
        // fail("Not yet implemented"); //$NON-NLS-1$
    }

    /**
     * Test method for {@link org.movsim.traffic.RoadSegment#addDefaultSink()}
     */
    @Test
    public final void testSetDefaultSink() {
        // fail("Not yet implemented"); //$NON-NLS-1$
    }

    /**
     * Test method for {@link org.movsim.traffic.RoadSegment#id()}
     */
    @Test
    public final void testId() {
        // fail("Not yet implemented"); //$NON-NLS-1$
    }

    /**
     * Test method for {@link org.movsim.traffic.RoadSegment#roadMapping()}
     */
    @Test
    public final void testRoadMapping() {
        // fail("Not yet implemented"); //$NON-NLS-1$
    }

    /**
     * Test method for {@link org.movsim.traffic.RoadSegment#source()}
     */
    @Test
    public final void testSource() {
        // fail("Not yet implemented"); //$NON-NLS-1$
    }

    /**
     * Test method for
     * {@link org.movsim.traffic.RoadSegment#setSource(org.movsim.traffic.TrafficSource)}
     */
    @Test
    public final void testSetSource() {
        // fail("Not yet implemented"); //$NON-NLS-1$
    }

    /**
     * Test method for {@link org.movsim.traffic.RoadSegment#sink()}
     */
    @Test
    public final void testSink() {
        // fail("Not yet implemented"); //$NON-NLS-1$
    }

    /**
     * Test method for
     * {@link org.movsim.traffic.RoadSegment#setSink(org.movsim.traffic.TrafficFlowBase)}
     */
    @Test
    public final void testSetSink() {
        // fail("Not yet implemented"); //$NON-NLS-1$
    }

    /**
     * Test method for {@link org.movsim.traffic.RoadSegment#trafficLaneMin()}
     */
    @Test
    public final void testTrafficLaneMin() {
        // fail("Not yet implemented"); //$NON-NLS-1$
    }

    /**
     * Test method for {@link org.movsim.traffic.RoadSegment#trafficLaneMax()}
     */
    @Test
    public final void testTrafficLaneMax() {
        // fail("Not yet implemented"); //$NON-NLS-1$
    }

    /**
     * Test method for
     * {@link org.movsim.traffic.RoadSegment#setSourceRoadSegmentForLane(org.movsim.traffic.RoadSegment, int)}
     */
    @Test
    public final void testSetSourceRoadSegmentForLane() {
        // fail("Not yet implemented"); //$NON-NLS-1$
    }

    /**
     * Test method for {@link org.movsim.traffic.RoadSegment#sourceRoadSegment(int)}
     */
    @Test
    public final void testSourceRoadSegment() {
        // fail("Not yet implemented"); //$NON-NLS-1$
    }

    /**
     * Test method for {@link org.movsim.traffic.RoadSegment#setSourceLaneForLane(int, int)}
     */
    @Test
    public final void testSetSourceLaneForLane() {
        // fail("Not yet implemented"); //$NON-NLS-1$
    }

    /**
     * Test method for {@link org.movsim.traffic.RoadSegment#sourceLane(int)}
     */
    @Test
    public final void testSourceLane() {
        // fail("Not yet implemented"); //$NON-NLS-1$
    }

    /**
     * Test method for
     * {@link org.movsim.traffic.RoadSegment#setSinkRoadSegmentForLane(org.movsim.traffic.RoadSegment, int)}
     */
    @Test
    public final void testSetSinkRoadSegmentForLane() {
        // fail("Not yet implemented"); //$NON-NLS-1$
    }

    /**
     * Test method for {@link org.movsim.traffic.RoadSegment#sinkRoadSegment(int)}
     */
    @Test
    public final void testSinkRoadSegment() {
        // fail("Not yet implemented"); //$NON-NLS-1$
    }

    /**
     * Test method for {@link org.movsim.traffic.RoadSegment#setSinkLaneForLane(int, int)}
     */
    @Test
    public final void testSetSinkLaneForLane() {
        // fail("Not yet implemented"); //$NON-NLS-1$
    }

    /**
     * Test method for {@link org.movsim.traffic.RoadSegment#sinkLane(int)}
     */
    @Test
    public final void testSinkLane() {
        // fail("Not yet implemented"); //$NON-NLS-1$
    }

    /**
     * Test method for
     * {@link org.movsim.traffic.RoadSegment#addInhomogeneity(org.movsim.traffic.Inhomogeneity)}
     */
    @Test
    public final void testAddInhomogeneity() {
        // fail("Not yet implemented"); //$NON-NLS-1$
    }

    /**
     * Test method for {@link org.movsim.traffic.RoadSegment#inhomogeneities()}
     */
    @Test
    public final void testInhomogeneities() {
        // fail("Not yet implemented"); //$NON-NLS-1$
    }

    /**
     * Test method for
     * {@link org.movsim.traffic.RoadSegment#setGradientProfile(org.movsim.traffic.GradientProfile)}
     */
    @Test
    public final void testSetGradientProfile() {
        // fail("Not yet implemented"); //$NON-NLS-1$
    }

    /**
     * Test method for {@link org.movsim.traffic.RoadSegment#roadLength()}
     */
    @Test
    public final void testRoadLength() {
        final int laneCount = 1;
        RoadSegment r = new RoadSegment(1000.0, laneCount);
        assertEquals(1000.0, r.roadLength(), delta);
        r = new RoadSegment(1234.5, laneCount);
        assertEquals(1234.5, r.roadLength(), delta);
    }

    /**
     * Test method for {@link org.movsim.traffic.RoadSegment#laneCount()}
     */
    @Test
    public final void testLaneCount() {
        final double roadLength = 1000.0;
        RoadSegment r = new RoadSegment(roadLength, 1);
        assertEquals(1, r.laneCount());
        r = new RoadSegment(roadLength, 2);
        assertEquals(2, r.laneCount());
        r = new RoadSegment(roadLength, 3);
        assertEquals(3, r.laneCount());
        r = new RoadSegment(roadLength, 4);
        assertEquals(4, r.laneCount());
    }

    /**
     * Test method for {@link org.movsim.traffic.RoadSegment#testCar()}
     */
    @Test
    public final void testTestCar() {
        // fail("Not yet implemented"); //$NON-NLS-1$
    }

    /**
     * Test method for {@link org.movsim.traffic.RoadSegment#clearVehicles()}
     */
    @Test
    public final void testClearVehicles() {
        final double roadLength = 1000.0;
        final int laneCount = 2;
        RoadSegment.resetNextId();
        final RoadSegment roadSegment = new RoadSegment(roadLength, laneCount);
        assertEquals(0, roadSegment.totalVehicleCount());
        roadSegment.addVehicle(newVehicle(900.0, 0.0, Lane.LANE1));
        assertEquals(1, roadSegment.totalVehicleCount());
        roadSegment.addVehicle(newVehicle(800.0, 0.0, Lane.LANE2));
        assertEquals(2, roadSegment.totalVehicleCount());
        roadSegment.addVehicle(newVehicle(700.0, 0.0, Lane.LANE1));
        assertEquals(3, roadSegment.totalVehicleCount());
        roadSegment.addVehicle(newVehicle(600.0, 0.0, Lane.LANE2));
        assertEquals(4, roadSegment.totalVehicleCount());
        roadSegment.clearVehicles();
        assertEquals(0, roadSegment.totalVehicleCount());
        assertEquals(0, roadSegment.vehicleCount(Lane.LANE1));
        assertEquals(0, roadSegment.vehicleCount(Lane.LANE2));
    }

    /**
     * Test method for {@link org.movsim.traffic.RoadSegment#totalVehicleCount()}
     */
    @Test
    public final void testVehicleCount() {
        final double roadLength = 1000.0;
        final int laneCount = 1;
        RoadSegment.resetNextId();
        final RoadSegment roadSegment = new RoadSegment(roadLength, laneCount);
        assertEquals(0, roadSegment.totalVehicleCount());
        roadSegment.addVehicle(newVehicle(1.0, 0.0, Lane.LANE1));
        assertEquals(1, roadSegment.totalVehicleCount());
        roadSegment.addVehicle(newVehicle(11.0, 0.0, Lane.LANE1));
        assertEquals(2, roadSegment.totalVehicleCount());
        roadSegment.clearVehicles();
        assertEquals(0, roadSegment.totalVehicleCount());
    }

    /**
     * Test method for {@link org.movsim.traffic.RoadSegment#getVehicle(int, int)}
     */
    @Test
    public final void testGetVehicle() {
        // fail("Not yet implemented"); //$NON-NLS-1$
    }

    /**
     * Test method for
     * {@link org.movsim.traffic.RoadSegment#addObstacle(org.movsim.traffic.Obstacle)}
     */
    @Test
    public final void testAddObstacle() {
        // fail("Not yet implemented"); //$NON-NLS-1$
    }

    /**
     * Test method for
     * {@link org.movsim.traffic.RoadSegment#addVehicle(org.movsim.traffic.Vehicle)}
     */
    @Test
    public final void testAddVehicleVehicle() {
        RoadSegment.resetNextId();
        Vehicle.resetNextId();

        final int laneCount = 1;
        final RoadSegment r0 = new RoadSegment(5000.0, laneCount);
        Vehicle v;

        final Vehicle v0 = newVehicle(3900.0, 1.0, Lane.LANE1);
        r0.addVehicle(v0);
        v = r0.getVehicle(Lane.LANE1, 0);
        assertEquals(v0, v);
        final Vehicle v1 = newVehicle(3700.0, 2.0, Lane.LANE1);
        r0.addVehicle(v1);
        v = r0.getVehicle(Lane.LANE1, 0);
        assertEquals(v0, v);
        v = r0.getVehicle(Lane.LANE1, 1);
        assertEquals(v1, v);
        final Vehicle v2 = newVehicle(3100.0, 3.0, Lane.LANE1);
        r0.addVehicle(v2);
        v = r0.getVehicle(Lane.LANE1, 0);
        assertEquals(v0, v);
        v = r0.getVehicle(Lane.LANE1, 1);
        assertEquals(v1, v);
        v = r0.getVehicle(Lane.LANE1, 2);
        assertEquals(v2, v);

        final Vehicle v1a = newVehicle(3500.0, 4.0, Lane.LANE1);
        r0.addVehicle(v1a);
        v = r0.getVehicle(Lane.LANE1, 0);
        assertEquals(v0, v);
        v = r0.getVehicle(Lane.LANE1, 1);
        assertEquals(v1, v);
        v = r0.getVehicle(Lane.LANE1, 2);
        assertEquals(v1a, v);
        v = r0.getVehicle(Lane.LANE1, 3);
        assertEquals(v2, v);
    }

    /**
     * Test method for org.mjbudden.traffic.RoadSegment#rearVehicleOnLane(int)
     */
    @Test
    public final void testRearVehicleOnLane() {
        RoadSegment.resetNextId();
        Vehicle.resetNextId();
        final double roadLength = 1000.0;
        final int laneCount = 1;
        final RoadSegment roadSegment = new RoadSegment(roadLength, laneCount);
        Vehicle vehicle = roadSegment.rearVehicleOnLane(Lane.LANE1);
        assertEquals(null, vehicle);

        final Vehicle v0 = newVehicle(900.0, 0.0, Lane.LANE1);
        roadSegment.addVehicle(v0);
        vehicle = roadSegment.rearVehicleOnLane(Lane.LANE1);
        assertEquals(v0.getId(), vehicle.getId());

        final Vehicle v1 = newVehicle(800.0, 0.0, Lane.LANE1);
        roadSegment.addVehicle(v1);
        vehicle = roadSegment.rearVehicleOnLane(Lane.LANE1);
        assertEquals(v1.getId(), vehicle.getId());
    }

    /**
     * Test method for org.mjbudden.traffic.RoadSegment#rearVehicle(int, double)
     */
    @Test
    public final void testRearVehicle() {
        RoadSegment.resetNextId();
        Vehicle.resetNextId();
        final double roadLength = 1000.0;
        final int laneCount = 1;
        final RoadSegment roadSegment = new RoadSegment(roadLength, laneCount);

        final Vehicle v0 = newVehicle(900.0, 1.0, Lane.LANE1);
        roadSegment.addVehicle(v0);
        Vehicle rV = roadSegment.rearVehicle(Lane.LANE1, 901.0);
        assertEquals(v0, rV);
        rV = roadSegment.rearVehicle(Lane.LANE1, 900.0);
        assertEquals(v0, rV);
        rV = roadSegment.rearVehicle(Lane.LANE1, 899.0);
        assertEquals(null, rV);
        rV = roadSegment.rearVehicle(Lane.LANE1, 0.0);
        assertEquals(null, rV);

        final Vehicle v1 = newVehicle(800.0, 2.0, Lane.LANE1);
        roadSegment.addVehicle(v1);
        rV = roadSegment.rearVehicle(Lane.LANE1, 900.0);
        assertEquals(v0, rV);
        rV = roadSegment.rearVehicle(Lane.LANE1, 800.0);
        assertEquals(v1, rV);
        rV = roadSegment.rearVehicle(Lane.LANE1, 901.0);
        assertEquals(v0, rV);
        rV = roadSegment.rearVehicle(Lane.LANE1, 899.0);
        assertEquals(v1, rV);
        rV = roadSegment.rearVehicle(Lane.LANE1, 801.0);
        assertEquals(v1, rV);
        rV = roadSegment.rearVehicle(Lane.LANE1, 799.0);
        assertEquals(null, rV);
        rV = roadSegment.rearVehicle(Lane.LANE1, 0.0);
        assertEquals(null, rV);

        final Vehicle v2 = newVehicle(700.0, 3.0, Lane.LANE1);
        roadSegment.addVehicle(v2);
        assert roadSegment.eachLaneIsSorted();
        rV = roadSegment.rearVehicle(Lane.LANE1, v0.getPosition());
        assertEquals(v0, rV);
        rV = roadSegment.rearVehicle(Lane.LANE1, v1.getPosition());
        assertEquals(v1, rV);
        rV = roadSegment.rearVehicle(Lane.LANE1, v2.getPosition());
        assertEquals(v2, rV);
        rV = roadSegment.rearVehicle(Lane.LANE1, 901.0);
        assertEquals(v0, rV);
        rV = roadSegment.rearVehicle(Lane.LANE1, 899.0);
        assertEquals(v1, rV);
        rV = roadSegment.rearVehicle(Lane.LANE1, 801.0);
        assertEquals(v1, rV);
        rV = roadSegment.rearVehicle(Lane.LANE1, 799.0);
        assertEquals(v2, rV);
        rV = roadSegment.rearVehicle(Lane.LANE1, 701.0);
        assertEquals(v2, rV);
        rV = roadSegment.rearVehicle(Lane.LANE1, 699.0);
        assertEquals(null, rV);
        rV = roadSegment.rearVehicle(Lane.LANE1, 0.0);
        assertEquals(null, rV);
    }

    /**
     * Test method for org.mjbudden.traffic.RoadSegment#frontVehicle(int, double)
     */
    @Test
    public final void testRearVehicleJoin() {
        RoadSegment.resetNextId();
        Vehicle.resetNextId();

        final int laneCount = 1;
        final RoadSegment r0 = new RoadSegment(700.0, laneCount);
        final RoadSegment r1 = new RoadSegment(5100.0, laneCount);
        // join r0 and r1 so vehicles move from r0 to r1
        Link.addJoin(r0, r1);

        final Vehicle v0 = newVehicle(3900.0, 1.0, Lane.LANE1);
        assertEquals(3900.0, v0.posRearBumper(), delta);
        r1.addVehicle(v0);
        final Vehicle v1 = newVehicle(3700.0, 2.0, Lane.LANE1);
        r1.addVehicle(v1);
        final Vehicle v2 = newVehicle(3100.0, 3.0, Lane.LANE1);
        r1.addVehicle(v2);

        final Vehicle v3 = newVehicle(600.0, 4.0, Lane.LANE1);
        r0.addVehicle(v3);
        final Vehicle v4 = newVehicle(500.0, 5.0, Lane.LANE1);
        r0.addVehicle(v4);

        Vehicle rV = r1.rearVehicle(Lane.LANE1, 3901.0);
        assertEquals(v0, rV);
        rV = r1.rearVehicle(Lane.LANE1, 3900.0);
        assertEquals(v0, rV);
        rV = r1.rearVehicle(Lane.LANE1, 3700.0);
        assertEquals(v1, rV);
        rV = r1.rearVehicle(Lane.LANE1, 3101.0);
        assertEquals(v2, rV);
        rV = r1.rearVehicle(Lane.LANE1, 3100.0);
        assertEquals(v2, rV);
        rV = r1.rearVehicle(Lane.LANE1, 3099.0);
        assertEquals(v3.getId(), rV.getId());
        assertEquals(-100.0, rV.posRearBumper(), delta); // pos relative to r1
        assertEquals(v3.getSpeed(), rV.getSpeed(), delta);

        rV = r1.rearVehicle(Lane.LANE1, 3099.0);
        assertEquals(v3.getId(), rV.getId());
        assertEquals(-100.0, rV.posRearBumper(), delta); // pos relative to r1
        rV = r0.rearVehicle(Lane.LANE1, 601.0);
        assertEquals(v3, rV);
        rV = r0.rearVehicle(Lane.LANE1, 600.0);
        assertEquals(v3, rV);
        rV = r0.rearVehicle(Lane.LANE1, 599.0);
        assertEquals(v4, rV);
        rV = r0.rearVehicle(Lane.LANE1, 501.0);
        assertEquals(v4, rV);
        rV = r0.rearVehicle(Lane.LANE1, 500.0);
        assertEquals(v4, rV);
        rV = r0.rearVehicle(Lane.LANE1, 499.0);
        assertEquals(null, rV);
    }

    /**
     * Test method for org.mjbudden.traffic.RoadSegment#rearVehicle(int, double)
     */
    @Test
    public final void testRearVehicleOffsetJoin() {
        // test rear vehicle when there is an offset join, for example a join
        // onto a road segment that has an exit lane
        RoadSegment.resetNextId();
        Vehicle.resetNextId();

        final int laneCount = 2;
        final int exitLaneCount = 1;
        final RoadSegment r0 = new RoadSegment(1000.0, laneCount);
        final RoadSegment r1 = new RoadSegment(200.0, laneCount + exitLaneCount);
        r1.setLaneType(Lane.LANE1, Lane.Type.EXIT);// so Lane1 is exit lane of r1
        // join r0 and r1 so vehicles move from r0 to r1
        // lane2 of r0 joins to lane3 of r1
        // lane1 of r0 joins to lane2 of r1
        // lane1 of r1 has no predecessor
        Link.addJoin(r0, r1);
        assert r1.sourceRoadSegment(Lane.LANE3).id() == r0.id();
        assert r1.sourceRoadSegment(Lane.LANE2).id() == r0.id();
        assert r1.sourceRoadSegment(Lane.LANE1) == null;

        // vehicles suffixed 0 are on r0, vehicles suffixed 1 are on r1
        final Vehicle z1 = newVehicle(5.0, 1.0, Lane.LANE3);
        r1.addVehicle(z1);
        final Vehicle z0 = newVehicle(996.0, 3.0, Lane.LANE2);
        r0.addVehicle(z0);
        final Vehicle y1 = newVehicle(3.0, 4.0, Lane.LANE2);
        r1.addVehicle(y1);
        final Vehicle y0 = newVehicle(998.0, 5.0, Lane.LANE1);
        r0.addVehicle(y0);
        // vehicle in exit lane
        final Vehicle x1 = newVehicle(5.0, 5.0, Lane.LANE1);
        r1.addVehicle(x1);

        Vehicle rV = r1.rearVehicle(Lane.LANE3, 6.0);
        assertEquals(z1, rV);
        rV = r1.rearVehicle(Lane.LANE3, 5.0);
        assertEquals(z1, rV);
        rV = r1.rearVehicle(Lane.LANE3, 4.0);
        assertEquals(z0.getId(), rV.getId());
        assertEquals(-4.0, rV.posRearBumper(), delta);
        assertEquals(3.0, rV.getSpeed(), delta);
        rV = r1.rearVehicle(Lane.LANE3, 3.0);
        assertEquals(z0.getId(), rV.getId());

        rV = r1.rearVehicle(Lane.LANE2, 4.0);
        assertEquals(y1, rV);
        rV = r1.rearVehicle(Lane.LANE2, 3.0);
        assertEquals(y1, rV);
        rV = r1.rearVehicle(Lane.LANE2, 2.0);
        assert rV != null;
        assertEquals(y0.getId(), rV.getId());
        assertEquals(-2.0, rV.posRearBumper(), delta);
        assertEquals(5.0, rV.getSpeed(), delta);
        rV = r1.rearVehicle(Lane.LANE2, 1.0);
        assertEquals(y0.getId(), rV.getId());
    }

    /**
     * Test method for {@link org.movsim.traffic.RoadSegment#frontVehicleOnLane(int)}
     * Vehicles are sorted in order of decreasing position:
     * start end
     * V(n+1).pos < V(n).pos < V(n-1).pos ... < V(1).pos < V(0).pos
     * 
     * The front vehicle is the one nearest the start of the road.
     */
    @Test
    public final void testFrontVehicleOnLane() {
        RoadSegment.resetNextId();
        Vehicle.resetNextId();
        final double roadLength = 1000.0;
        final int laneCount = 1;
        final RoadSegment roadSegment = new RoadSegment(roadLength, laneCount);
        Vehicle vehicle = roadSegment.frontVehicleOnLane(Lane.LANE1);
        assertEquals(null, vehicle);

        final Vehicle v0 = newVehicle(900.0, 0.0, Lane.LANE1);
        roadSegment.addVehicle(v0);
        vehicle = roadSegment.frontVehicleOnLane(Lane.LANE1);
        assertEquals(v0.getId(), vehicle.getId());

        final Vehicle v1 = newVehicle(800.0, 0.0, Lane.LANE1);
        roadSegment.addVehicle(v1);
        vehicle = roadSegment.frontVehicleOnLane(Lane.LANE1);
        assertEquals(v0.getId(), vehicle.getId());
    }

    /**
     * Test method for org.mjbudden.traffic.RoadSegment#frontVehicle(int, int)
     */
    @Test
    public final void testFrontVehicle() {
        RoadSegment.resetNextId();
        final double roadLength = 1000.0;
        final int laneCount = 1;
        final RoadSegment roadSegment = new RoadSegment(roadLength, laneCount);

        Vehicle.resetNextId();
        final Vehicle v0 = newVehicle(900.0, 1.0, Lane.LANE1);
        roadSegment.addVehicle(v0);
        Vehicle fV = roadSegment.frontVehicle(Lane.LANE1, 900.0);
        assertEquals(null, fV);
        fV = roadSegment.frontVehicle(Lane.LANE1, 1000.0);
        assertEquals(null, fV);
        fV = roadSegment.frontVehicle(Lane.LANE1, 901.0);
        assertEquals(null, fV);
        fV = roadSegment.frontVehicle(Lane.LANE1, 899.0);
        assertEquals(v0, fV);
        fV = roadSegment.frontVehicle(Lane.LANE1, 0.0);
        assertEquals(v0, fV);

        final Vehicle v1 = newVehicle(800.0, 2.0, Lane.LANE1);
        roadSegment.addVehicle(v1);
        fV = roadSegment.frontVehicle(Lane.LANE1, 900.0);
        assertEquals(null, fV);
        fV = roadSegment.frontVehicle(Lane.LANE1, 800.0);
        assertEquals(v0, fV);
        fV = roadSegment.frontVehicle(Lane.LANE1, 901.0);
        assertEquals(null, fV);
        fV = roadSegment.frontVehicle(Lane.LANE1, 899.0);
        assertEquals(v0, fV);
        fV = roadSegment.frontVehicle(Lane.LANE1, 801.0);
        assertEquals(v0, fV);
        fV = roadSegment.frontVehicle(Lane.LANE1, 799.0);
        assertEquals(v1, fV);
        fV = roadSegment.frontVehicle(Lane.LANE1, 0.0);
        assertEquals(v1, fV);

        final Vehicle v2 = newVehicle(700.0, 3.0, Lane.LANE1);
        roadSegment.addVehicle(v2);
        fV = roadSegment.frontVehicle(Lane.LANE1, v0.getPosition());
        assertEquals(null, fV);
        fV = roadSegment.frontVehicle(Lane.LANE1, v1.getPosition());
        assertEquals(v0, fV);
        fV = roadSegment.frontVehicle(Lane.LANE1, v2.getPosition());
        assertEquals(v1, fV);
        fV = roadSegment.frontVehicle(Lane.LANE1, 901.0);
        assertEquals(null, fV);
        fV = roadSegment.frontVehicle(Lane.LANE1, 899.0);
        assertEquals(v0, fV);
        fV = roadSegment.frontVehicle(Lane.LANE1, 801.0);
        assertEquals(v0, fV);
        fV = roadSegment.frontVehicle(Lane.LANE1, 799.0);
        assertEquals(v1, fV);
        fV = roadSegment.frontVehicle(Lane.LANE1, 701.0);
        assertEquals(v1, fV);
        fV = roadSegment.frontVehicle(Lane.LANE1, 699.0);
        assertEquals(v2, fV);
        fV = roadSegment.frontVehicle(Lane.LANE1, 0.0);
        assertEquals(v2, fV);
    }

    /**
     * Test method for org.mjbudden.traffic.RoadSegment#frontVehicle(int, int)
     */
    @Test
    public final void testFrontVehicleJoin() {
        RoadSegment.resetNextId();
        Vehicle.resetNextId();

        final int laneCount = 1;
        final RoadSegment r0 = new RoadSegment(700.0, laneCount);
        final RoadSegment r1 = new RoadSegment(5100.0, laneCount);
        // join r0 and r1 so vehicles move from r0 to r1
        Link.addJoin(r0, r1); // r0=source, r1=sink

        final Vehicle v0 = newVehicle(3900.0, 1.0, Lane.LANE1);
        r1.addVehicle(v0);
        final Vehicle v1 = newVehicle(3700.0, 2.0, Lane.LANE1);
        r1.addVehicle(v1);
        final Vehicle v2 = newVehicle(3100.0, 3.0, Lane.LANE1);
        r1.addVehicle(v2);

        final Vehicle v3 = newVehicle(600.0, 4.0, Lane.LANE1);
        r0.addVehicle(v3);
        final Vehicle v4 = newVehicle(500.0, 5.0, Lane.LANE1);
        r0.addVehicle(v4);

        Vehicle fV = r1.frontVehicle(Lane.LANE1, v0.getPosition());
        assertEquals(null, fV);
        fV = r1.frontVehicle(Lane.LANE1, r1.roadLength());
        assertEquals(null, fV);
        fV = r1.frontVehicle(Lane.LANE1, v1.getPosition());
        assertEquals(v0, fV);
        fV = r1.frontVehicle(Lane.LANE1, v2.getPosition());
        assertEquals(v1, fV);

        // vehicle in front of end of road0 is v2
        fV = r0.frontVehicle(Lane.LANE1, r0.roadLength());
        assertEquals(v2.getId(), fV.getId());
        assertEquals(v2.getPosition() + r0.roadLength(), fV.getPosition(), delta);
        assertEquals(v2.getSpeed(), fV.getSpeed(), delta);
        // vehicle in front of v3 is v2
        fV = r0.frontVehicle(Lane.LANE1, v3.getPosition());
        assertEquals(v2.getId(), fV.getId());
        fV = r0.frontVehicle(Lane.LANE1, v4.getPosition());
        assertEquals(v3, fV);
        fV = r0.frontVehicle(Lane.LANE1, 0.0);
        assertEquals(v4, fV);
    }

    /**
     * Test method for org.mjbudden.traffic.RoadSegment#rearVehicle(int, double)
     */
    @Test
    public final void testFrontVehicleOffsetJoin() {
        // test front vehicle when there is an offset join, for example a join
        // onto a road segment that has an exit lane
        RoadSegment.resetNextId();
        Vehicle.resetNextId();

        final int laneCount = 2;
        final int exitLaneCount = 1;
        final RoadSegment r0 = new RoadSegment(1000.0, laneCount);
        final RoadSegment r1 = new RoadSegment(200.0, laneCount + exitLaneCount);
        r1.setLaneType(Lane.LANE1, Lane.Type.EXIT);// so Lane1 is exit lane of r1
        // join r0 and r1 so vehicles move from r0 to r1
        // lane2 of r0 joins to lane3 of r1
        // lane1 of r0 joins to lane2 of r1
        // lane1 of r1 has no predecessor
        Link.addJoin(r0, r1);

        // vehicles suffixed 0 are on r0, vehicles suffixed 1 are on r1
        final Vehicle z1 = newVehicle(5.0, 1.0, Lane.LANE3);
        r1.addVehicle(z1);
        final Vehicle z0 = newVehicle(996.0, 3.0, Lane.LANE2);
        r0.addVehicle(z0);
        final Vehicle y1 = newVehicle(3.0, 4.0, Lane.LANE2);
        r1.addVehicle(y1);
        final Vehicle y0 = newVehicle(998.0, 5.0, Lane.LANE1);
        r0.addVehicle(y0);
        // vehicle in exit lane
        final Vehicle x1 = newVehicle(5.0, 5.0, Lane.LANE1);
        r1.addVehicle(x1);

        Vehicle fV = r0.frontVehicle(Lane.LANE2, 995.0);
        assertEquals(z0, fV);
        fV = r0.frontVehicle(Lane.LANE2, 996.0);
        assertEquals(z1.getId(), fV.getId());
        assertEquals(1005.0, fV.posRearBumper(), delta);
        assertEquals(1.0, fV.getSpeed(), delta);
        fV = r0.frontVehicle(Lane.LANE2, 997.0);
        assertEquals(z1.getId(), fV.getId());

        fV = r0.frontVehicle(Lane.LANE1, 997.0);
        assertEquals(y0, fV);
        fV = r0.frontVehicle(Lane.LANE1, 998.0);
        assertEquals(y1.getId(), fV.getId());
        assertEquals(1003.0, fV.posRearBumper(), delta);
        assertEquals(4.0, fV.getSpeed(), delta);
        fV = r0.frontVehicle(Lane.LANE1, 999.0);
        assertEquals(y1.getId(), fV.getId());
    }

    /**
     * Test method for {@link org.movsim.traffic.RoadSegment#makeLaneChanges(double, double, long)}
     */
    @Test
    public final void testMakeLaneChanges() {
        // fail("Not yet implemented"); //$NON-NLS-1$
//        RoadSegment.resetNextId();
//        Vehicle.resetNextId();
//
//        final int laneCount = 2;
//        final RoadSegment r0 = new RoadSegment(1000.0, laneCount);
//
//        // set up an obstacle directly in front of a vehicle, so the vehicle will change lanes
//        // Obstacle(pos, lane, length, width, color) {
//        final Obstacle obstacle = new Obstacle(600.0, Lane.LANE1, 1.0, 1.0, 0);
//        r0.addVehicle(obstacle);
//        final Vehicle v0 = newVehicle(593.0, 5.0, Lane.LANE1);
//        final LaneChangeModel lcm = newLaneChangeModel();
//        v0.setLaneChangeModel(lcm);
//        r0.addVehicle(v0);
//        final double dt = 0.25;
//        final double simulationTime = 0.0;
//        final long iterationCount = 0;
//        r0.makeLaneChanges(dt, simulationTime, iterationCount);
//        assertEquals(Lane.LANE1, obstacle.getLane());
//        assertEquals(Lane.LANE2, v0.getLane());
//        assertEquals(1, r0.vehicleCount(Lane.LANE1));
//        assertEquals(1, r0.vehicleCount(Lane.LANE2));
    }

    /**
     * Test method for
     * {@link org.movsim.traffic.RoadSegment#updateVehiclePositionsAndVelocities(double, double, long)}
     */
    @Test
    public final void testUpdateVehiclePositionsAndVelocities() {
//        RoadSegment.resetNextId();
//        Vehicle.resetNextId();
//        Vehicle.setIntegrationType(Vehicle.IntegrationType.EULER);
//        final double roadLength = 1000.0;
//        final int laneCount = 1;
//        final RoadSegment roadSegment = new RoadSegment(roadLength, laneCount);
//
//        final Vehicle v0 = newVehicle(900.0, 10.0, Lane.LANE1);
//        roadSegment.addVehicle(v0);
//        final Vehicle v1 = newVehicle(800.0, 20.0, Lane.LANE1);
//        roadSegment.addVehicle(v1);
//        final Vehicle v2 = newVehicle(700.0, 30.0, Lane.LANE1);
//        roadSegment.addVehicle(v2);
//
//        final double dt = 0.25;
//        roadSegment.updateVehiclePositionsAndVelocities(dt, 0.0, 0);
//        assertEquals(v0.getPosition(), 902.5, delta);
//        assertEquals(v1.getPosition(), 805.0, delta);
//        assertEquals(v2.getPosition(), 707.5, delta);
    }

    /**
     * Test method for
     * {@link org.movsim.traffic.RoadSegment#updateVehiclePositionsAndVelocities(double, double, long)}
     */
    @Test
    public final void testUpdateVehiclePositionsAndVelocitiesJoin() {
//        RoadSegment.resetNextId();
//        Vehicle.resetNextId();
//        Vehicle.setIntegrationType(Vehicle.IntegrationType.EULER);
//
//        final int laneCount = 1;
//        final RoadSegment r0 = new RoadSegment(700.0, laneCount);
//        final RoadSegment r1 = new RoadSegment(5100.0, laneCount);
//        // join r0 and r1 so vehicles move from r0 to r1
//        Link.addJoin(r0, r1);
//
//        final Vehicle v0 = newVehicle(3900.0, 10.0, Lane.LANE1);
//        r1.addVehicle(v0);
//        final Vehicle v1 = newVehicle(3700.0, 20.0, Lane.LANE1);
//        r1.addVehicle(v1);
//        final Vehicle v2 = newVehicle(3100.0, 30.0, Lane.LANE1);
//        r1.addVehicle(v2);
//
//        final Vehicle v3 = newVehicle(695.0, 40.0, Lane.LANE1);
//        r0.addVehicle(v3);
//        final Vehicle v4 = newVehicle(500.0, 50.0, Lane.LANE1);
//        r0.addVehicle(v4);
//
//        final double dt = 0.25;
//        final double simulationTime = 0.0;
//        final long iterationCount = 0;
//        r0.updateVehiclePositionsAndVelocities(dt, simulationTime, iterationCount);
//        assertEquals(v3.getPosition(), 705.0, delta);
//        assertEquals(v4.getPosition(), 512.5, delta);
//
//        r1.updateVehiclePositionsAndVelocities(dt, simulationTime, iterationCount);
//        assertEquals(v0.getPosition(), 3902.5, delta);
//        assertEquals(v1.getPosition(), 3705.0, delta);
//        assertEquals(v2.getPosition(), 3107.5, delta);
//        // check has been moved to new road segment by outflow
//        r0.outFlow(dt, simulationTime, iterationCount);
//        r1.outFlow(dt, simulationTime, iterationCount);
//        assertEquals(v3.roadSegmentId(), r1.id());
//        assertEquals(v3.getPosition(), 5.0, delta);
    }

    /**
     * Test method for
     * {@link org.movsim.traffic.RoadSegment#updateVehiclePositionsAndVelocities(double, double, long)}
     */
    @SuppressWarnings("boxing")
    @Test
    public final void testUpdateVehiclePositionsAndVelocitiesSelfJoin() {
//        RoadSegment.resetNextId();
//        Vehicle.resetNextId();
//        Vehicle.setIntegrationType(Vehicle.IntegrationType.EULER);
//
//        final int laneCount = 1;
//        final RoadSegment r0 = new RoadSegment(3900.0, laneCount);
//        // join r0 and r1 so vehicles move from r0 to r1
//        Link.addJoin(r0, r0);
//
//        final Vehicle v0 = newVehicle(3895.0, 80.0, Lane.LANE1);
//        r0.addVehicle(v0);
//        final Vehicle v1 = newVehicle(3700.0, 20.0, Lane.LANE1);
//        r0.addVehicle(v1);
//        final Vehicle v2 = newVehicle(3100.0, 30.0, Lane.LANE1);
//        r0.addVehicle(v2);
//
//        final Vehicle v3 = newVehicle(695.0, 40.0, Lane.LANE1);
//        r0.addVehicle(v3);
//        final Vehicle v4 = newVehicle(500.0, 50.0, Lane.LANE1);
//        r0.addVehicle(v4);
//
//        final double dt = 0.25;
//        final double simulationTime = 0.0;
//        final long iterationCount = 0;
//        r0.updateVehiclePositionsAndVelocities(dt, simulationTime, iterationCount);
//        assertEquals(true, r0.eachLaneIsSorted());
//        r0.outFlow(dt, simulationTime, iterationCount);
//
//        assertEquals(v0.getPosition(), 15.0, delta);
//        assertEquals(v1.getPosition(), 3705.0, delta);
//        assertEquals(v2.getPosition(), 3107.5, delta);
//        assertEquals(v3.getPosition(), 705.0, delta);
//        assertEquals(v4.getPosition(), 512.5, delta);
//        assertEquals(true, r0.eachLaneIsSorted());
    }

    /**
     * Test method for
     * {@link org.movsim.traffic.RoadSegment#updateVehiclePositionsAndVelocities(double, double, long)}
     */
    @SuppressWarnings("boxing")
    @Test
    public final void testUpdateVehiclePositionsAndVelocitiesCalc() {
//        RoadSegment.resetNextId();
//        Vehicle.resetNextId();
//        Vehicle.setIntegrationType(Vehicle.IntegrationType.EULER);
//
//        final int laneCount = 1;
//        final RoadSegment r0 = new RoadSegment(100000.0, laneCount);
//        final int vehicleCount = 5;
//        final ArrayList<Vehicle> vehicles = new ArrayList<Vehicle>(vehicleCount);
//
//        final Vehicle v0 = newVehicle(3895.0, 80.0, Lane.LANE1);
//        r0.addVehicle(v0);
//        final Vehicle v1 = newVehicle(3700.0, 20.0, Lane.LANE1);
//        r0.addVehicle(v1);
//        final Vehicle v2 = newVehicle(3100.0, 30.0, Lane.LANE1);
//        r0.addVehicle(v2);
//        final Vehicle v3 = newVehicle(695.0, 40.0, Lane.LANE1);
//        r0.addVehicle(v3);
//        final Vehicle v4 = newVehicle(500.0, 50.0, Lane.LANE1);
//        r0.addVehicle(v4);
//
//        final Vehicle w0 = new Vehicle(v0);
//        vehicles.add(w0);
//        final Vehicle w1 = new Vehicle(v1);
//        vehicles.add(w1);
//        final Vehicle w2 = new Vehicle(v2);
//        vehicles.add(w2);
//        final Vehicle w3 = new Vehicle(v3);
//        vehicles.add(w3);
//        final Vehicle w4 = new Vehicle(v4);
//        vehicles.add(w4);
//        assertEquals(true, r0.eachLaneIsSorted());
//
//        final double dt = 0.25;
//        final double simulationTime = 0.0;
//        final long iterationCount = 0;
//        r0.updateVehiclePositionsAndVelocities(dt, simulationTime, iterationCount);
//        assertEquals(true, r0.eachLaneIsSorted());
//
//        assertEquals(v0.getPosition(), 3915.0, delta);
//        assertEquals(v1.getPosition(), 3705.0, delta);
//        assertEquals(v2.getPosition(), 3107.5, delta);
//        assertEquals(v3.getPosition(), 705.0, delta);
//        assertEquals(v4.getPosition(), 512.5, delta);
//        final int count = vehicles.size();
//        for (int i = count - 1; i >= 2; i--) {
//            final Vehicle vehicle = vehicles.get(i);
//            final Vehicle frontVehicle = vehicles.get(i - 1);
//            if (Vehicle.integrationType() == Vehicle.IntegrationType.EULER) {
//                vehicle.eulerIntegrate(dt, frontVehicle);
//            } else {
//                final Vehicle frontFrontVehicle = vehicles.get(i - 2);
//                vehicle.rungeKuttaIntegrate(dt, frontVehicle, frontFrontVehicle);
//            }
//        }
//        if (Vehicle.integrationType() == Vehicle.IntegrationType.EULER) {
//            w1.eulerIntegrate(dt, w0);
//            w0.eulerIntegrate(dt, null);
//        } else {
//            w1.rungeKuttaIntegrate(dt, w0, null);
//            w0.rungeKuttaIntegrate(dt, null, null);
//        }
//        assertEquals(w0.getPosition(), v0.getPosition(), delta);
//        assertEquals(w0.getSpeed(), v0.getSpeed(), delta);
//        assertEquals(w1.getPosition(), v1.getPosition(), delta);
//        assertEquals(w1.getSpeed(), v1.getSpeed(), delta);
//        assertEquals(w2.getPosition(), v2.getPosition(), delta);
//        assertEquals(w2.getSpeed(), v2.getSpeed(), delta);
//        assertEquals(w3.getPosition(), v3.getPosition(), delta);
//        assertEquals(w3.getSpeed(), v3.getSpeed(), delta);
//        assertEquals(w4.getPosition(), v4.getPosition(), delta);
//        assertEquals(w4.getSpeed(), v4.getSpeed(), delta);
    }

    /**
     * Test method for
     * {@link org.movsim.traffic.RoadSegment#updateVehiclePositionsAndVelocities(double, double, long)}
     */
    @SuppressWarnings("boxing")
    @Test
    public final void testUpdateVehiclePositionsAndVelocitiesMany() {
//        RoadSegment.resetNextId();
//        Vehicle.resetNextId();
//        Vehicle.setIntegrationType(Vehicle.IntegrationType.EULER);
//
//        final int laneCount = 1;
//        final int vehicleCount = 1000;
//        final ArrayList<Vehicle> vehicles = new ArrayList<Vehicle>(vehicleCount);
//        final double averageSpacing = 200.0;
//        final double averageVelocity = 90.0 / 3.6; // 90 km/h
//        final RoadSegment r0 = new RoadSegment(10 * vehicleCount * averageSpacing, laneCount);
//
//        for (int i = vehicleCount - 1; i >= 0; i--) {
//            // TODO - add random variation to pos and vel
//            final double pos = i * averageSpacing;
//            final double vel = averageVelocity;
//            final Vehicle v = newVehicle(pos, vel, Lane.LANE1);
//            r0.addVehicle(v);
//            final Vehicle w = new Vehicle(v);
//            vehicles.add(w);
//        }
//        assertEquals(true, r0.eachLaneIsSorted());
//
//        final double dt = 0.25;
//        final double simulationTime = 0.0;
//        final long iterationCount = 0;
//        r0.updateVehiclePositionsAndVelocities(dt, simulationTime, iterationCount);
//        assertEquals(true, r0.eachLaneIsSorted());
//
//        final int count = vehicles.size();
//        for (int i = count - 1; i >= 2; i--) {
//            final Vehicle vehicle = vehicles.get(i);
//            final Vehicle frontVehicle = vehicles.get(i - 1);
//            if (Vehicle.integrationType() == Vehicle.IntegrationType.EULER) {
//                vehicle.eulerIntegrate(dt, frontVehicle);
//            } else {
//                final Vehicle frontFrontVehicle = vehicles.get(i - 2);
//                vehicle.rungeKuttaIntegrate(dt, frontVehicle, frontFrontVehicle);
//            }
//        }
//        final Vehicle w1 = vehicles.get(1);
//        final Vehicle w0 = vehicles.get(0);
//        if (Vehicle.integrationType() == Vehicle.IntegrationType.EULER) {
//            w1.eulerIntegrate(dt, w0);
//            w0.eulerIntegrate(dt, null);
//        } else {
//            w1.rungeKuttaIntegrate(dt, w0, null);
//            w0.rungeKuttaIntegrate(dt, null, null);
//        }
//        for (int i = 0; i < vehicleCount; ++i) {
//            final Vehicle v = r0.getVehicle(Lane.LANE1, i);
//            final Vehicle w = vehicles.get(i);
//            assertEquals(w.getPosition(), v.getPosition(), delta);
//            assertEquals(w.getSpeed(), v.getSpeed(), delta);
//        }
    }

    /**
     * Test method for {@link org.movsim.traffic.RoadSegment#inFlow(double, double, long)}
     */
    @Test
    public final void testInFlow() {
        // fail("Not yet implemented"); //$NON-NLS-1$
    }

    /**
     * Test method for {@link org.movsim.traffic.RoadSegment#outFlow(double, double, long)}
     */
    @Test
    public final void testOutFlow() {
        RoadSegment.resetNextId();
        Vehicle.resetNextId();

        final int laneCount = 1;
        final RoadSegment r0 = new RoadSegment(1000.0, laneCount);
        final RoadSegment r1 = new RoadSegment(5000.0, laneCount);
        // join r0 and r1 so vehicles move from r0 to r1
        Link.addJoin(r0, r1);

        final Vehicle v0 = newVehicle(999.0, 40.0, Lane.LANE1);
        r0.addVehicle(v0);
        final double dt = 0.25;
        final double simulationTime = 0.0;
        final long iterationCount = 0;
        r0.updateVehiclePositionsAndVelocities(dt, simulationTime, iterationCount);
//        assertEquals(1009.0, v0.getPosition(), delta);
//        r0.outFlow(dt, simulationTime, iterationCount);
//        assertEquals(0, r0.totalVehicleCount());
//        assertEquals(1, r1.totalVehicleCount());
//        final Vehicle v = r1.getVehicle(Lane.LANE1, 0);
//        assertEquals(9.0, v.getPosition(), delta);
    }

    @Test
    public final void testOutFlowTrafficLane() {
        RoadSegment.resetNextId();
        Vehicle.resetNextId();

        final int laneCount = 3;
        final RoadSegment r0 = new RoadSegment(1000.0, laneCount);
        final RoadSegment r1 = new RoadSegment(5000.0, laneCount + 1);
        r1.setLaneType(Lane.LANE1, Lane.Type.ENTRANCE);
        // join r0 and r1 so vehicles move from r0 to r1
        Link.addJoin(r0, r1);

        final Vehicle v1 = newVehicle(999.0, 40.0, Lane.LANE1);
        r0.addVehicle(v1);
        final Vehicle v2 = newVehicle(998.0, 40.0, Lane.LANE2);
        r0.addVehicle(v2);
        final Vehicle v3 = newVehicle(997.0, 40.0, Lane.LANE3);
        r0.addVehicle(v3);

        final double dt = 0.25;
        final double simulationTime = 0.0;
        final long iterationCount = 0;
        r0.updateVehiclePositionsAndVelocities(dt, simulationTime, iterationCount);
//        assertEquals(1009.0, v1.getPosition(), delta);
//        assertEquals(1008.0, v2.getPosition(), delta);
//        assertEquals(1007.0, v3.getPosition(), delta);
//        r0.outFlow(dt, simulationTime, iterationCount);
//        assertEquals(0, r0.totalVehicleCount());
//        assertEquals(3, r1.totalVehicleCount());
//        assertEquals(Lane.LANE2, v1.getLane());
//        assertEquals(Lane.LANE3, v2.getLane());
//        assertEquals(Lane.LANE4, v3.getLane());
//        final Vehicle nv1 = r1.getVehicle(Lane.LANE2, 0);
//        assertEquals(9.0, nv1.getPosition(), delta);
//        final Vehicle nv2 = r1.getVehicle(Lane.LANE3, 0);
//        assertEquals(8.0, nv2.getPosition(), delta);
//        final Vehicle nv3 = r1.getVehicle(Lane.LANE4, 0);
//        assertEquals(7.0, nv3.getPosition(), delta);
    }

    /**
     * Test method for {@link org.movsim.traffic.RoadSegment#eachLaneIsSorted()}
     */
    @Test
    public final void testEachLaneIsSorted() {
        // fail("Not yet implemented"); //$NON-NLS-1$
    }

    /**
     * Test method for {@link org.movsim.traffic.RoadSegment#addTimeBasedDetector(double)}
     */
    @Test
    public final void testAddTimeBasedDetector() {
        // fail("Not yet implemented"); //$NON-NLS-1$
    }

    /**
     * Test method for {@link org.movsim.traffic.RoadSegment#addSpaceBasedDetector(double)}
     */
    @Test
    public final void testAddSpaceBasedDetector() {
        // fail("Not yet implemented"); //$NON-NLS-1$
    }

    /**
     * Test method for {@link org.movsim.traffic.RoadSegment#timeBasedDetector(int, int)}
     */
    @Test
    public final void testTimeBasedDetector() {
        // fail("Not yet implemented"); //$NON-NLS-1$
    }

    /**
     * Test method for {@link org.movsim.traffic.RoadSegment#spaceBasedDetector(int, int)}
     */
    @Test
    public final void testSpaceBasedDetector() {
        // fail("Not yet implemented"); //$NON-NLS-1$
    }

    /**
     * Test method for {@link org.movsim.traffic.RoadSegment#clearTimeBasedDetectors()}
     */
    @Test
    public final void testClearTimeBasedDetectors() {
        // fail("Not yet implemented"); //$NON-NLS-1$
    }

    /**
     * Test method for {@link org.movsim.traffic.RoadSegment#clearSpaceBasedDetectors()}
     */
    @Test
    public final void testClearSpaceBasedDetectors() {
        // fail("Not yet implemented"); //$NON-NLS-1$
    }

    /**
     * Test method for {@link org.movsim.traffic.RoadSegment#timeBasedDetectorCount()}
     */
    @Test
    public final void testTimeBasedDetectorCount() {
        // fail("Not yet implemented"); //$NON-NLS-1$
    }

    /**
     * Test method for {@link org.movsim.traffic.RoadSegment#spaceBasedDetectorCount()}
     */
    @Test
    public final void testSpaceBasedDetectorCount() {
        // fail("Not yet implemented"); //$NON-NLS-1$
    }

    /**
     * Test method for
     * {@link org.movsim.traffic.RoadSegment#updatePositionsCacheForSpaceDetectors()}
     */
    @Test
    public final void testUpdatePositionsCacheForSpaceDetectors() {
        // fail("Not yet implemented"); //$NON-NLS-1$
    }

    /**
     * Test method for {@link org.movsim.traffic.RoadSegment#updateDetectors(double, double, long)}
     */
    @Test
    public final void testUpdateDetectors() {
        // fail("Not yet implemented"); //$NON-NLS-1$
    }

    /**
     * Test method for {@link org.movsim.traffic.RoadSegment#iterator()}
     */
    @SuppressWarnings("boxing")
    @Test
    public final void testIterator() {
        // fail("Not yet implemented");
        RoadSegment.resetNextId();
        Vehicle.resetNextId();

        final int laneCount = 3;
        final RoadSegment r0 = new RoadSegment(1000.0, laneCount);

        final Vehicle v0 = newVehicle(800.0, 1.0, Lane.LANE2);
        r0.addVehicle(v0);
        final Vehicle v1 = newVehicle(700.0, 1.0, Lane.LANE3);
        r0.addVehicle(v1);
        final Vehicle v2 = newVehicle(600.0, 1.0, Lane.LANE3);
        r0.addVehicle(v2);
        Iterator<Vehicle> iterator = r0.iterator();
        assertEquals(true, iterator.hasNext());
        assertEquals(v0, iterator.next());
        assertEquals(true, iterator.hasNext());
        assertEquals(v1, iterator.next());
        assertEquals(true, iterator.hasNext());
        assertEquals(v2, iterator.next());
        assertEquals(false, iterator.hasNext());
    }

    @SuppressWarnings("boxing")
    @Test
    public final void testIteratorEmptylane() {
        // fail("Not yet implemented");
        RoadSegment.resetNextId();
        Vehicle.resetNextId();

        final int laneCount = 3;
        final RoadSegment r0 = new RoadSegment(1000.0, laneCount);

        final Vehicle v0 = newVehicle(800.0, 1.0, Lane.LANE1);
        r0.addVehicle(v0);
        final Vehicle v1 = newVehicle(700.0, 1.0, Lane.LANE1);
        r0.addVehicle(v1);
        final Vehicle v2 = newVehicle(600.0, 1.0, Lane.LANE3);
        r0.addVehicle(v2);
        Iterator<Vehicle> iterator = r0.iterator();
        assertEquals(true, iterator.hasNext());
        assertEquals(v0, iterator.next());
        assertEquals(true, iterator.hasNext());
        assertEquals(v1, iterator.next());
        assertEquals(true, iterator.hasNext());
        assertEquals(v2, iterator.next());
        assertEquals(false, iterator.hasNext());
    }
}
