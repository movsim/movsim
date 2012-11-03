package org.movsim.simulator.roadnetwork;

import org.movsim.input.model.simulation.SimpleRampData;
import org.movsim.simulator.vehicles.VehicleGenerator;
import org.movsim.utilities.Units;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleRamp extends AbstractTrafficSource {

    final static Logger logger = LoggerFactory.getLogger(SimpleRamp.class);

    private final double relativeGapToLeader;

    private final double relativeSpeedToLeader;

    public SimpleRamp(VehicleGenerator vehGenerator, RoadSegment roadSegment, SimpleRampData simpleRampData,
            InflowTimeSeries inflowTimeSeries) {
        super(vehGenerator, roadSegment, inflowTimeSeries);
        this.relativeSpeedToLeader = simpleRampData.getRelativeSpeedToLeader();
        this.relativeGapToLeader = simpleRampData.getRelativeGapToLeader();
    }

    public void timeStep(double dt, double simulationTime, long iterationCount) {
        logger.debug("simple ramp timestep={}", simulationTime);
        System.out.println("simple ramp timestep=" + simulationTime + ", current inflow="
                + (int) (getTotalInflow(simulationTime) * Units.INVS_TO_INVH) + ", waiting vehicles="
                + getQueueLength());

        final double totalInflow = getTotalInflow(simulationTime);
        nWait += totalInflow * dt;

        if (recordDataCallback != null) {
            recordDataCallback.recordData(simulationTime, laneEnterLast, xEnterLast, vEnterLast, totalInflow,
                    enteringVehCounter, 0);
        }
    }

}
