package org.movsim.simulator.roadnetwork;

import java.util.List;
import java.util.Map;

import org.movsim.simulator.roadnetwork.MicroInflowQueue.MicroInflowRecord;
import org.movsim.simulator.vehicles.TrafficCompositionGenerator;
import org.movsim.simulator.vehicles.Vehicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

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
        createVehicleFromInput();
    }

    private void createVehicleFromInput() {
        for (MicroInflowRecord record : inflowQueue) {
            final Vehicle vehicle = vehGenerator.createVehicle(record.getTypeLabel());
            record.setVehicle(vehicle);
            if (record.hasRoute()) {
                Preconditions.checkArgument(routes.containsKey(record.getRoute()), "route=" + record.getRoute()
                        + " in microscopic boundary input on roadSegment=" + roadSegment.id() + " not defined!");
                Route route = routes.get(record.getRoute());
                LOG.info("overwrites vehicle's default route by route provided by input file: route={}",
                        route.getName());
                vehicle.setRoute(route);
            }
            if (record.hasComment()) {
                vehicle.setInfoComment(record.getComment());
            }
            if (record.hasLength()) {
                vehicle.setLength(record.getLength());
            }
            if (record.hasWeight()) {
                vehicle.setWeight(record.getWeight());
            }
            if (record.hasLength() || record.hasWeight()) {
                LOG.info("and set individual length or weight: length={}, weight={}", vehicle.getLength(),
                        vehicle.getWeight());
            }
        }
    }

    @Override
    public void timeStep(double dt, double simulationTime, long iterationCount) {
        calcApproximateInflow(dt);
        nextInflowRecord = getNextVehicleRecord(simulationTime);
        if (nextInflowRecord != null) {
            int testLane = nextInflowRecord.hasLane() ? nextInflowRecord.getLane()
                    : getNewCyclicLaneForEntering(laneEnterLast);
            LaneSegment laneSegment = roadSegment.laneSegment(testLane);
            Vehicle vehicle = nextInflowRecord.getVehicle();
            final boolean isEntered = tryEnteringNewVehicle(vehicle, laneSegment);
            if (isEntered) {
                nextInflowRecord = null;
                incrementInflowCount(1);
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

    private boolean tryEnteringNewVehicle(Vehicle vehicle, LaneSegment laneSegment) {
        Vehicle leader = laneSegment.rearVehicle();
        double vEnter = nextInflowRecord.hasSpeed() ? nextInflowRecord.getSpeed() : 0;
        if (leader == null) {
            enterVehicle(laneSegment, vEnter, vehicle);
            return true;
        }
        vEnter = Math.min(vEnter, leader.getSpeed());
        // check if gap to leader is sufficiently large (xEnter of road section is assumed to be zero)
        final double netGapToLeader = leader.getRearPosition();
        // very crude approximation for minimum gap
        double minRequiredGap = vehicle.getLength() + vehicle.getLongitudinalModel().getMinimumGap() + 2
                * vehicle.getLongitudinalModel().getDesiredSpeed();
        if (vehicle.getLongitudinalModel().isCA()) {
            minRequiredGap = leader.getSpeed();
        }
        if (netGapToLeader > minRequiredGap) {
            enterVehicle(laneSegment, vEnter, vehicle);
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
    private void enterVehicle(LaneSegment laneSegment, double vEnter, Vehicle vehicle) {
        final double xEnter = 0;
        initVehicle(laneSegment, xEnter, vEnter, vehicle);
        LOG.info("add vehicle from upstream boundary to empty road: xEnter={}, vEnter={}", xEnter, vEnter);
    }

    @Override
    public double getTotalInflow(double time) {
        return 0; // no flow-based input
    }

}
