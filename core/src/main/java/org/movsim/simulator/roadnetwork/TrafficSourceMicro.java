package org.movsim.simulator.roadnetwork;

import java.util.List;
import java.util.Map;

import org.movsim.simulator.roadnetwork.MicroInflowQueue.MicroInflowRecord;
import org.movsim.simulator.vehicles.TestVehicle;
import org.movsim.simulator.vehicles.TrafficCompositionGenerator;
import org.movsim.simulator.vehicles.Vehicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrafficSourceMicro extends AbstractTrafficSource {

    private static final Logger LOG = LoggerFactory.getLogger(TrafficSourceMicro.class);

    private final List<MicroInflowRecord> inflowQueue;

    private MicroInflowRecord nextInflowRecord;

    private Map<String, Route> routes;

    public TrafficSourceMicro(TrafficCompositionGenerator vehGenerator, Map<String, Route> routes,
            RoadSegment roadSegment, List<MicroInflowRecord> inflowQueue) {
        super(vehGenerator, roadSegment);
        this.routes = routes;
        this.inflowQueue = inflowQueue;
        checkInput();
    }

    private void checkInput() {
        for (MicroInflowRecord record : inflowQueue) {
            if (!vehGenerator.hasVehicle(record.getTypeLabel())) {
                throw new IllegalArgumentException(
                        "vehicle type in microscopic boundary input (from file) not defined in traffic composition="
                                + record.getTypeLabel());
            }
            if (record.hasRoute() && !routes.containsKey(record.getRoute())) {
                throw new IllegalArgumentException("route=" + record.getRoute()
                        + " in microscopic boundary input on roadSegment=" + roadSegment.id() + " not defined!");
            }
        }
    }

    @Override
    public void timeStep(double dt, double simulationTime, long iterationCount) {
        nextInflowRecord = getNextVehicleRecord(simulationTime);
        if (nextInflowRecord != null) {
            int testLane = nextInflowRecord.hasLane() ? nextInflowRecord.getLane()
                    : getNewCyclicLaneIndexForEntering(laneEnterLast);
            if (testLane >= roadSegment.laneCount()) {
                // already handled in parsing, should not occur here
                LOG.error("Reduce init lane because lane={} not available for roadSegment={}", testLane,
                        roadSegment.id());
                testLane = roadSegment.laneCount() - 1;
            }
            LaneSegment laneSegment = roadSegment.laneSegment(testLane);
            TestVehicle testVehicle = vehGenerator.getTestVehicle();
            final boolean isEntered = tryEnteringNewVehicle(testVehicle, laneSegment);
            if (isEntered) {
                nextInflowRecord = null;
                // measuredInflowCount++;
                recordData(simulationTime, 0);
            }
        }
    }

    private MicroInflowRecord getNextVehicleRecord(double simulationTime) {
        MicroInflowRecord microInflowRecord = nextInflowRecord;
        if (microInflowRecord != null) {
            // not yet entered
            return microInflowRecord;
        }
        if (!inflowQueue.isEmpty() && inflowQueue.get(0).getTime() < simulationTime) {
            microInflowRecord = inflowQueue.get(0);
            inflowQueue.remove(0);
        }
        return microInflowRecord;
    }

    private boolean tryEnteringNewVehicle(TestVehicle testVehicle, LaneSegment laneSegment) {
        Vehicle leader = laneSegment.rearVehicle();
        double vEnter = nextInflowRecord.hasSpeed() ? nextInflowRecord.getSpeed() : 0;
        if (leader == null) {
            enterVehicle(laneSegment, vEnter, testVehicle);
            return true;

        }
        vEnter = Math.min(vEnter, leader.getSpeed());
        // (2) check if gap to leader is sufficiently large origin of road section is assumed to be zero
        final double netGapToLeader = leader.getRearPosition();
        final double gapAtQMax = 1. / testVehicle.getRhoQMax();
        // minimal distance set to 80% of 1/rho at flow maximum in fundamental diagram
        double minRequiredGap = 0.8 * gapAtQMax;
        if (testVehicle.getLongModel().isCA()) {
            minRequiredGap = leader.getSpeed();
        }
        if (netGapToLeader > minRequiredGap) {
            enterVehicle(laneSegment, vEnter, testVehicle);
            return true;
        }
        return false;
    }

    /**
     * Enter vehicle on empty road.
     * 
     * @param laneSegment
     * @param time
     *            the time
     * @param vehPrototype
     *            the vehicle prototype
     */
    private void enterVehicle(LaneSegment laneSegment, double vEnter, TestVehicle testVehicle) {
        final double xEnter = 0;
        Vehicle addedVehicle = addVehicle(laneSegment, testVehicle, xEnter, vEnter);
        LOG.info("add vehicle from upstream boundary to empty road: xEnter={}, vEnter={}", xEnter, vEnter);
        if (nextInflowRecord.hasRoute()) {
            addedVehicle.setRoute(routes.get(nextInflowRecord.getRoute()));
            LOG.info("overwrote vehicle's default route (from composition) by route from input file: route={}",
                    nextInflowRecord.getRoute());
        }
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
