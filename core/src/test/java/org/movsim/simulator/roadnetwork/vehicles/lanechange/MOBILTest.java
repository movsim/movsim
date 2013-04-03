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
package org.movsim.simulator.roadnetwork.vehicles.lanechange;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.movsim.autogen.LaneChangeModelType;
import org.movsim.autogen.ModelParameterMOBIL;
import org.movsim.simulator.roadnetwork.LaneSegment;
import org.movsim.simulator.roadnetwork.Lanes;
import org.movsim.simulator.roadnetwork.Link;
import org.movsim.simulator.roadnetwork.RoadSegment;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.simulator.vehicles.lanechange.LaneChangeModel;
import org.movsim.simulator.vehicles.lanechange.MOBIL;
import org.movsim.simulator.vehicles.longitudinalmodel.acceleration.IDM;

/**
 * Test module for the MOBIL class.
 * 
 */
@SuppressWarnings("static-method")
public class MOBILTest {
    private static final double delta = 0.00001;

    private Vehicle newVehicle(double rearPosition, double speed, int lane, double length) {
        final IDM idm = new IDM(33.0, 0.5, 3.0, 1.5, 2.0, 5.0);
        final Vehicle vehicle = new Vehicle(rearPosition, speed, lane, length, 2.5);
        vehicle.setLongitudinalModel(idm);
        vehicle.setSpeedlimit(80.0 / 3.6); // 80 km/h
        return vehicle;
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test method for {@link org.movsim.simulator.vehicles.lanechange.MOBIL#MOBIL(org.movsim.simulator.vehicles.Vehicle)}.
     */
    @Test
    public final void testMOBILVehicle() {
        //fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.movsim.simulator.vehicles.lanechange.MOBIL#MOBIL(org.movsim.simulator.vehicles.Vehicle, org.movsim.input.model.vehicle.lanechange.LaneChangeMobilData)}.
     */
    @Test
    public final void testMOBILVehicleLaneChangeMobilData() {
        //fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.movsim.simulator.vehicles.lanechange.MOBIL#MOBIL(org.movsim.simulator.vehicles.Vehicle, double, double, double, double, double)}.
     */
    @Test
    public final void testMOBILVehicleDoubleDoubleDoubleDoubleDouble() {
        //fail("Not yet implemented");
    }

    /**
     * Test method for {@link org.movsim.simulator.vehicles.lanechange.MOBIL#calcAccelerationBalance(int, org.movsim.simulator.roadnetwork.RoadSegment)}.
     */
    @Test
    public final void testCalcAccelerationBalance() {
        final double lengthCar = 6.0;
        // final double lengthTruck = 16.0;
        RoadSegment.resetNextId();
        Vehicle.resetNextId();
        final double roadLength = 1000.0;
        final int laneCount = 2;
        final RoadSegment roadSegment = new RoadSegment(roadLength, laneCount);
        final double minimumGap = 2.0;
        final double tooSmallGap = 1.0;
        final double safeDeceleration = 4.0;
        final double politeness = 0.1;
        final double thresholdAcceleration = 0.2;
        final double rightBiasAcceleration = 0.3;

        // set up a vehicle in most inner lane (left lane)
        final Vehicle v1 = newVehicle(900.0, 0.0, Lanes.LANE1, lengthCar);
        final MOBIL m1 = new MOBIL(v1, createModelParameterMOBIL(minimumGap, safeDeceleration, politeness,
                thresholdAcceleration, rightBiasAcceleration));
        final LaneChangeModel lcm1 = new LaneChangeModel(v1, createLaneChangeModelType(m1.getParameter()));
        v1.setLaneChangeModel(lcm1);
        roadSegment.addVehicle(v1);

        // set up a vehicle in right lane
        final Vehicle v2 = newVehicle(900.0 - lengthCar - tooSmallGap, 0.0, Lanes.LANE2, lengthCar);
        final MOBIL m2 = new MOBIL(v2, createModelParameterMOBIL(minimumGap, safeDeceleration, politeness,
                thresholdAcceleration, rightBiasAcceleration));
        final LaneChangeModel lcm2 = new LaneChangeModel(v2, createLaneChangeModelType(m2.getParameter()));
        v2.setLaneChangeModel(lcm2);
        roadSegment.addVehicle(v2);

        // vehicles too close together, so acceleration balance should be large negative
        double balance = m1.calcAccelerationBalance(v1, Lanes.TO_RIGHT, roadSegment);
        assertEquals(-Double.MAX_VALUE, balance, delta);
        balance = m2.calcAccelerationBalance(v2, Lanes.TO_LEFT, roadSegment);
        assertEquals(-Double.MAX_VALUE, balance, delta);

        // now set up with sufficient gap between vehicles, but v2 needs to decelerate, so it is not
        // favourable to change lanes
        roadSegment.clearVehicles();
        v2.setFrontPosition(v1.getRearPosition() - 2 * minimumGap);
        v2.setSpeed(80.0 / 3.6);
        roadSegment.addVehicle(v1);
        roadSegment.addVehicle(v2);
        balance = m2.calcAccelerationBalance(v2, Lanes.TO_LEFT, roadSegment);
        assertTrue(balance < 0.0);

        // now set up with sufficient gap between vehicles, but v1 needs to brake heavily, so it is not
        // safe to change lanes
        roadSegment.clearVehicles();
        v2.setRearPosition(v1.getFrontPosition() + 2 * minimumGap);
        v2.setSpeed(80.0 / 3.6); // 80 km/h
        v1.setSpeed(120.0 / 3.6); // 120 km/h
        roadSegment.addVehicle(v1);
        roadSegment.addVehicle(v2);
        balance = m2.calcAccelerationBalance(v2, Lanes.TO_LEFT, roadSegment);
        assertEquals(-Double.MAX_VALUE, balance, delta);
    }

    @Test
    public final void testCalcAccelerationBalance2() {
        RoadSegment.resetNextId();
        Vehicle.resetNextId();
        final int laneCount = 2;
        final int exitLaneCount = 1;
        final RoadSegment r0 = new RoadSegment(300.0, laneCount + exitLaneCount);
        final RoadSegment r1 = new RoadSegment(400.0, laneCount);
        r0.setLaneType(Lanes.LANE3, Lanes.Type.EXIT);// so Lane3 is exit lane of r1
        // join r0 and r1 so vehicles move from r0 to r1
        // lane1 of r0 joins to lane1 of r1
        // lane2 of r0 joins to lane2 of r1
        // lane3 of r0 has no successor
        Link.addJoin(r0, r1);
        assertEquals(Lanes.LANE1, r1.sourceLane(Lanes.LANE1));
        assertEquals(Lanes.LANE2, r1.sourceLane(Lanes.LANE2));
        final double lengthCar = 6.0;
        final double minimumGap = 2.0;
        // final double tooSmallGap = 1.0;
        final double safeDeceleration = 4.0;
        final double politeness = 0.1;
        final double thresholdAcceleration = 0.2;
        final double rightBiasAcceleration = 0.3;

        // set up a vehicle in lane 2
        final Vehicle v1 = newVehicle(293.1, 26.983, Lanes.LANE1, lengthCar);
        // final MOBIL m1 = new MOBIL(v1, minimumGap, safeDeceleration, politeness, thresholdAcceleration, rightBiasAcceleration);
        r0.addVehicle(v1);

        final Vehicle v2 = newVehicle(6.3, 5.589, Lanes.LANE2, lengthCar);
        final MOBIL m2 = new MOBIL(v2, createModelParameterMOBIL(minimumGap, safeDeceleration, politeness,
                thresholdAcceleration, rightBiasAcceleration));
        r1.addVehicle(v2);
        final LaneSegment sls = r1.sourceLaneSegment(Lanes.LANE1);
        assertEquals(1, sls.vehicleCount());

        final Vehicle v3 = newVehicle(25.0, 4.0, Lanes.LANE2, lengthCar);
        // final MOBIL m3 = new MOBIL(v3, minimumGap, safeDeceleration, politeness, thresholdAcceleration, rightBiasAcceleration);
        r1.addVehicle(v3);

        final Vehicle rV = r1.rearVehicle(Lanes.LANE1, v2.getRearPosition());
        assertEquals(v1.getId(), rV.getId());

        double balance = m2.calcAccelerationBalance(v2, Lanes.TO_LEFT, r1);
        assertTrue(balance < 0.0);
    }

    /**
     * Test method for {@link org.movsim.simulator.vehicles.lanechange.MOBIL#getMinimumGap()}.
     */
    @Test
    public final void testGetMinimumGap() {
        final double minimumGap = 2.1;
        final MOBIL mobil = new MOBIL(null, createModelParameterMOBIL(minimumGap, 0.0, 0.0, 0.0, 0.0));
        assertEquals(minimumGap, mobil.getParameter().getMinimumGap(), delta);
    }

    /**
     * Test method for {@link org.movsim.simulator.vehicles.lanechange.MOBIL#getSafeDeceleration()}.
     */
    @Test
    public final void testGetSafeDeceleration() {
        final double safeDeceleration = 4.3;
        final MOBIL mobil = new MOBIL(null, createModelParameterMOBIL(0.0, safeDeceleration, 0.0, 0.0, 0.0));
        assertEquals(safeDeceleration, mobil.getParameter().getSafeDeceleration(), delta);
    }

    private static ModelParameterMOBIL createModelParameterMOBIL(double minimumGap, double safeDeceleration,
            double politeness, double thresholdAcceleration, double rightBiasAcceleration) {
        ModelParameterMOBIL param = new ModelParameterMOBIL();
        param.setMinimumGap(minimumGap);
        param.setSafeDeceleration(safeDeceleration);
        param.setPoliteness(politeness);
        param.setThresholdAcceleration(thresholdAcceleration);
        param.setRightBiasAcceleration(rightBiasAcceleration);
        return param;
    }

    private static LaneChangeModelType createLaneChangeModelType(ModelParameterMOBIL mobilParameter) {
        LaneChangeModelType lcType = new LaneChangeModelType();
        lcType.setModelParameterMOBIL(mobilParameter);
        lcType.setEuropeanRules(true);
        lcType.setCritSpeedEur(5);
        return lcType;
    }

}
