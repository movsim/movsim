package org.movsim.simulator.roadnetwork;

import java.util.List;

import org.movsim.simulator.roadnetwork.MicroInflowQueue.MicroInflowRecord;
import org.movsim.simulator.vehicles.TrafficCompositionGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrafficSourceMicro extends AbstractTrafficSource {

    static final Logger LOG = LoggerFactory.getLogger(TrafficSourceMicro.class);

    private List<MicroInflowRecord> inflowQueue;

    public TrafficSourceMicro(TrafficCompositionGenerator vehGenerator, RoadSegment roadSegment,
            List<MicroInflowRecord> inflowQueue) {
        super(vehGenerator, roadSegment);
        this.inflowQueue = inflowQueue;
    }

    @Override
    public void timeStep(double dt, double simulationTime, long iterationCount) {
        // TODO Auto-generated method stub

    }

    @Override
    public double getTotalInflow(double time) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double measuredInflow() {
        // TODO Auto-generated method stub
        return 0;
    }

}
