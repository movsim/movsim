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

import java.util.Iterator;

import org.junit.Test;
import org.movsim.autogen.LaneChangeModelType;
import org.movsim.autogen.ModelParameterMOBIL;
import org.movsim.roadmappings.RoadMapping;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.simulator.vehicles.lanechange.LaneChangeModel;
import org.movsim.simulator.vehicles.longitudinalmodel.acceleration.IDM;

/**
 * Test module for the RoadSegment class.
 */
@SuppressWarnings("static-method")
public class RoadSegmentTest {
    private final double delta = 0.00001;

    // max safe braking decelerations
    private static final double MAX_SAFE_BRAKING_CAR = 5.0;
    // private static final double MAX_SAFE_SELF_BRAKING = 8.0;

    // minimum distances
    private static final double GAP_MIN_FRONT_CAR = 4.0;
    // private static final double GAP_MIN_REAR_CAR = 8.0;

    // inside-lane bias
    private static final double BIAS_INSIDE_LANE_CAR = 0.1;

    // politeness when changing lanes
    private static final double POLITENESS_CAR = 0.2;

    // lane changing thresholds (m/s^2)
    static final double THRESHOLD_CAR = 0.3;

    private Vehicle newVehicle(double rearPosition, double speed, int lane) {
        // Vehicle(type, pos, vel, lane, ldm, lcm, length, width, color);
        // return new Vehicle(Vehicle.Type.NONE, pos, vel, lane, idm, null, 5.0, 2.5, 3);
        final IDM idm = new IDM(33.0, 0.5, 3.0, 1.5, 2.0, 5.0);
        final Vehicle vehicle = new Vehicle(rearPosition, speed, lane, 5.0, 2.5);
        vehicle.setLongitudinalModel(idm);
        vehicle.setSpeedlimit(80.0 / 3.6); // 80 km/h
        return vehicle;
    }

    private Vehicle newObstacle(double rearPosition, int lane) {
        return new Vehicle(rearPosition, 0.0, lane, 5.0, 2.5);
    }

    private LaneChangeModel newLaneChangeModel(Vehicle vehicle) {
        return new LaneChangeModel(vehicle, createLaneChangeModelType());
    }

    private static LaneChangeModelType createLaneChangeModelType() {
        LaneChangeModelType lcType = new LaneChangeModelType();
        lcType.setModelParameterMOBIL(createModelParameterMOBIL());
        lcType.setEuropeanRules(true);
        lcType.setCritSpeedEur(5);
        return lcType;
    }

    private static ModelParameterMOBIL createModelParameterMOBIL() {
        ModelParameterMOBIL param = new ModelParameterMOBIL();
        param.setMinimumGap(GAP_MIN_FRONT_CAR);
        param.setSafeDeceleration(MAX_SAFE_BRAKING_CAR);
        param.setPoliteness(POLITENESS_CAR);
        param.setThresholdAcceleration(THRESHOLD_CAR);
        param.setRightBiasAcceleration(BIAS_INSIDE_LANE_CAR);
        return param;
    }

    /**
     * Test method for {@link org.movsim.simulator.roadnetwork.RoadSegment#resetNextId()}
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
     * Test method for {@link org.movsim.simulator.roadnetwork.RoadSegment#RoadSegment(double, int)}
     */
    @Test
    public final void testRoadSegmentDoubleInt() {
        final double roadLength = 1000.0;
        final int laneCount = 3;
        RoadSegment.resetNextId();
        final RoadSegment roadSegment = new RoadSegment(roadLength, laneCount);
        assertEquals(roadLength, roadSegment.roadLength(), delta);
        assertEquals(laneCount, roadSegment.laneCount());
        assertEquals(null, roadSegment.trafficSource());
        assertEquals(null, roadSegment.sink());
    }

    /**
     * Test method for {@link org.movsim.simulator.roadnetwork.RoadSegment#RoadSegment(org.movsim.roadmappings.RoadMapping)}
     */
    @Test
    public final void testRoadSegmentRoadMapping() {
        final int laneCount = 1;
        final double roadLength = 1000.0;
        final RoadMapping m = RoadMappingConcrete.create(laneCount, roadLength);
        final RoadSegment r = new RoadSegment(roadLength, laneCount, m, RoadSegmentDirection.FORWARD);
        assertEquals(roadLength, r.roadLength(), delta);
        assertEquals(laneCount, r.laneCount());
        assertEquals(null, r.trafficSource());
        assertEquals(null, r.sink());
    }

    /**
     * Test method for {@link org.movsim.simulator.roadnetwork.RoadSegment#addDefaultSink()}
     */
    @Test
    public final void testSetDefaultSink() {
        // fail("Not yet implemented"); //$NON-NLS-1$
    }

    /**
     * Test method for {@link org.movsim.simulator.roadnetwork.RoadSegment#id()}
     */
    @Test
    public final void testId() {
        // fail("Not yet implemented"); //$NON-NLS-1$
    }

    /**
     * Test method for {@link org.movsim.simulator.roadnetwork.RoadSegment#roadMapping()}
     */
    @Test
    public final void testRoadMapping() {
        // fail("Not yet implemented"); //$NON-NLS-1$
    }

    /**
     * Test method for {@link org.movsim.simulator.roadnetwork.RoadSegment#trafficSource()}
     */
    @Test
    public final void testGetTrafficSource() {
        // fail("Not yet implemented"); //$NON-NLS-1$
    }

    /**
     * Test method for
     * {@link org.movsim.simulator.roadnetwork.RoadSegment#setTrafficSource(org.movsim.simulator.roadnetwork.TrafficSourceMacro)}
     */
    @Test
    public final void testSetTrafficSource() {
        // fail("Not yet implemented"); //$NON-NLS-1$
    }

    /**
     * Test method for {@link org.movsim.simulator.roadnetwork.RoadSegment#sink()}
     */
    @Test
    public final void testSink() {
        // fail("Not yet implemented"); //$NON-NLS-1$
    }

    /**
     * Test method for {@link org.movsim.simulator.roadnetwork.RoadSegment#setSink(org.movsim.simulator.roadnetwork.TrafficSink)}
     */
    @Test
    public final void testSetSink() {
        // fail("Not yet implemented"); //$NON-NLS-1$
    }

    /**
     * Test method for {@link org.movsim.simulator.roadnetwork.RoadSegment#trafficLaneMin()}
     */
    @Test
    public final void testTrafficLaneMin() {
        // fail("Not yet implemented"); //$NON-NLS-1$
    }

    /**
     * Test method for {@link org.movsim.simulator.roadnetwork.RoadSegment#trafficLaneMax()}
     */
    @Test
    public final void testTrafficLaneMax() {
        // fail("Not yet implemented"); //$NON-NLS-1$
    }

    /**
     * Test method for {@link org.movsim.simulator.roadnetwork.RoadSegment#sourceRoadSegment(int)}
     */
    @Test
    public final void testSourceRoadSegment() {
        // fail("Not yet implemented"); //$NON-NLS-1$
    }

    /**
     * Test method for
     * {@link org.movsim.simulator.roadnetwork.RoadSegment#setSourceLaneSegmentForLane(org.movsim.simulator.roadnetwork.LaneSegment, int)}
     */
    @Test
    public final void testSetSourceLaneSegmentForLane() {
        // fail("Not yet implemented"); //$NON-NLS-1$
    }

    /**
     * Test method for {@link org.movsim.simulator.roadnetwork.RoadSegment#sourceLane(int)}
     */
    @Test
    public final void testSourceLane() {
        // fail("Not yet implemented"); //$NON-NLS-1$
    }

    /**
     * Test method for {@link org.movsim.simulator.roadnetwork.RoadSegment#sinkRoadSegment(int)}
     */
    @Test
    public final void testSinkRoadSegment() {
        // fail("Not yet implemented"); //$NON-NLS-1$
    }

    /**
     * Test method for
     * {@link org.movsim.simulator.roadnetwork.RoadSegment#setSinkLaneSegmentForLane(org.movsim.simulator.roadnetwork.LaneSegment, int)}
     */
    @Test
    public final void testSetSinkLaneSegmentForLane() {
        // fail("Not yet implemented"); //$NON-NLS-1$
    }

    /**
     * Test method for {@link org.movsim.simulator.roadnetwork.RoadSegment#sinkLane(int)}
     */
    @Test
    public final void testSinkLane() {
        // fail("Not yet implemented"); //$NON-NLS-1$
    }

    /**
     * Test method for {@link org.movsim.simulator.roadnetwork.RoadSegment#roadLength()}
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
     * Test method for {@link org.movsim.simulator.roadnetwork.RoadSegment#laneCount()}
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
     * Test method for {@link org.movsim.simulator.roadnetwork.RoadSegment#clearVehicles()}
     */
    @Test
    public final void testClearVehicles() {
        final double roadLength = 1000.0;
        final int laneCount = 2;
        RoadSegment.resetNextId();
        final RoadSegment roadSegment = new RoadSegment(roadLength, laneCount);
        assertEquals(0, roadSegment.getVehicleCount());
        roadSegment.addVehicle(newVehicle(900.0, 0.0, Lanes.LANE1));
        assertEquals(1, roadSegment.getVehicleCount());
        roadSegment.addVehicle(newVehicle(800.0, 0.0, Lanes.LANE2));
        assertEquals(2, roadSegment.getVehicleCount());
        roadSegment.addVehicle(newVehicle(700.0, 0.0, Lanes.LANE1));
        assertEquals(3, roadSegment.getVehicleCount());
        roadSegment.addVehicle(newVehicle(600.0, 0.0, Lanes.LANE2));
        assertEquals(4, roadSegment.getVehicleCount());
        roadSegment.clearVehicles();
        assertEquals(0, roadSegment.getVehicleCount());
        assertEquals(0, roadSegment.getVehicleCount(Lanes.LANE1));
        assertEquals(0, roadSegment.getVehicleCount(Lanes.LANE2));
    }

    /**
     * Test method for {@link org.movsim.simulator.roadnetwork.RoadSegment#getVehicleCount()}
     */
    @Test
    public final void testVehicleCount() {
        final double roadLength = 1000.0;
        final int laneCount = 1;
        RoadSegment.resetNextId();
        final RoadSegment roadSegment = new RoadSegment(roadLength, laneCount);
        assertEquals(0, roadSegment.getVehicleCount());
        roadSegment.addVehicle(newVehicle(1.0, 0.0, Lanes.LANE1));
        assertEquals(1, roadSegment.getVehicleCount());
        roadSegment.addVehicle(newVehicle(11.0, 0.0, Lanes.LANE1));
        assertEquals(2, roadSegment.getVehicleCount());
        roadSegment.clearVehicles();
        assertEquals(0, roadSegment.getVehicleCount());
    }

    /**
     * Test method for {@link org.movsim.simulator.roadnetwork.RoadSegment#getVehicle(int, int)}
     */
    @Test
    public final void testGetVehicle() {
        // fail("Not yet implemented"); //$NON-NLS-1$
    }

    /**
     * Test method for {@link org.movsim.simulator.roadnetwork.RoadSegment#addVehicle(org.movsim.simulator.vehicles.Vehicle)}
     */
    @Test
    public final void testAddObstacle() {
        // fail("Not yet implemented"); //$NON-NLS-1$
    }

    /**
     * Test method for {@link org.movsim.simulator.roadnetwork.RoadSegment#addVehicle(org.movsim.simulator.vehicles.Vehicle)}
     */
    @Test
    public final void testAddVehicleVehicle() {
        RoadSegment.resetNextId();
        Vehicle.resetNextId();

        final int laneCount = 1;
        final RoadSegment r0 = new RoadSegment(5000.0, laneCount);
        Vehicle v;

        final Vehicle v0 = newVehicle(3900.0, 1.0, Lanes.LANE1);
        r0.addVehicle(v0);
        v = r0.getVehicle(Lanes.LANE1, 0);
        assertEquals(v0, v);
        final Vehicle v1 = newVehicle(3700.0, 2.0, Lanes.LANE1);
        r0.addVehicle(v1);
        v = r0.getVehicle(Lanes.LANE1, 0);
        assertEquals(v0, v);
        v = r0.getVehicle(Lanes.LANE1, 1);
        assertEquals(v1, v);
        final Vehicle v2 = newVehicle(3100.0, 3.0, Lanes.LANE1);
        r0.addVehicle(v2);
        v = r0.getVehicle(Lanes.LANE1, 0);
        assertEquals(v0, v);
        v = r0.getVehicle(Lanes.LANE1, 1);
        assertEquals(v1, v);
        v = r0.getVehicle(Lanes.LANE1, 2);
        assertEquals(v2, v);

        final Vehicle v1a = newVehicle(3500.0, 4.0, Lanes.LANE1);
        r0.addVehicle(v1a);
        v = r0.getVehicle(Lanes.LANE1, 0);
        assertEquals(v0, v);
        v = r0.getVehicle(Lanes.LANE1, 1);
        assertEquals(v1, v);
        v = r0.getVehicle(Lanes.LANE1, 2);
        assertEquals(v1a, v);
        v = r0.getVehicle(Lanes.LANE1, 3);
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
        Vehicle vehicle = roadSegment.rearVehicleOnLane(Lanes.LANE1);
        assertEquals(null, vehicle);

        final Vehicle v0 = newVehicle(900.0, 0.0, Lanes.LANE1);
        roadSegment.addVehicle(v0);
        vehicle = roadSegment.rearVehicleOnLane(Lanes.LANE1);
        assertEquals(v0.getId(), vehicle.getId());

        final Vehicle v1 = newVehicle(800.0, 0.0, Lanes.LANE1);
        roadSegment.addVehicle(v1);
        vehicle = roadSegment.rearVehicleOnLane(Lanes.LANE1);
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

        final Vehicle v0 = newVehicle(900.0, 1.0, Lanes.LANE1);
        roadSegment.addVehicle(v0);
        Vehicle rV = roadSegment.rearVehicle(Lanes.LANE1, 901.0);
        assertEquals(v0, rV);
        rV = roadSegment.rearVehicle(Lanes.LANE1, 900.0);
        assertEquals(v0, rV);
        rV = roadSegment.rearVehicle(Lanes.LANE1, 899.0);
        assertEquals(null, rV);
        rV = roadSegment.rearVehicle(Lanes.LANE1, 0.0);
        assertEquals(null, rV);

        final Vehicle v1 = newVehicle(800.0, 2.0, Lanes.LANE1);
        roadSegment.addVehicle(v1);
        rV = roadSegment.rearVehicle(Lanes.LANE1, 900.0);
        assertEquals(v0, rV);
        rV = roadSegment.rearVehicle(Lanes.LANE1, 800.0);
        assertEquals(v1, rV);
        rV = roadSegment.rearVehicle(Lanes.LANE1, 901.0);
        assertEquals(v0, rV);
        rV = roadSegment.rearVehicle(Lanes.LANE1, 899.0);
        assertEquals(v1, rV);
        rV = roadSegment.rearVehicle(Lanes.LANE1, 801.0);
        assertEquals(v1, rV);
        rV = roadSegment.rearVehicle(Lanes.LANE1, 799.0);
        assertEquals(null, rV);
        rV = roadSegment.rearVehicle(Lanes.LANE1, 0.0);
        assertEquals(null, rV);

        final Vehicle v2 = newVehicle(700.0, 3.0, Lanes.LANE1);
        roadSegment.addVehicle(v2);
        assert roadSegment.eachLaneIsSorted();
        rV = roadSegment.rearVehicle(Lanes.LANE1, v0.getMidPosition());
        assertEquals(v0, rV);
        rV = roadSegment.rearVehicle(Lanes.LANE1, v1.getMidPosition());
        assertEquals(v1, rV);
        rV = roadSegment.rearVehicle(Lanes.LANE1, v2.getMidPosition());
        assertEquals(v2, rV);
        rV = roadSegment.rearVehicle(Lanes.LANE1, 901.0);
        assertEquals(v0, rV);
        rV = roadSegment.rearVehicle(Lanes.LANE1, 899.0);
        assertEquals(v1, rV);
        rV = roadSegment.rearVehicle(Lanes.LANE1, 801.0);
        assertEquals(v1, rV);
        rV = roadSegment.rearVehicle(Lanes.LANE1, 799.0);
        assertEquals(v2, rV);
        rV = roadSegment.rearVehicle(Lanes.LANE1, 701.0);
        assertEquals(v2, rV);
        rV = roadSegment.rearVehicle(Lanes.LANE1, 699.0);
        assertEquals(null, rV);
        rV = roadSegment.rearVehicle(Lanes.LANE1, 0.0);
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

        final Vehicle v0 = newVehicle(3900.0, 1.0, Lanes.LANE1);
        assertEquals(3900.0, v0.getRearPosition(), delta);
        r1.addVehicle(v0);
        final Vehicle v1 = newVehicle(3700.0, 2.0, Lanes.LANE1);
        r1.addVehicle(v1);
        final Vehicle v2 = newVehicle(3100.0, 3.0, Lanes.LANE1);
        r1.addVehicle(v2);

        final Vehicle v3 = newVehicle(600.0, 4.0, Lanes.LANE1);
        r0.addVehicle(v3);
        final Vehicle v4 = newVehicle(500.0, 5.0, Lanes.LANE1);
        r0.addVehicle(v4);

        Vehicle rV = r1.rearVehicle(Lanes.LANE1, 3901.0);
        assertEquals(v0, rV);
        rV = r1.rearVehicle(Lanes.LANE1, 3900.0);
        assertEquals(v0, rV);
        rV = r1.rearVehicle(Lanes.LANE1, 3700.0);
        assertEquals(v1, rV);
        rV = r1.rearVehicle(Lanes.LANE1, 3101.0);
        assertEquals(v2, rV);
        rV = r1.rearVehicle(Lanes.LANE1, 3100.0);
        assertEquals(v2, rV);
        rV = r1.rearVehicle(Lanes.LANE1, 3099.0);
        assertEquals(v3.getId(), rV.getId());
        assertEquals(-100.0, rV.getRearPosition(), delta); // pos relative to r1
        assertEquals(v3.getSpeed(), rV.getSpeed(), delta);

        rV = r1.rearVehicle(Lanes.LANE1, 3099.0);
        assertEquals(v3.getId(), rV.getId());
        assertEquals(-100.0, rV.getRearPosition(), delta); // pos relative to r1
        rV = r0.rearVehicle(Lanes.LANE1, 601.0);
        assertEquals(v3, rV);
        rV = r0.rearVehicle(Lanes.LANE1, 600.0);
        assertEquals(v3, rV);
        rV = r0.rearVehicle(Lanes.LANE1, 599.0);
        assertEquals(v4, rV);
        rV = r0.rearVehicle(Lanes.LANE1, 501.0);
        assertEquals(v4, rV);
        rV = r0.rearVehicle(Lanes.LANE1, 500.0);
        assertEquals(v4, rV);
        rV = r0.rearVehicle(Lanes.LANE1, 499.0);
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
        r1.setLaneType(Lanes.LANE3, Lanes.Type.EXIT);// so Lane3 is exit lane of r1
        Link.addJoin(r0, r1);
        assertEquals(r0.id(), r1.sourceRoadSegment(Lanes.LANE1).id());
        assertEquals(r0.id(), r1.sourceRoadSegment(Lanes.LANE2).id());
        assertEquals(null, r1.sourceRoadSegment(Lanes.LANE3));

        // vehicles suffixed 0 are on r0, vehicles suffixed 1 are on r1
        final Vehicle z1 = newVehicle(5.0, 1.0, Lanes.LANE1);
        r1.addVehicle(z1);
        final Vehicle z0 = newVehicle(996.0, 3.0, Lanes.LANE1);
        r0.addVehicle(z0);
        final Vehicle y1 = newVehicle(3.0, 4.0, Lanes.LANE2);
        r1.addVehicle(y1);
        final Vehicle y0 = newVehicle(998.0, 5.0, Lanes.LANE2);
        r0.addVehicle(y0);
        // vehicle in exit lane
        final Vehicle x1 = newVehicle(5.0, 5.0, Lanes.LANE3);
        r1.addVehicle(x1);

        Vehicle rV = r1.rearVehicle(Lanes.LANE1, 6.0);
        assertEquals(z1, rV);
        rV = r1.rearVehicle(Lanes.LANE1, 5.0);
        assertEquals(z1, rV);
        rV = r1.rearVehicle(Lanes.LANE1, 4.0);
        assertEquals(z0.getId(), rV.getId());
        assertEquals(-4.0, rV.getRearPosition(), delta);
        assertEquals(3.0, rV.getSpeed(), delta);
        rV = r1.rearVehicle(Lanes.LANE1, 3.0);
        assertEquals(z0.getId(), rV.getId());

        rV = r1.rearVehicle(Lanes.LANE2, 4.0);
        assertEquals(y1, rV);
        rV = r1.rearVehicle(Lanes.LANE2, 3.0);
        assertEquals(y1, rV);
        rV = r1.rearVehicle(Lanes.LANE2, 2.0);
        assert rV != null;
        assertEquals(y0.getId(), rV.getId());
        assertEquals(-2.0, rV.getRearPosition(), delta);
        assertEquals(5.0, rV.getSpeed(), delta);
        rV = r1.rearVehicle(Lanes.LANE2, 1.0);
        assertEquals(y0.getId(), rV.getId());
    }

    /**
     * Test method for org.mjbudden.traffic.RoadSegment#rearVehicle(int, double)
     */
    @Test
    public final void testRearVehicleMerge() {
        // test rear vehicle when there is an offset join, for example a join
        // onto a road segment that has an exit lane
        RoadSegment.resetNextId();
        Vehicle.resetNextId();

        // r0 has 3 lanes which merge into 2 lanes of r1
        final int laneCount = 2;
        final int exitLaneCount = 1;
        final RoadSegment r0 = new RoadSegment(1000.0, laneCount + exitLaneCount);
        final RoadSegment r1 = new RoadSegment(200.0, laneCount);
        r0.setLaneType(Lanes.LANE3, Lanes.Type.EXIT);// so Lane1 is exit lane of r1
        // join r0 and r1 so vehicles move from r0 to r1
        Link.addJoin(r0, r1);
        assertEquals(r0.id(), r1.sourceRoadSegment(Lanes.LANE2).id());
        assertEquals(r0.id(), r1.sourceRoadSegment(Lanes.LANE1).id());
        assertEquals(Lanes.LANE1, r1.sourceLane(Lanes.LANE1));
        assertEquals(Lanes.LANE2, r1.sourceLane(Lanes.LANE2));

        assertEquals(r1.id(), r0.sinkRoadSegment(Lanes.LANE1).id());
        assertEquals(r1.id(), r0.sinkRoadSegment(Lanes.LANE2).id());
        assertEquals(null, r0.sinkRoadSegment(Lanes.LANE3));
        assertEquals(Lanes.NONE, r0.sinkLane(Lanes.LANE3));
        assertEquals(Lanes.LANE1, r0.sinkLane(Lanes.LANE1));
        assertEquals(Lanes.LANE2, r0.sinkLane(Lanes.LANE2));

        // vehicles suffixed 0 are on r0, vehicles suffixed 1 are on r1
        final Vehicle z1 = newVehicle(5.0, 1.0, Lanes.LANE1);
        r1.addVehicle(z1);
        final Vehicle z0 = newVehicle(996.0, 3.0, Lanes.LANE1);
        r0.addVehicle(z0);
        final Vehicle y1 = newVehicle(3.0, 4.0, Lanes.LANE2);
        r1.addVehicle(y1);
        final Vehicle y0 = newVehicle(998.0, 5.0, Lanes.LANE2);
        r0.addVehicle(y0);
        // vehicle in exit lane
        final Vehicle x0 = newVehicle(5.0, 5.0, Lanes.LANE3);
        r0.addVehicle(x0);

        Vehicle rV = r1.rearVehicle(Lanes.LANE1, 6.0);
        assertEquals(z1, rV);
        rV = r1.rearVehicle(Lanes.LANE1, 5.0);
        assertEquals(z1, rV);
        rV = r1.rearVehicle(Lanes.LANE1, 4.0);
        assertEquals(z0.getId(), rV.getId());
        assertEquals(-4.0, rV.getRearPosition(), delta);
        assertEquals(3.0, rV.getSpeed(), delta);
        rV = r1.rearVehicle(Lanes.LANE1, 3.0);
        assertEquals(z0.getId(), rV.getId());

        rV = r1.rearVehicle(Lanes.LANE2, 4.0);
        assertEquals(y1, rV);
        rV = r1.rearVehicle(Lanes.LANE2, 3.0);
        assertEquals(y1, rV);
        rV = r1.rearVehicle(Lanes.LANE2, 2.0);
        assert rV != null;
        assertEquals(y0.getId(), rV.getId());
        assertEquals(-2.0, rV.getRearPosition(), delta);
        assertEquals(5.0, rV.getSpeed(), delta);
        rV = r1.rearVehicle(Lanes.LANE2, 1.0);
        assertEquals(y0.getId(), rV.getId());
    }

    /**
     * Test method for {@link org.movsim.simulator.roadnetwork.RoadSegment#frontVehicleOnLane(int)} Vehicles are sorted in order of
     * decreasing position: start end V(n+1).pos < V(n).pos < V(n-1).pos ... < V(1).pos < V(0).pos
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
        Vehicle vehicle = roadSegment.frontVehicleOnLane(Lanes.LANE1);
        assertEquals(null, vehicle);

        final Vehicle v0 = newVehicle(900.0, 0.0, Lanes.LANE1);
        roadSegment.addVehicle(v0);
        vehicle = roadSegment.frontVehicleOnLane(Lanes.LANE1);
        assertEquals(v0.getId(), vehicle.getId());

        final Vehicle v1 = newVehicle(800.0, 0.0, Lanes.LANE1);
        roadSegment.addVehicle(v1);
        vehicle = roadSegment.frontVehicleOnLane(Lanes.LANE1);
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
        final Vehicle v0 = newVehicle(900.0, 1.0, Lanes.LANE1);
        roadSegment.addVehicle(v0);
        Vehicle fV = roadSegment.frontVehicle(Lanes.LANE1, 900.0);
        assertEquals(null, fV);
        fV = roadSegment.frontVehicle(Lanes.LANE1, 1000.0);
        assertEquals(null, fV);
        fV = roadSegment.frontVehicle(Lanes.LANE1, 901.0);
        assertEquals(null, fV);
        fV = roadSegment.frontVehicle(Lanes.LANE1, 899.0);
        assertEquals(v0, fV);
        fV = roadSegment.frontVehicle(Lanes.LANE1, 0.0);
        assertEquals(v0, fV);

        final Vehicle v1 = newVehicle(800.0, 2.0, Lanes.LANE1);
        roadSegment.addVehicle(v1);
        fV = roadSegment.frontVehicle(Lanes.LANE1, 900.0);
        assertEquals(null, fV);
        fV = roadSegment.frontVehicle(Lanes.LANE1, 800.0);
        assertEquals(v0, fV);
        fV = roadSegment.frontVehicle(Lanes.LANE1, 901.0);
        assertEquals(null, fV);
        fV = roadSegment.frontVehicle(Lanes.LANE1, 899.0);
        assertEquals(v0, fV);
        fV = roadSegment.frontVehicle(Lanes.LANE1, 801.0);
        assertEquals(v0, fV);
        fV = roadSegment.frontVehicle(Lanes.LANE1, 799.0);
        assertEquals(v1, fV);
        fV = roadSegment.frontVehicle(Lanes.LANE1, 0.0);
        assertEquals(v1, fV);

        final Vehicle v2 = newVehicle(700.0, 3.0, Lanes.LANE1);
        roadSegment.addVehicle(v2);
        fV = roadSegment.frontVehicle(Lanes.LANE1, v0.getMidPosition());
        assertEquals(null, fV);
        fV = roadSegment.frontVehicle(Lanes.LANE1, v1.getMidPosition());
        assertEquals(v0, fV);
        fV = roadSegment.frontVehicle(Lanes.LANE1, v2.getMidPosition());
        assertEquals(v1, fV);
        fV = roadSegment.frontVehicle(Lanes.LANE1, 901.0);
        assertEquals(null, fV);
        fV = roadSegment.frontVehicle(Lanes.LANE1, 899.0);
        assertEquals(v0, fV);
        fV = roadSegment.frontVehicle(Lanes.LANE1, 801.0);
        assertEquals(v0, fV);
        fV = roadSegment.frontVehicle(Lanes.LANE1, 799.0);
        assertEquals(v1, fV);
        fV = roadSegment.frontVehicle(Lanes.LANE1, 701.0);
        assertEquals(v1, fV);
        fV = roadSegment.frontVehicle(Lanes.LANE1, 699.0);
        assertEquals(v2, fV);
        fV = roadSegment.frontVehicle(Lanes.LANE1, 0.0);
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

        final Vehicle v0 = newVehicle(3900.0, 1.0, Lanes.LANE1);
        r1.addVehicle(v0);
        final Vehicle v1 = newVehicle(3700.0, 2.0, Lanes.LANE1);
        r1.addVehicle(v1);
        final Vehicle v2 = newVehicle(3100.0, 3.0, Lanes.LANE1);
        r1.addVehicle(v2);

        final Vehicle v3 = newVehicle(600.0, 4.0, Lanes.LANE1);
        r0.addVehicle(v3);
        final Vehicle v4 = newVehicle(500.0, 5.0, Lanes.LANE1);
        r0.addVehicle(v4);

        Vehicle fV = r1.frontVehicle(Lanes.LANE1, v0.getMidPosition());
        assertEquals(null, fV);
        fV = r1.frontVehicle(Lanes.LANE1, r1.roadLength());
        assertEquals(null, fV);
        fV = r1.frontVehicle(Lanes.LANE1, v1.getMidPosition());
        assertEquals(v0, fV);
        fV = r1.frontVehicle(Lanes.LANE1, v2.getMidPosition());
        assertEquals(v1, fV);

        // vehicle in front of end of road0 is v2
        fV = r0.frontVehicle(Lanes.LANE1, r0.roadLength());
        assertEquals(v2.getId(), fV.getId());
        assertEquals(v2.getMidPosition() + r0.roadLength(), fV.getMidPosition(), delta);
        assertEquals(v2.getSpeed(), fV.getSpeed(), delta);
        // vehicle in front of v3 is v2
        fV = r0.frontVehicle(Lanes.LANE1, v3.getMidPosition());
        assertEquals(v2.getId(), fV.getId());
        fV = r0.frontVehicle(Lanes.LANE1, v4.getMidPosition());
        assertEquals(v3, fV);
        fV = r0.frontVehicle(Lanes.LANE1, 0.0);
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
        r1.setLaneType(Lanes.LANE3, Lanes.Type.EXIT);// so Lane1 is exit lane of r1
        // join r0 and r1 so vehicles move from r0 to r1
        Link.addJoin(r0, r1);

        // vehicles suffixed 0 are on r0, vehicles suffixed 1 are on r1
        final Vehicle z1 = newVehicle(5.0, 1.0, Lanes.LANE1);
        r1.addVehicle(z1);
        final Vehicle z0 = newVehicle(996.0, 3.0, Lanes.LANE1);
        r0.addVehicle(z0);
        final Vehicle y1 = newVehicle(3.0, 4.0, Lanes.LANE2);
        r1.addVehicle(y1);
        final Vehicle y0 = newVehicle(998.0, 5.0, Lanes.LANE2);
        r0.addVehicle(y0);
        // vehicle in exit lane
        final Vehicle x1 = newVehicle(5.0, 5.0, Lanes.LANE3);
        r1.addVehicle(x1);

        Vehicle fV = r0.frontVehicle(Lanes.LANE1, 995.0);
        assertEquals(z0, fV);
        fV = r0.frontVehicle(Lanes.LANE1, 996.0);
        assertEquals(z1.getId(), fV.getId());
        assertEquals(1005.0, fV.getRearPosition(), delta);
        assertEquals(1.0, fV.getSpeed(), delta);
        fV = r0.frontVehicle(Lanes.LANE1, 997.0);
        assertEquals(z1.getId(), fV.getId());

        fV = r0.frontVehicle(Lanes.LANE2, 997.0);
        assertEquals(y0, fV);
        fV = r0.frontVehicle(Lanes.LANE2, 998.0);
        assertEquals(y1.getId(), fV.getId());
        assertEquals(1003.0, fV.getRearPosition(), delta);
        assertEquals(4.0, fV.getSpeed(), delta);
        fV = r0.frontVehicle(Lanes.LANE2, 999.0);
        assertEquals(y1.getId(), fV.getId());
    }

    /**
     * Test method for {@link org.movsim.simulator.roadnetwork.RoadSegment#makeLaneChanges(double, double, long)}
     */
    @Test
    public final void testMakeLaneChanges() {
        RoadSegment.resetNextId();
        Vehicle.resetNextId();

        final int laneCount = 2;
        final RoadSegment r0 = new RoadSegment(1000.0, laneCount);

        // set up an obstacle directly in front of a vehicle, so the vehicle will change lanes
        // Obstacle(pos, lane, length, width, color) {
        final Vehicle obstacle = newObstacle(600.0, Lanes.LANE1);
        r0.addVehicle(obstacle);
        final Vehicle v0 = newVehicle(593.0, 5.0, Lanes.LANE1);
        final LaneChangeModel lcm = newLaneChangeModel(v0);
        v0.setLaneChangeModel(lcm);
        r0.addVehicle(v0);
        final double dt = 0.25;
        final double simulationTime = 0.0;
        final long iterationCount = 0;
        r0.makeLaneChanges(dt, simulationTime, iterationCount);
        assertEquals(Lanes.LANE1, obstacle.lane());
        assertEquals(Lanes.LANE2, v0.lane());
        assertEquals(1, r0.laneSegment(Lanes.LANE1).vehicleCount());
        assertEquals(1, r0.laneSegment(Lanes.LANE2).vehicleCount());
    }

    /**
     * Test method for {@link org.movsim.simulator.roadnetwork.RoadSegment#updateVehiclePositionsAndSpeeds(double, double, long)}
     */
    @Test
    public final void testUpdateVehiclePositionsAndVelocities() {
        RoadSegment.resetNextId();
        Vehicle.resetNextId();
        // Vehicle.setIntegrationType(Vehicle.IntegrationType.EULER);
        final double roadLength = 1000.0;
        final int laneCount = 1;
        final RoadSegment roadSegment = new RoadSegment(roadLength, laneCount);

        final Vehicle v0 = newVehicle(900.0, 10.0, Lanes.LANE1);
        roadSegment.addVehicle(v0);
        final Vehicle v1 = newVehicle(800.0, 20.0, Lanes.LANE1);
        roadSegment.addVehicle(v1);
        final Vehicle v2 = newVehicle(700.0, 30.0, Lanes.LANE1);
        roadSegment.addVehicle(v2);

        final double dt = 0.25;
        roadSegment.updateVehiclePositionsAndSpeeds(dt, 0.0, 0);
        assertEquals(902.5, v0.getRearPosition(), delta);
        assertEquals(805.0, v1.getRearPosition(), delta);
        assertEquals(707.5, v2.getRearPosition(), delta);
    }

    /**
     * Test method for {@link org.movsim.simulator.roadnetwork.RoadSegment#updateVehiclePositionsAndSpeeds(double, double, long)}
     */
    @Test
    public final void testUpdateVehiclePositionsAndVelocitiesJoin() {
        RoadSegment.resetNextId();
        Vehicle.resetNextId();
        // Vehicle.setIntegrationType(Vehicle.IntegrationType.EULER);

        final int laneCount = 1;
        final RoadSegment r0 = new RoadSegment(700.0, laneCount);
        final RoadSegment r1 = new RoadSegment(5100.0, laneCount);
        // join r0 and r1 so vehicles move from r0 to r1
        Link.addJoin(r0, r1);

        final Vehicle v0 = newVehicle(3900.0, 10.0, Lanes.LANE1);
        r1.addVehicle(v0);
        final Vehicle v1 = newVehicle(3700.0, 20.0, Lanes.LANE1);
        r1.addVehicle(v1);
        final Vehicle v2 = newVehicle(3100.0, 30.0, Lanes.LANE1);
        r1.addVehicle(v2);

        final Vehicle v3 = newVehicle(695.0, 40.0, Lanes.LANE1);
        r0.addVehicle(v3);
        final Vehicle v4 = newVehicle(500.0, 50.0, Lanes.LANE1);
        r0.addVehicle(v4);

        final double dt = 0.25;
        final double simulationTime = 0.0;
        final long iterationCount = 0;
        r0.updateVehiclePositionsAndSpeeds(dt, simulationTime, iterationCount);
        assertEquals(705.0, v3.getRearPosition(), delta);
        assertEquals(512.5, v4.getRearPosition(), delta);

        r1.updateVehiclePositionsAndSpeeds(dt, simulationTime, iterationCount);
        assertEquals(3902.5, v0.getRearPosition(), delta);
        assertEquals(3705.0, v1.getRearPosition(), delta);
        assertEquals(3107.5, v2.getRearPosition(), delta);
        // check has been moved to new road segment by outflow
        r0.outFlow(dt, simulationTime, iterationCount);
        r1.outFlow(dt, simulationTime, iterationCount);
        assertEquals(r1.id(), v3.roadSegmentId());
        assertEquals(5.0, v3.getRearPosition(), delta);
    }

    /**
     * Test method for {@link org.movsim.simulator.roadnetwork.RoadSegment#updateVehiclePositionsAndSpeeds(double, double, long)}
     */
    @SuppressWarnings("boxing")
    @Test
    public final void testUpdateVehiclePositionsAndVelocitiesSelfJoin() {
        RoadSegment.resetNextId();
        Vehicle.resetNextId();
        // Vehicle.setIntegrationType(Vehicle.IntegrationType.EULER);

        final int laneCount = 1;
        final RoadSegment r0 = new RoadSegment(3900.0, laneCount);
        // join r0 and r1 so vehicles move from r0 to r1
        Link.addJoin(r0, r0);

        final Vehicle v0 = newVehicle(3895.0, 80.0, Lanes.LANE1);
        r0.addVehicle(v0);
        final Vehicle v1 = newVehicle(3700.0, 20.0, Lanes.LANE1);
        r0.addVehicle(v1);
        final Vehicle v2 = newVehicle(3100.0, 30.0, Lanes.LANE1);
        r0.addVehicle(v2);

        final Vehicle v3 = newVehicle(695.0, 40.0, Lanes.LANE1);
        r0.addVehicle(v3);
        final Vehicle v4 = newVehicle(500.0, 50.0, Lanes.LANE1);
        r0.addVehicle(v4);

        final double dt = 0.25;
        final double simulationTime = 0.0;
        final long iterationCount = 0;
        r0.updateVehiclePositionsAndSpeeds(dt, simulationTime, iterationCount);
        assertEquals(true, r0.eachLaneIsSorted());
        r0.outFlow(dt, simulationTime, iterationCount);

        assertEquals(15.0, v0.getRearPosition(), delta);
        assertEquals(3705.0, v1.getRearPosition(), delta);
        assertEquals(3107.5, v2.getRearPosition(), delta);
        assertEquals(705.0, v3.getRearPosition(), delta);
        assertEquals(512.5, v4.getRearPosition(), delta);
        assertEquals(true, r0.eachLaneIsSorted());
    }

    /**
     * Test method for {@link org.movsim.simulator.roadnetwork.RoadSegment#updateVehiclePositionsAndSpeeds(double, double, long)}
     */
    @SuppressWarnings("boxing")
    @Test
    public final void testUpdateVehiclePositionsAndVelocitiesCalc() {
        // RoadSegment.resetNextId();
        // Vehicle.resetNextId();
        // Vehicle.setIntegrationType(Vehicle.IntegrationType.EULER);
        //
        // final int laneCount = 1;
        // final RoadSegment r0 = new RoadSegment(100000.0, laneCount);
        // final int vehicleCount = 5;
        // final ArrayList<Vehicle> vehicles = new ArrayList<Vehicle>(vehicleCount);
        //
        // final Vehicle v0 = newVehicle(3895.0, 80.0, Lanes.LANE1);
        // r0.addVehicle(v0);
        // final Vehicle v1 = newVehicle(3700.0, 20.0, Lanes.LANE1);
        // r0.addVehicle(v1);
        // final Vehicle v2 = newVehicle(3100.0, 30.0, Lanes.LANE1);
        // r0.addVehicle(v2);
        // final Vehicle v3 = newVehicle(695.0, 40.0, Lanes.LANE1);
        // r0.addVehicle(v3);
        // final Vehicle v4 = newVehicle(500.0, 50.0, Lanes.LANE1);
        // r0.addVehicle(v4);
        //
        // final Vehicle w0 = new Vehicle(v0);
        // vehicles.add(w0);
        // final Vehicle w1 = new Vehicle(v1);
        // vehicles.add(w1);
        // final Vehicle w2 = new Vehicle(v2);
        // vehicles.add(w2);
        // final Vehicle w3 = new Vehicle(v3);
        // vehicles.add(w3);
        // final Vehicle w4 = new Vehicle(v4);
        // vehicles.add(w4);
        // assertEquals(true, r0.eachLaneIsSorted());
        //
        // final double dt = 0.25;
        // final double simulationTime = 0.0;
        // final long iterationCount = 0;
        // r0.updateVehiclePositionsAndVelocities(dt, simulationTime, iterationCount);
        // assertEquals(true, r0.eachLaneIsSorted());
        //
        // assertEquals(v0.getPosition(), 3915.0, delta);
        // assertEquals(v1.getPosition(), 3705.0, delta);
        // assertEquals(v2.getPosition(), 3107.5, delta);
        // assertEquals(v3.getPosition(), 705.0, delta);
        // assertEquals(v4.getPosition(), 512.5, delta);
        // final int count = vehicles.size();
        // for (int i = count - 1; i >= 2; i--) {
        // final Vehicle vehicle = vehicles.get(i);
        // final Vehicle frontVehicle = vehicles.get(i - 1);
        // if (Vehicle.integrationType() == Vehicle.IntegrationType.EULER) {
        // vehicle.eulerIntegrate(dt, frontVehicle);
        // } else {
        // final Vehicle frontFrontVehicle = vehicles.get(i - 2);
        // vehicle.rungeKuttaIntegrate(dt, frontVehicle, frontFrontVehicle);
        // }
        // }
        // if (Vehicle.integrationType() == Vehicle.IntegrationType.EULER) {
        // w1.eulerIntegrate(dt, w0);
        // w0.eulerIntegrate(dt, null);
        // } else {
        // w1.rungeKuttaIntegrate(dt, w0, null);
        // w0.rungeKuttaIntegrate(dt, null, null);
        // }
        // assertEquals(w0.getPosition(), v0.getPosition(), delta);
        // assertEquals(w0.getSpeed(), v0.getSpeed(), delta);
        // assertEquals(w1.getPosition(), v1.getPosition(), delta);
        // assertEquals(w1.getSpeed(), v1.getSpeed(), delta);
        // assertEquals(w2.getPosition(), v2.getPosition(), delta);
        // assertEquals(w2.getSpeed(), v2.getSpeed(), delta);
        // assertEquals(w3.getPosition(), v3.getPosition(), delta);
        // assertEquals(w3.getSpeed(), v3.getSpeed(), delta);
        // assertEquals(w4.getPosition(), v4.getPosition(), delta);
        // assertEquals(w4.getSpeed(), v4.getSpeed(), delta);
    }

    /**
     * Test method for {@link org.movsim.simulator.roadnetwork.RoadSegment#updateVehiclePositionsAndSpeeds(double, double, long)}
     */
    @SuppressWarnings("boxing")
    @Test
    public final void testUpdateVehiclePositionsAndVelocitiesMany() {
        // RoadSegment.resetNextId();
        // Vehicle.resetNextId();
        // Vehicle.setIntegrationType(Vehicle.IntegrationType.EULER);
        //
        // final int laneCount = 1;
        // final int vehicleCount = 1000;
        // final ArrayList<Vehicle> vehicles = new ArrayList<Vehicle>(vehicleCount);
        // final double averageSpacing = 200.0;
        // final double averageVelocity = 90.0 / 3.6; // 90 km/h
        // final RoadSegment r0 = new RoadSegment(10 * vehicleCount * averageSpacing, laneCount);
        //
        // for (int i = vehicleCount - 1; i >= 0; i--) {
        // // TODO - add random variation to pos and vel
        // final double pos = i * averageSpacing;
        // final double vel = averageVelocity;
        // final Vehicle v = newVehicle(pos, vel, Lanes.LANE1);
        // r0.addVehicle(v);
        // final Vehicle w = new Vehicle(v);
        // vehicles.add(w);
        // }
        // assertEquals(true, r0.eachLaneIsSorted());
        //
        // final double dt = 0.25;
        // final double simulationTime = 0.0;
        // final long iterationCount = 0;
        // r0.updateVehiclePositionsAndVelocities(dt, simulationTime, iterationCount);
        // assertEquals(true, r0.eachLaneIsSorted());
        //
        // final int count = vehicles.size();
        // for (int i = count - 1; i >= 2; i--) {
        // final Vehicle vehicle = vehicles.get(i);
        // final Vehicle frontVehicle = vehicles.get(i - 1);
        // if (Vehicle.integrationType() == Vehicle.IntegrationType.EULER) {
        // vehicle.eulerIntegrate(dt, frontVehicle);
        // } else {
        // final Vehicle frontFrontVehicle = vehicles.get(i - 2);
        // vehicle.rungeKuttaIntegrate(dt, frontVehicle, frontFrontVehicle);
        // }
        // }
        // final Vehicle w1 = vehicles.get(1);
        // final Vehicle w0 = vehicles.get(0);
        // if (Vehicle.integrationType() == Vehicle.IntegrationType.EULER) {
        // w1.eulerIntegrate(dt, w0);
        // w0.eulerIntegrate(dt, null);
        // } else {
        // w1.rungeKuttaIntegrate(dt, w0, null);
        // w0.rungeKuttaIntegrate(dt, null, null);
        // }
        // for (int i = 0; i < vehicleCount; ++i) {
        // final Vehicle v = r0.getVehicle(Lanes.LANE1, i);
        // final Vehicle w = vehicles.get(i);
        // assertEquals(w.getPosition(), v.getPosition(), delta);
        // assertEquals(w.getSpeed(), v.getSpeed(), delta);
        // }
    }

    /**
     * Test method for {@link org.movsim.simulator.roadnetwork.RoadSegment#inFlow(double, double, long)}
     */
    @Test
    public final void testInFlow() {
        // fail("Not yet implemented"); //$NON-NLS-1$
    }

    /**
     * Test method for {@link org.movsim.simulator.roadnetwork.RoadSegment#outFlow(double, double, long)}
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

        final Vehicle v0 = newVehicle(999.0, 40.0, Lanes.LANE1);
        r0.addVehicle(v0);
        final double dt = 0.25;
        final double simulationTime = 0.0;
        final long iterationCount = 0;
        r0.updateVehiclePositionsAndSpeeds(dt, simulationTime, iterationCount);
        assertEquals(1009.0, v0.getRearPosition(), delta);
        r0.outFlow(dt, simulationTime, iterationCount);
        assertEquals(0, r0.getVehicleCount());
        assertEquals(1, r1.getVehicleCount());
        final Vehicle v = r1.getVehicle(Lanes.LANE1, 0);
        assertEquals(9.0, v.getRearPosition(), delta);
    }

    @Test
    public final void testOutFlowTrafficLane() {
        RoadSegment.resetNextId();
        Vehicle.resetNextId();

        final int laneCount = 3;
        final RoadSegment r0 = new RoadSegment(1000.0, laneCount);
        final RoadSegment r1 = new RoadSegment(5000.0, laneCount + 1);
        r1.setLaneType(Lanes.LANE1, Lanes.Type.ENTRANCE);
        // join r0 and r1 so vehicles move from r0 to r1
        Link.addJoin(r0, r1);

        final Vehicle v1 = newVehicle(999.0, 40.0, Lanes.LANE1);
        r0.addVehicle(v1);
        final Vehicle v2 = newVehicle(998.0, 40.0, Lanes.LANE2);
        r0.addVehicle(v2);
        final Vehicle v3 = newVehicle(997.0, 40.0, Lanes.LANE3);
        r0.addVehicle(v3);

        final double dt = 0.25;
        final double simulationTime = 0.0;
        final long iterationCount = 0;
        r0.updateVehiclePositionsAndSpeeds(dt, simulationTime, iterationCount);
        // assertEquals(1009.0, v1.getPosition(), delta);
        // assertEquals(1008.0, v2.getPosition(), delta);
        // assertEquals(1007.0, v3.getPosition(), delta);
        // r0.outFlow(dt, simulationTime, iterationCount);
        // assertEquals(0, r0.totalVehicleCount());
        // assertEquals(3, r1.totalVehicleCount());
        // assertEquals(Lanes.LANE2, v1.getLane());
        // assertEquals(Lanes.LANE3, v2.getLane());
        // assertEquals(Lanes.LANE4, v3.getLane());
        // final Vehicle nv1 = r1.getVehicle(Lanes.LANE2, 0);
        // assertEquals(9.0, nv1.getPosition(), delta);
        // final Vehicle nv2 = r1.getVehicle(Lanes.LANE3, 0);
        // assertEquals(8.0, nv2.getPosition(), delta);
        // final Vehicle nv3 = r1.getVehicle(Lanes.LANE4, 0);
        // assertEquals(7.0, nv3.getPosition(), delta);
    }

    /**
     * Test method for {@link org.movsim.simulator.roadnetwork.RoadSegment#eachLaneIsSorted()}
     */
    @Test
    public final void testEachLaneIsSorted() {
        // fail("Not yet implemented"); //$NON-NLS-1$
    }

    /**
     * Test method for {@link org.movsim.simulator.roadnetwork.RoadSegment#iterator()}
     */
    @SuppressWarnings("boxing")
    @Test
    public final void testIterator() {
        // fail("Not yet implemented");
        RoadSegment.resetNextId();
        Vehicle.resetNextId();

        final int laneCount = 3;
        final RoadSegment r0 = new RoadSegment(1000.0, laneCount);

        final Vehicle v0 = newVehicle(800.0, 1.0, Lanes.LANE2);
        r0.addVehicle(v0);
        final Vehicle v1 = newVehicle(700.0, 1.0, Lanes.LANE3);
        r0.addVehicle(v1);
        final Vehicle v2 = newVehicle(600.0, 1.0, Lanes.LANE3);
        r0.addVehicle(v2);
        final Iterator<Vehicle> iterator = r0.iterator();
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

        final Vehicle v0 = newVehicle(800.0, 1.0, Lanes.LANE1);
        r0.addVehicle(v0);
        final Vehicle v1 = newVehicle(700.0, 1.0, Lanes.LANE1);
        r0.addVehicle(v1);
        final Vehicle v2 = newVehicle(600.0, 1.0, Lanes.LANE3);
        r0.addVehicle(v2);
        final Iterator<Vehicle> iterator = r0.iterator();
        assertEquals(true, iterator.hasNext());
        assertEquals(v0, iterator.next());
        assertEquals(true, iterator.hasNext());
        assertEquals(v1, iterator.next());
        assertEquals(true, iterator.hasNext());
        assertEquals(v2, iterator.next());
        assertEquals(false, iterator.hasNext());
    }

    @Test
    public final void testLaneSegmentIterator() {
        RoadSegment.resetNextId();
        Vehicle.resetNextId();

        final int laneCount = 3;
        final RoadSegment r0 = new RoadSegment(1000.0, laneCount);

        final Iterator<LaneSegment> iterator = r0.laneSegmentIterator();

        assertEquals(true, iterator.hasNext());
        LaneSegment laneSegment = r0.laneSegment(Lanes.LANE1);
        LaneSegment next = iterator.next();
        assertEquals(laneSegment.lane(), next.lane());

        assertEquals(true, iterator.hasNext());
        laneSegment = r0.laneSegment(Lanes.LANE2);
        next = iterator.next();
        assertEquals(laneSegment.lane(), next.lane());

        assertEquals(true, iterator.hasNext());
        laneSegment = r0.laneSegment(Lanes.LANE3);
        next = iterator.next();
        assertEquals(laneSegment.lane(), next.lane());
    }
}
