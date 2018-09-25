package org.movsim.simulator.vehicles;

import com.google.common.base.Preconditions;
import org.movsim.simulator.observer.RouteAlternative;
import org.movsim.simulator.observer.ServiceProvider;
import org.movsim.simulator.roadnetwork.Lanes;
import org.movsim.simulator.roadnetwork.RoadSegment;
import org.movsim.simulator.roadnetwork.routing.Route;
import org.movsim.utilities.MyRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class RoutingDecisions {

    private static final Logger LOG = LoggerFactory.getLogger(RoutingDecisions.class);

    private static final double NOT_INIT = -1.0;

    private static int countReroutings = 0;

    private ServiceProvider serviceProvider;
    private double uncertainty;
    private double reroutingThreshold;
    private final double randomAlternative = MyRandom.nextDouble();

    private final Vehicle vehicle;

    private double lastUpdateTime = NOT_INIT;

    private RouteAlternative routeAlternative;

    public RoutingDecisions(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public void considerRouteAlternatives(double simulationTime, RoadSegment roadSegment) {
        if (serviceProvider == null) {
            return;
        }

        if (lastUpdateTime == NOT_INIT) {
            // initialize update time with random (negative) offset to avoid synchronization at the inflow boundary
            lastUpdateTime = simulationTime - MyRandom.nextDouble() * serviceProvider.getVehicleUpdateInterval();
        }

        if (readyForNextUpdate(serviceProvider.getVehicleUpdateInterval(), simulationTime)) {

            LOG.debug("vehicle gets update at time={}, last update was at time={}", (int) simulationTime,
                    (int) lastUpdateTime);
            lastUpdateTime = simulationTime;

            // quick hack for finite vehicle update interval: look-ahead one road segment to assign routing decision in advance
            RoadSegment decisionPointSegment = roadSegment;
            if (roadSegment.userId().equals("1")) {
                decisionPointSegment = roadSegment.sinkRoadSegment(Lanes.MOST_INNER_LANE);
            }

            List<RouteAlternative> alternatives = serviceProvider.getAlternativesForDecisionPoint(decisionPointSegment);
            if (alternatives != null) {
                ServiceProvider.updateRouteAlternatives(alternatives, uncertainty);
                RouteAlternative newRouteAlternative = ServiceProvider
                        .selectMostProbableAlternative(alternatives, randomAlternative);

                // quick-hack: assign exit lane to vehicle since routing capabilities not yet available in movsim
                boolean doRerouting = (uncertainty > 0) || checkForRerouting(newRouteAlternative, alternatives);
                if (doRerouting) {
                    routeAlternative = newRouteAlternative;
                    assignRoute(decisionPointSegment, routeAlternative.getRoute());
                }
            }
        }
    }

    private boolean checkForRerouting(RouteAlternative newRouteAlternative, List<RouteAlternative> alternatives) {
        if (routeAlternative == null) {
            return true;
        }

        if (newRouteAlternative.getRoute().equals(routeAlternative.getRoute())) {
            return false; // no new route
        }

        RouteAlternative alternativeFromLastRouting = getAlternativeFromLastUpdate(alternatives);

        boolean doRerouting =
                newRouteAlternative.getDisutility() + reroutingThreshold < alternativeFromLastRouting.getDisutility();
        if (doRerouting) {
            ++countReroutings;
            LOG.info("vehicle is re-routed: diff disutility={}, counterReroutings={}",
                    newRouteAlternative.getDisutility() - alternativeFromLastRouting.getDisutility(), countReroutings);
        }
        return doRerouting;
    }

    private RouteAlternative getAlternativeFromLastUpdate(List<RouteAlternative> alternatives) {
        for (RouteAlternative alternative : alternatives) {
            if (alternative.getRoute().equals(routeAlternative.getRoute())) {
                return alternative;
            }
        }
        throw new IllegalStateException("shouldn't come here - alternative not found");
    }

    private void assignRoute(RoadSegment roadSegment, Route route) {
        if (!route.getName().equals("A1") && !route.getName().equals("A2")) {
            throw new IllegalArgumentException("cannot handle other alternatives=" + route + "  then A1 and A2 yet!!!");
        }

        if ("A2".equals(route.getName())) {
            // activate decision to diverge at exit
            vehicle.setExitRoadSegmentId(roadSegment.id());
            if (roadSegment.laneType(roadSegment.laneCount()) != Lanes.Type.EXIT) {
                throw new IllegalArgumentException(
                        "cannot do diverge on roadSegment " + roadSegment.userId() + " without exit lane!");
            }
        } else if ("A1".equals(route.getName())) {
            // reset if A2 has been chosen in previous update
            vehicle.setExitRoadSegmentId(Vehicle.ROAD_SEGMENT_ID_NOT_SET);
        }
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

    public double getReroutingThreshold() {
        return reroutingThreshold;
    }

    public void setReroutingThreshold(double reroutingThreshold) {
        this.reroutingThreshold = reroutingThreshold;
    }

}
