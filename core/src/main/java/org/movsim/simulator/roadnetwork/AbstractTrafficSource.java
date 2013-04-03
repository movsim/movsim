package org.movsim.simulator.roadnetwork;

import javax.annotation.Nullable;

import org.movsim.simulator.SimulationTimeStep;
import org.movsim.simulator.vehicles.TestVehicle;
import org.movsim.simulator.vehicles.TrafficCompositionGenerator;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.utilities.Units;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

public abstract class AbstractTrafficSource implements SimulationTimeStep {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(AbstractTrafficSource.class);

    public interface RecordDataCallback {
        /**
         * Callback to allow the application to process or record the traffic source data.
         * 
         */
        public void recordData(double simulationTime, int laneEnter, double xEnter, double vEnter, double totalInflow,
                int enteringVehCounter, double nWait);
    }

    RecordDataCallback recordDataCallback;

    void recordData(double simulationTime, final double totalInflow) {
        if (recordDataCallback != null) {
            recordDataCallback.recordData(simulationTime, laneEnterLast, xEnterLast, vEnterLast, totalInflow,
                    enteringVehCounter, nWait);
        }
    }

    protected static final double MEASURING_INTERVAL_S = 60.0;
    private double measuredTime;
    private int measuredInflowCount;
    protected double measuredInflow;

    int enteringVehCounter;

    /** The x enter last. status of last merging vehicle for logging to file */
    double xEnterLast;

    double vEnterLast;

    int laneEnterLast;

    /** number of vehicles in the queue as result from integration over demand minus inserted vehicles. */
    double nWait;

    final TrafficCompositionGenerator vehGenerator;

    final RoadSegment roadSegment;

    public AbstractTrafficSource(@Nullable TrafficCompositionGenerator vehGenerator, RoadSegment roadSegment) {
        this.vehGenerator = vehGenerator;
        this.roadSegment = Preconditions.checkNotNull(roadSegment);
        nWait = 0;
        measuredInflow = 0;
        measuredTime = 0;
        measuredInflowCount = 0;
    }

    /**
     * Sets the traffic source recorder.
     * 
     * @param recordDataCallback
     */
    public void setRecorder(RecordDataCallback recordDataCallback) {
        enteringVehCounter = 0;
        this.recordDataCallback = recordDataCallback;
    }

    /**
     * Gets the entering veh counter.
     * 
     * @return the entering veh counter
     */
    public int getEnteringVehCounter() {
        return enteringVehCounter;
    }

    /**
     * Gets the total inflow over all lanes.
     * 
     * @param time
     *            the time
     * @return the total inflow over all lanes
     */
    public abstract double getTotalInflow(double time);

    /**
     * Returns the number of vehicles in the queue.
     * 
     * @return integer queue length over all lanes
     */
    public int getQueueLength() {
        return (int) nWait;
    }

    /**
     * Adds a the vehicle to the {@link LaneSegment} at initial front position with initial speed.
     */
    protected Vehicle addVehicle(LaneSegment laneSegment, TestVehicle testVehicle, double frontPosition, double speed) {
        final Vehicle vehicle = vehGenerator.createVehicle(testVehicle);
        initVehicle(laneSegment, frontPosition, speed, vehicle);
        return vehicle;
    }

    protected void initVehicle(LaneSegment laneSegment, double frontPosition, double speed, final Vehicle vehicle) {
        vehicle.setFrontPosition(frontPosition);
        vehicle.setSpeed(speed);
        if (vehicle.lane() != laneSegment.lane()) {
            vehicle.setLane(laneSegment.lane());
        }
        vehicle.setRoadSegment(roadSegment.id(), roadSegment.roadLength());
        laneSegment.addVehicle(vehicle);
        // status variables of entering vehicle for logging
        enteringVehCounter++;
        xEnterLast = frontPosition;
        vEnterLast = speed;
        laneEnterLast = laneSegment.lane();
    }
    
    /**
     * Gets the new cyclic lane for entering.
     * 
     * @param iLane
     *            the i lane
     * @return the new cyclic lane for entering
     */
    protected int getNewCyclicLaneForEntering(int iLane) {
        return (iLane == roadSegment.laneCount()) ? Lanes.MOST_INNER_LANE : iLane + Lanes.TO_RIGHT;
    }

    /**
     * Returns the measured inflow in vehicles per second, averaged over the measuring interval.
     * 
     * @return measured inflow over all lanes in vehicles per seconds
     * 
     */
    public double measuredInflow() {
        return measuredInflow;
    }

    protected void incrementInflowCount(int incr) {
        measuredInflowCount += incr;
    }

    protected void calcApproximateInflow(double dt) {
        measuredTime += dt;
        if (measuredTime > MEASURING_INTERVAL_S) {
            measuredInflow = measuredInflowCount / MEASURING_INTERVAL_S; // vehicles per second
            measuredTime = 0.0;
            measuredInflowCount = 0;
            LOG.debug(String.format(
                    "source=%d with measured inflow Q=%.1f/h over all lanes and queue length %d of waiting vehicles",
                    roadSegment.id(), measuredInflow * Units.INVS_TO_INVH, getQueueLength()));
        }
    }

}
