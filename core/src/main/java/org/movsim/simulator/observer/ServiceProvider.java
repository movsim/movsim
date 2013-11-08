package org.movsim.simulator.observer;

import org.movsim.autogen.ServiceProviderType;
import org.movsim.simulator.SimulationTimeStep;
import org.movsim.simulator.roadnetwork.RoadNetwork;
import org.movsim.simulator.roadnetwork.routing.Routing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;

public class ServiceProvider implements SimulationTimeStep {

    private static final double MIN_UNCERTAINTY = 20; // in SI units: here seconds

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(ServiceProvider.class);

    private final String label;

    private final DecisionPoints decisionPoints;

    private final RoadNetwork roadNetwork;

    private final Routing routing;

    private final ServiceProviderLogging fileOutput;

    public ServiceProvider(ServiceProviderType configuration, Routing routing, RoadNetwork roadNetwork) {
        Preconditions.checkNotNull(configuration);
        this.routing = Preconditions.checkNotNull(routing);
        this.label = configuration.getLabel();
        this.roadNetwork = Preconditions.checkNotNull(roadNetwork);
        this.decisionPoints = new DecisionPoints(configuration.getDecisionPoints(), routing);
        this.fileOutput = configuration.isLogging() ? new ServiceProviderLogging(this) : null;
    }

    public String getLabel() {
        return label;
    }

    public DecisionPoints getDecisionPoints() {
        return decisionPoints;
    }

    @Override
    public void timeStep(double dt, double simulationTime, long iterationCount) {
        evaluateAlternatives();
        if (fileOutput != null) {
            fileOutput.timeStep(dt, simulationTime, iterationCount);
        }
    }

    public String selectRoute(double uncertainty, String roadSegmentUserId, double random) {
        DecisionPoint decisionPoint = decisionPoints.get(roadSegmentUserId);
        if (decisionPoint != null) {
            return selectAlternativeRoute(decisionPoint.getAlternatives(), uncertainty, random);
        }
        return "";
    }

    private void evaluateAlternatives() {
        double uncertainty = decisionPoints.getUncertainty();
        for (DecisionPoint decisionPoint : decisionPoints) {
            for (RouteAlternative alternative : decisionPoint) {
                double value = RoadNetwork.instantaneousTravelTime(routing.get(alternative.getRouteLabel()));
                alternative.setDisutility(value);
            }
            calcProbability(decisionPoint, uncertainty);
        }
    }

    public static void calcProbability(Iterable<RouteAlternative> alternatives, double uncertainty) {
        Preconditions.checkArgument(uncertainty >= 0, "uncertainty corresponding to standard deviation must be >=0");
        if (uncertainty < MIN_UNCERTAINTY) {
            calcProbabilityIfDeterministic(alternatives);
        }
        else {
            calcProbabilityIfStochastic(alternatives, uncertainty);
        }
    }

    private static String selectAlternativeRoute(Iterable<RouteAlternative> alternatives, double uncertainty,
            double random) {
        Preconditions.checkArgument(random >= 0 && random < 1);
        calcProbability(alternatives, uncertainty);
        double sumProb = 0;
        for (RouteAlternative alternative : alternatives) {
            sumProb += alternative.getProbability();
            if (random <= sumProb) {
                return alternative.getRouteLabel();
            }
        }
        Preconditions.checkState(false, "probabilities do not sumed correctly");
        return null;
    }

    private static void calcProbabilityIfStochastic(Iterable<RouteAlternative> alternatives, double uncertainty) {
        Preconditions.checkArgument(uncertainty >= MIN_UNCERTAINTY);
        final double beta = -1 / uncertainty;
        double denom = 0;
        double num = 0;
        double probability = 0;

        for (RouteAlternative alternative : alternatives) {
            // TODO handle diverging exponential
            denom += Math.exp(beta * alternative.getDisutility());
        }

        for (RouteAlternative alternative : alternatives) {
            num = Math.exp(beta * alternative.getDisutility());
            probability = num / denom;
            alternative.setProbability(probability);
        }
    }

    private static void calcProbabilityIfDeterministic(Iterable<RouteAlternative> alternatives) {
        RouteAlternative bestAlternative = Iterables.getLast(alternatives);
        for (RouteAlternative alternative : alternatives) {
            alternative.setProbability(0);
            if (alternative.getDisutility() < bestAlternative.getDisutility()) {
                bestAlternative = alternative;
            }
        }
        bestAlternative.setProbability(1);
    }

}
