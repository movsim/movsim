package org.movsim.simulator.vehicles;

import org.movsim.simulator.observer.ServiceProvider;
import org.movsim.simulator.roadnetwork.Lanes;
import org.movsim.simulator.roadnetwork.RoadSegment;
import org.movsim.simulator.roadnetwork.routing.Route;
import org.movsim.utilities.MyRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

public class RoutingDecisions {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(RoutingDecisions.class);

    private static final double NOT_INIT = -1d;
    private ServiceProvider serviceProvider;
    private double uncertainty;
    private final double randomAlternative = MyRandom.nextDouble();

    private final Vehicle vehicle;

    private Route route;
    private double lastUpdateTime = NOT_INIT;

    public RoutingDecisions(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public void considerRouteAlternatives(double simulationTime, RoadSegment roadSegment) {
        if (serviceProvider == null) {
            return;
        }

        if (lastUpdateTime == NOT_INIT) {
            // initialize update time with random (negative) offset to avoid synchronization at the inflow boundary
            lastUpdateTime = -MyRandom.nextDouble() * serviceProvider.getVehicleUpdateInterval();
        }

        // discrete update works only for one decision point
        if (readyForNextUpdate(serviceProvider.getVehicleUpdateInterval(), simulationTime)) {
            LOG.debug("vehicle gets update at time={}, last update was at time={}", simulationTime, lastUpdateTime);
            lastUpdateTime = simulationTime;

            // FIXME avoid hack of getting relevant decision point
            // String roadSegmentWithDecisionPoint = roadSegment.userId().equals("1") || roadSegment.userId().equals("2") ? "2"
            // : roadSegment.userId();
            route = serviceProvider.selectRoute(uncertainty, roadSegment.userId(), randomAlternative);
            LOG.debug("selected route is={}", route != null ? route.getName() : "");
        }

        if (route == null) {
            return;
        }

        // FIXME fully-fleshed routing decision making, here quick hack
        if (!route.getName().equals("A1") && !route.getName().equals("A2")) {
            throw new IllegalArgumentException("cannot handle other alternatives="+route+"  then A1 and A2 yet!!!");
        }


        if ("A2".equals(route.getName())) {
            // activate decision to diverge at exit
            vehicle.setExitRoadSegmentId(roadSegment.id());
            if (roadSegment.laneType(roadSegment.laneCount()) != Lanes.Type.EXIT) {
                throw new IllegalArgumentException("cannot do diverge on roadSegment " + roadSegment.userId()
                        + " without exit lane!");
            }
        } else if ("A1".equals(route.getName())) {
            // reset if A2 has been chosen in previous update
            vehicle.setExitRoadSegmentId(Vehicle.ROAD_SEGMENT_ID_NOT_SET);
        }

        route = null;
    }

    private boolean readyForNextUpdate(double vehicleUpdateInterval, double simulationTime) {
        return simulationTime - lastUpdateTime >= vehicleUpdateInterval;
    }

    public ServiceProvider getServiceProvider() {
        return serviceProvider;
    }

    public void setServiceProvider(ServiceProvider serviceProvider) {
        this.serviceProvider = Preconditions.checkNotNull(serviceProvider);
    }

    public double getUncertainty() {
        return uncertainty;
    }

    public void setUncertainty(double uncertainty) {
        this.uncertainty = uncertainty;
    }

    public boolean hasServiceProvider() {
        return serviceProvider != null;
    }

}
