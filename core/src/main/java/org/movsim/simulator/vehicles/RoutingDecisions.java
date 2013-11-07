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

        if (serviceProvider.doDiverge(uncertainty, roadSegment.userId(), randomAlternative)) {
            vehicle.setExitRoadSegmentId(roadSegment.id());
            if (roadSegment.laneType(roadSegment.laneCount()) != Lanes.Type.EXIT) {
                throw new IllegalArgumentException("cannot do diverge on roadSegment " + roadSegment.userId()
                        + " without exit lane!");
            }
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

}
