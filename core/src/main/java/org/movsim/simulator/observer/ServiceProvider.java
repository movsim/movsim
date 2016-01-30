package org.movsim.simulator.observer;

import java.util.List;

import javax.annotation.CheckForNull;

import org.movsim.autogen.ServiceProviderType;
import org.movsim.simulator.SimulationTimeStep;
import org.movsim.simulator.roadnetwork.RoadNetwork;
import org.movsim.simulator.roadnetwork.RoadNetworkUtils;
import org.movsim.simulator.roadnetwork.RoadSegment;
import org.movsim.simulator.roadnetwork.routing.Routing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

public class ServiceProvider implements SimulationTimeStep {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceProvider.class);

    private static final double GRID_LENGTH_TRAVELTIME_ESTIMATION = 100;

    private final String label;

    private final double serverUpdateInterval;

    private boolean serverUpdate = true;

    private final double vehicleUpdateInterval;

    private final DecisionPoints decisionPoints;

    private final Noise noise;

    private final ServiceProviderLogging fileOutput;

    public ServiceProvider(ServiceProviderType configuration, Routing routing, RoadNetwork roadNetwork) {
        Preconditions.checkNotNull(configuration);
        this.label = configuration.getLabel();
        this.serverUpdateInterval = configuration.getServerUpdateInterval();
        this.vehicleUpdateInterval = configuration.getVehicleUpdateInterval();
        this.decisionPoints = new DecisionPoints(configuration.getDecisionPoints(), routing);
        this.noise = new Noise(configuration.getTau(), configuration.getFluctStrength());
        this.fileOutput = configuration.isLogging() ? new ServiceProviderLogging(this) : null;
    }

    public String getLabel() {
        return label;
    }

    public DecisionPoints getDecisionPoints() {
        return decisionPoints;
    }

    public double getVehicleUpdateInterval() {
        return vehicleUpdateInterval;
    }

    @Override
    public void timeStep(double dt, double simulationTime, long iterationCount) {
        if (serverUpdateInterval != 0) {
            serverUpdate = (iterationCount % (serverUpdateInterval / dt) == 0) ? true : false;
        }
        evaluateDecisionPoints(dt);
        if (fileOutput != null) {
            fileOutput.timeStep(dt, simulationTime, iterationCount);
        }
    }

    /** calculate individual probabilities */
    public static void updateRouteAlternatives(Iterable<RouteAlternative> alternatives, double uncertainty) {
        LogitRouteDecisionMaking.calcProbabilities(alternatives, uncertainty);
    }

    public static RouteAlternative selectMostProbableAlternative(Iterable<RouteAlternative> alternatives,
            double random) {
        return LogitRouteDecisionMaking.selectMostProbableAlternative(alternatives, random);
    }

    @CheckForNull
    public List<RouteAlternative> getAlternativesForDecisionPoint(RoadSegment roadSegment) {
        DecisionPoint decisionPoint = getDecisionPoint(roadSegment.userId());
        if (decisionPoint == null) {
            return null;
        }
        return decisionPoint.createRouteAlternatives();
    }

    @CheckForNull
    private DecisionPoint getDecisionPoint(String roadSegmentUserId) {
        return decisionPoints.get(roadSegmentUserId);
    }

    // public RouteAlternative selectRouteAlternative(Iterable<RouteAlternative> alternatives, double uncertainty,
    // double random) {
    // RouteAlternative routeAlternative = LogitRouteDecisionMaking.selectAlternativeRoute(alternatives, uncertainty,
    // random);
    // return new RouteAlternative(routeAlternative);
    // }

    private void evaluateDecisionPoints(double dt) {
        double uncertainty = decisionPoints.getUncertainty();
        // uncertainty as standard deviation must be >=0, already required by xsd
        for (DecisionPoint decisionPoint : decisionPoints) {
            evaluateDecisionPoint(dt, uncertainty, decisionPoint);
        }
    }

    private void evaluateDecisionPoint(double dt, double uncertainty, DecisionPoint decisionPoint) {
        for (RouteAlternative alternative : decisionPoint) {
            double traveltimeError = 0;
            if (noise != null) {
                noise.update(dt, alternative.getTravelTimeError());
                traveltimeError = noise.getTimeError();
            }
            // traveltime is the metric for disutility
            double traveltime = traveltimeError + RoadNetworkUtils.instantaneousTravelTimeOnGrid(alternative.getRoute(),
                    GRID_LENGTH_TRAVELTIME_ESTIMATION);
            alternative.setTravelTimeError(traveltimeError);
            if (serverUpdate) {
                alternative.setDisutility(traveltime);
            }
        }
        LogitRouteDecisionMaking.calcProbabilities(decisionPoint, uncertainty);
    }

}
