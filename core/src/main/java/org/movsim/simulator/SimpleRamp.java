package org.movsim.simulator;

import java.util.List;

import org.movsim.input.model.simulation.InflowDataPoint;
import org.movsim.input.model.simulation.SimpleRampData;
import org.movsim.simulator.roadnetwork.RoadSegment;
import org.movsim.simulator.vehicles.VehicleGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleRamp {

    final static Logger logger = LoggerFactory.getLogger(SimpleRamp.class);

    private final List<InflowDataPoint> inflowTimeSeries;

    public SimpleRamp(VehicleGenerator roadVehGenerator, RoadSegment roadSegment, SimpleRampData simpleRampData) {
        inflowTimeSeries = simpleRampData.getInflowTimeSeries();
        if (inflowTimeSeries.isEmpty()) {
            logger.info("no inflow data for onramp. Do nothing.");
        }
    }


}
