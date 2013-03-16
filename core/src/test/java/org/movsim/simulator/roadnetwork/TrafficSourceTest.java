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

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.movsim.autogen.Inflow;
import org.movsim.simulator.vehicles.TrafficCompositionGenerator;

/**
 *
 */
public class TrafficSourceTest {

	private TrafficSourceMacro trafficSource;

    /**
     * Sets up the test fixture. 
     * (Called before every test case method.)
     */
    @Before
    public void setUp() throws Exception {
        final TrafficCompositionGenerator vehicleGenerator = null;
		final RoadSegment roadSegment = new RoadSegment(1000.0, 1);
        final List<Inflow> inflowDataPoints = new ArrayList<>();
		final InflowTimeSeries inflowTimeSeries = new InflowTimeSeries(inflowDataPoints);
		trafficSource = new TrafficSourceMacro(vehicleGenerator, roadSegment, inflowTimeSeries);
    }

    /**
     * Tears down the test fixture. 
     * (Called after every test case method.)
     */
    @After
    public void tearDown() throws Exception {
    }

	/**
	 * Test method for {@link org.movsim.simulator.roadnetwork.TrafficSourceMacro#TrafficSource(org.movsim.simulator.vehicles.VehicleGeneratorOld, org.movsim.simulator.roadnetwork.RoadSegment, org.movsim.simulator.roadnetwork.InflowTimeSeries)}
	 */
	@Test
	public final void testTrafficSource() {
		assertNotNull(trafficSource);
	}

	/**
	 * Test method for {@link org.movsim.simulator.roadnetwork.TrafficSourceMacro#setRecorder(org.movsim.simulator.roadnetwork.TrafficSourceMacro.RecordDataCallback)}
	 */
	@Test
	public final void testSetRecorder() {
		//fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.movsim.simulator.roadnetwork.TrafficSourceMacro#getEnteringVehCounter()}
	 */
	@Test
	public final void testGetEnteringVehCounter() {
		//fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.movsim.simulator.roadnetwork.TrafficSourceMacro#timeStep(double, double, long)}.
	 */
	@Test
	public final void testTimeStep() {
		//fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.movsim.simulator.roadnetwork.TrafficSourceMacro#setFlowPerLane(double)}.
	 */
	@Test
	public final void testSetFlowPerLane() {
		//fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.movsim.simulator.roadnetwork.TrafficSourceMacro#getFlowPerLane(double)}.
	 */
	@Test
	public final void testGetFlowPerLane() {
		//fail("Not yet implemented");
	}
}
