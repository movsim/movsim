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
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.movsim.autogen.Inflow;

public class InflowTimeSeriesTest {
    private static final double delta = 0.00001;
	private InflowTimeSeries inflowTimeSeries;

	@Before
	public void setUp() throws Exception {
        final List<Inflow> inflowDataPoints = new ArrayList<>();
		// time, flowPerHour, speed
        inflowDataPoints.add(createInflowDataPoint(0.0, 0.0, 24.0));
        inflowDataPoints.add(createInflowDataPoint(600.0, 1200.0, 24.0));
        inflowDataPoints.add(createInflowDataPoint(900.0, 1800.0, 12.0));
		inflowTimeSeries = new InflowTimeSeries(inflowDataPoints);
	}

    private static Inflow createInflowDataPoint(double time, double flow, double speed) {
        Inflow inflow = new Inflow();
        inflow.setQPerHour(flow);
        inflow.setT(time);
        inflow.setV(speed);
        return inflow;
    }

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public final void testInflowTimeSeries() {
		assertNotNull(inflowTimeSeries);
	}

	@Test
	public final void testGetFlowPerLane() {
        assertEquals(0.0, inflowTimeSeries.getFlowPerLane(0.0), delta);
        assertEquals(600.0 / 3600.0, inflowTimeSeries.getFlowPerLane(300.0), delta);
        assertEquals(1200.0 / 3600.0, inflowTimeSeries.getFlowPerLane(600.0), delta);
        assertEquals(1500.0 / 3600.0, inflowTimeSeries.getFlowPerLane(750.0), delta);
        assertEquals(1800.0 / 3600.0, inflowTimeSeries.getFlowPerLane(900.0), delta);
	}

	@Test
	public final void testGetSpeed() {
        assertEquals(24.0, inflowTimeSeries.getSpeed(0.0), delta);
        assertEquals(24.0, inflowTimeSeries.getSpeed(300.0), delta);
        assertEquals(24.0, inflowTimeSeries.getSpeed(600.0), delta);
        assertEquals(18.0, inflowTimeSeries.getSpeed(750.0), delta);
        assertEquals(12.0, inflowTimeSeries.getSpeed(900.0), delta);
	}

}
