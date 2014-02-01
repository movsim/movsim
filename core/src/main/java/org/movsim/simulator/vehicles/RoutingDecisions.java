package org.movsim.simulator.vehicles;

import org.movsim.simulator.observer.ServiceProvider;
import org.movsim.simulator.roadnetwork.Lanes;
import org.movsim.simulator.roadnetwork.RoadSegment;
import org.movsim.utilities.MyRandom;

import com.google.common.base.Preconditions;

public class RoutingDecisions {

    private ServiceProvider serviceProvider;
    private double uncertainty;
    private final double randomAlternative = MyRandom.nextDouble();

    private final Vehicle vehicle;

    public RoutingDecisions(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public void considerRouteAlternatives(RoadSegment roadSegment) {
        if (serviceProvider == null) {
            return;
        }

        String routeLabel = serviceProvider.selectRoute(uncertainty, roadSegment.userId(), randomAlternative);
        if (routeLabel == null || routeLabel.isEmpty()) {
            return;
        }

        // FIXME fully-fleshed routing decision making, here quick hack
        if (!routeLabel.equals("A1") && !routeLabel.equals("A2")) {
            throw new IllegalArgumentException("cannot handle other alternatives="+routeLabel+"  then A1 and A2 yet!!!");
        }

        if ("A2".equals(routeLabel)) {
            // activate decision to diverge at exit
            vehicle.setExitRoadSegmentId(roadSegment.id());
            if (roadSegment.laneType(roadSegment.laneCount()) != Lanes.Type.EXIT) {
                throw new IllegalArgumentException("cannot do diverge on roadSegment " + roadSegment.userId()
                        + " without exit lane!");
            }
        } else if ("A1".equals(routeLabel)) {
            // reset if A2 has been chosen in previous update
            vehicle.setExitRoadSegmentId(Vehicle.ROAD_SEGMENT_ID_NOT_SET);
        }
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
