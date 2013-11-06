package org.movsim.simulator.observer;

import org.movsim.autogen.ServiceProviderType;
import org.movsim.simulator.SimulationTimeStep;
import org.movsim.simulator.roadnetwork.RoadNetwork;
import org.movsim.simulator.roadnetwork.routing.Routing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

public class ServiceProvider implements SimulationTimeStep {

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

    private void valueAlternative() {
        double uncertainty = decisionPoints.getUncertainty();
        for (DecisionPoint decisionPoint : decisionPoints) {
            for (RouteAlternative alternative : decisionPoint) {
                double value = RoadNetwork.instantaneousTravelTime(routing.get(alternative.getRouteLabel()));
                alternative.setValue(value);
            }
            calcProbability(decisionPoint, uncertainty);
        }
    }

    public static void calcProbability(Iterable<RouteAlternative> alternatives, double uncertainty) {
        double sum = 0;
        double num = 0;
        double probability = 0;
        double beta = -100;
        if (Math.abs(uncertainty) > 0.01) {
            beta = -1 / uncertainty;
        }

        for (RouteAlternative alternative : alternatives) {
            sum += Math.exp(beta * alternative.getValue());
        }

        if (sum != 0) {
            for (RouteAlternative alternative : alternatives) {
                num = Math.exp(beta * alternative.getValue());
                probability = num / sum;
                if (uncertainty == 0) {
                    probability = Math.round(probability);
                }
                alternative.setProbability(probability);
            }
        }
    }

    // TODO use cached values from RouteAlternative, refactor methods
    private boolean alternativeAvailableAndMoreAttractive(double uncertainty, DecisionPoint decisionPoint, double random) {
        double sum = 0;
        double temp = 0;
        double probability = -1;
        double beta = -100;
        if (uncertainty > 0.01) {
            beta = -1 / uncertainty;
        }

        for (RouteAlternative route : decisionPoint) {
            sum += Math.exp(beta * RoadNetwork.instantaneousTravelTime(routing.get(route.getRouteLabel())));
        }

        // specific solution TODO
        if (sum != 0) {
            for (RouteAlternative route : decisionPoint) {
                if (route.getRouteLabel().equals("A1")) {
                    temp = Math.exp(beta * RoadNetwork.instantaneousTravelTime(routing.get(route.getRouteLabel())));
                    probability = temp / sum;
                    if (uncertainty == 0) {
                        probability = Math.round(probability);
                    }
                }
            }
        }

        // LOG.debug("inst travel alternativ1={}, alternative2={}", probability, (1-probability));

        if (random > probability) {
            return true;
        }
        return false;
    }

    @Override
    public void timeStep(double dt, double simulationTime, long iterationCount) {
        valueAlternative();
        if (fileOutput != null) {
            fileOutput.timeStep(dt, simulationTime, iterationCount);
        }
    }

    public boolean doDiverge(double uncertainty, String roadSegmentUserId, double random) {
        DecisionPoint decisionPoint = decisionPoints.get(roadSegmentUserId);
        if (decisionPoint != null) {
            return alternativeAvailableAndMoreAttractive(uncertainty, decisionPoint, random);
        }
        return false;
    }
}
