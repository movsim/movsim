package org.movsim.simulator.roadnetwork;

import org.movsim.simulator.vehicles.VehicleGenerator;


public class AbstractTrafficSource {

    public interface RecordDataCallback {
        /**
         * Callback to allow the application to process or record the traffic source data.
         * 
         */
        public void recordData(double simulationTime, int laneEnter, double xEnter, double vEnter, double totalInflow,
                int enteringVehCounter, double nWait);
    }

    RecordDataCallback recordDataCallback;

    int enteringVehCounter;

    /** The x enter last. status of last merging vehicle for logging to file */
    double xEnterLast;

    double vEnterLast;

    int laneEnterLast;

    /** number of vehicles in the queue as result from integration over demand minus inserted vehicles. */
    double nWait;

    final VehicleGenerator vehGenerator;

    final RoadSegment roadSegment;

    final InflowTimeSeries inflowTimeSeries;

    public AbstractTrafficSource(VehicleGenerator vehGenerator, RoadSegment roadSegment,
            InflowTimeSeries inflowTimeSeries) {
        this.vehGenerator = vehGenerator;
        this.roadSegment = roadSegment;
        this.inflowTimeSeries = inflowTimeSeries;
        nWait = 0;
    }

    /**
     * Sets the traffic source recorder.
     * 
     * @param recordDataCallback
     */
    public void setRecorder(RecordDataCallback recordDataCallback) {
        enteringVehCounter = 1;
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
     * Gets the total inflow over all lanes
     * 
     * @param time
     *            the time
     * @return the total inflow over all lanes
     */
    public double getTotalInflow(double time) {
        return inflowTimeSeries.getFlowPerLane(time) * roadSegment.laneCount();
    }

    /**
     * Returns the number of vehicles in the queue.
     * 
     * @return integer queue length over all lanes
     */
    public int getQueueLength() {
        return (int) nWait;
    }

    public double getFlowPerLane(double time) {
        return inflowTimeSeries.getFlowPerLane(time);
    }

}
