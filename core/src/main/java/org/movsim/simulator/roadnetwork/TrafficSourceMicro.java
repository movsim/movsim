package org.movsim.simulator.roadnetwork;

import java.util.SortedMap;
import java.util.TreeMap;

import org.movsim.simulator.vehicles.TrafficCompositionGenerator;
import org.movsim.simulator.vehicles.Vehicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

public class TrafficSourceMicro extends AbstractTrafficSource {

    private static final Logger LOG = LoggerFactory.getLogger(TrafficSourceMicro.class);

    private final SortedMap<Long, Vehicle> vehicleQueue = new TreeMap<>();

    public TrafficSourceMicro(TrafficCompositionGenerator vehGenerator, RoadSegment roadSegment) {
        super(vehGenerator, roadSegment);
    }

    public void addVehicleToQueue(long time, Vehicle vehicle) {
        Preconditions.checkArgument(vehicleQueue.put(time, vehicle) == null);
        LOG.debug("added vehicle with (re)entering-time={}, queueSize={}", time, vehicleQueue.size());
        // vehicle.setSpeed(0);
        showQueue();
    }

    private void showQueue() {
        for (Long entryTime : vehicleQueue.keySet()) {
            LOG.debug("entryTime={}", entryTime);
        }
    }

    @Override
    public void timeStep(double dt, double simulationTime, long iterationCount) {
        calcApproximateInflow(dt);
        if (vehicleQueue.isEmpty()) {
            return;
        }
        Long entryTime = vehicleQueue.firstKey();
        if (simulationTime >= entryTime.longValue()) {
            Vehicle vehicle = vehicleQueue.get(entryTime);
            int testLane = (vehicle.lane() != Vehicle.LANE_NOT_SET) ? vehicle.lane()
                    : getNewCyclicLaneForEntering(laneEnterLast);
            LaneSegment laneSegment = roadSegment.laneSegment(testLane);
            final boolean isEntered = tryEnteringNewVehicle(vehicle, laneSegment);
            if (isEntered) {
                vehicleQueue.remove(entryTime);
                incrementInflowCount(1);
                recordData(simulationTime, 0);
            }
        }
    }

    private boolean tryEnteringNewVehicle(Vehicle vehicle, LaneSegment laneSegment) {
        Vehicle leader = laneSegment.rearVehicle();
        double vEnter = vehicle.getSpeed();
        if (leader == null) {
            enterVehicle(laneSegment, vEnter, vehicle);
            return true;
        }
        vEnter = Math.min(vEnter, leader.getSpeed());
        // check if gap to leader is sufficiently large (xEnter of road section is assumed to be zero)
        final double netGapToLeader = leader.getRearPosition();
        // very crude approximation for minimum gap
        double minRequiredGap = vehicle.getEffectiveLength() + 2 * vehicle.getLongitudinalModel().getDesiredSpeed();
        if (vehicle.getLongitudinalModel().isCA()) {
            minRequiredGap = leader.getSpeed();
        }
        if (netGapToLeader > minRequiredGap) {
            enterVehicle(laneSegment, vEnter, vehicle);
            return true;
        }
        return false;
    }

    private void enterVehicle(LaneSegment laneSegment, double vEnter, Vehicle vehicle) {
        double xEnter = 0;
        initVehicle(laneSegment, xEnter, vEnter, vehicle);
        LOG.info("add vehicle from upstream boundary to empty road: xEnter={}, vEnter={}", xEnter, vEnter);
    }

    @Override
    public double getTotalInflow(double time) {
        return 0; // no flow-based input
    }

}
