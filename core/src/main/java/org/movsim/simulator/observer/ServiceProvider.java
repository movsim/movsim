package org.movsim.simulator.observer;

import org.movsim.autogen.ServiceProviderType;
import org.movsim.simulator.SimulationTimeStep;
import org.movsim.simulator.roadnetwork.RoadNetwork;
import org.movsim.simulator.roadnetwork.RoadNetworkUtils;
import org.movsim.simulator.roadnetwork.routing.Routing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;

public class ServiceProvider implements SimulationTimeStep {

    private static final int TOO_LARGE_EXPONENT = 100;

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(ServiceProvider.class);

    private final String label;

    private final double updateTime;
    boolean update = true; 

    private final DecisionPoints decisionPoints;

    private final Noise noise;

    private final RoadNetwork roadNetwork;

    private final Routing routing;

    private final ServiceProviderLogging fileOutput;

    public ServiceProvider(ServiceProviderType configuration, Routing routing, RoadNetwork roadNetwork) {
        Preconditions.checkNotNull(configuration);
        this.routing = Preconditions.checkNotNull(routing);
        this.label = configuration.getLabel();
        this.updateTime = configuration.getUpdateTime();
        this.roadNetwork = Preconditions.checkNotNull(roadNetwork);
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

    @Override
    public void timeStep(double dt, double simulationTime, long iterationCount) {      
        if(updateTime!=0){
            update = (iterationCount%(updateTime / dt)==0) ? true: false;
        }        
        evaluateDecisionPoints(dt);
        if (fileOutput != null) {
            fileOutput.timeStep(dt, simulationTime, iterationCount);
        }
    }

    /**
     * @param uncertainty
     * @param roadSegmentUserId
     * @param random
     * @return
     */
    public String selectRoute(double uncertainty, String roadSegmentUserId, double random) {
        DecisionPoint decisionPoint = decisionPoints.get(roadSegmentUserId);
        if (decisionPoint != null) {
            return selectAlternativeRoute(decisionPoint.getAlternatives(), uncertainty, random);
        }
        return ""; // TODO
    }

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
            // usage of metric for disutility
            double traveltime = traveltimeError
                    + RoadNetworkUtils.instantaneousTravelTime(routing.get(alternative.getRouteLabel()));
            alternative.setTravelTimeError(traveltimeError);
            if (update) {
                alternative.setDisutility(traveltime);
            }
        }
        calcProbabilities(decisionPoint, uncertainty);
    }

    private static void calcProbabilities(Iterable<RouteAlternative> alternatives, double uncertainty) {
        if (uncertainty > 0) {
            calcProbabilityIfStochastic(alternatives, uncertainty);
        } else {
            calcProbabilityForDeterministic(alternatives);
        }
    }

    private static void calcProbabilityIfStochastic(Iterable<RouteAlternative> alternatives, double uncertainty) {
        final double beta = -1 / uncertainty;
        for (RouteAlternative alternative : alternatives) {
            // check first for large exponential
            if (hasTooLargeExponent(beta, alternative, alternatives)) {
                // probability of 0 as trivial result
                alternative.setProbability(0);

            } else {
                double probAlternative = calcProbability(beta, alternative, alternatives);
                alternative.setProbability(probAlternative);
            }
            LOG.debug("calculated prob for stochastic case: {}", alternative);
        }
    }

    private static double calcProbability(double beta, RouteAlternative alternative,
            Iterable<RouteAlternative> alternatives) {
        double denom = 0;
        for (RouteAlternative otherAlternative : alternatives) {
            denom += Math.exp(beta * (otherAlternative.getDisutility() - alternative.getDisutility()));
        }
        return 1. / denom;
    }

    private static void calcProbabilityForDeterministic(Iterable<RouteAlternative> alternatives) {
        RouteAlternative bestAlternative = Iterables.getLast(alternatives);
        for (RouteAlternative alternative : alternatives) {
            alternative.setProbability(0);
            if (alternative.getDisutility() < bestAlternative.getDisutility()) {
                bestAlternative = alternative;
            }
        }
        bestAlternative.setProbability(1);
    }

    private static String selectAlternativeRoute(Iterable<RouteAlternative> alternatives, double uncertainty,
            double random) {
        Preconditions.checkArgument(random >= 0 && random < 1);
        calcProbabilities(alternatives, uncertainty);
        double sumProb = 0;
        for (RouteAlternative alternative : alternatives) {
            sumProb += alternative.getProbability();
            LOG.debug("alternative={}, sumProb={}", alternative.toString(), sumProb);
            if (random <= sumProb) {
                return alternative.getRouteLabel();
            }
        }
        Preconditions.checkState(false, "probabilities not sumed correctly: random=" + random + ", sumProb=" + sumProb);
        return null;
    }

    private static boolean hasTooLargeExponent(double beta, RouteAlternative alternative,
            Iterable<RouteAlternative> alternatives) {
        for (RouteAlternative otherAlternative : alternatives) {
            double delta = Math.abs(alternative.getDisutility() - otherAlternative.getDisutility());
            if (beta * delta > TOO_LARGE_EXPONENT) {
                return true;
            }
        }
        return false;
    }

}
