package org.movsim.simulator.observer;

import java.util.Collection;

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
        this.decisionPoints = new DecisionPoints(configuration.getDecisionPoints());
        this.fileOutput = configuration.isLogging() ? new ServiceProviderLogging(this) : null;
    }

    public String getLabel() {
        return label;
    }

    public DecisionPoints getDecisionPoints() {
        return decisionPoints;
    }

    public void valueAlternative() {
        double uncertainty = decisionPoints.getUncertainty();
        for (DecisionPoint decisionPoint : decisionPoints.getDecisionPoints().values()) {
            for (RouteAlternative alternative : decisionPoint.getAlternatives().values()) {
                double value = RoadNetwork.instantaneousTravelTime(routing.get(alternative.getRoute()));
                alternative.setValue(value);
            }
            calcProbability(decisionPoint.getAlternatives().values(), uncertainty);
        }
    }

    public static void calcProbability(Collection<RouteAlternative> alternatives, double uncertainty) {
        double sum = 0;
        double num = 0;
        double probability = 0;
        double beta = -10;
        if (uncertainty > 0.05) {
            beta = -1 / uncertainty;
        }

        for (RouteAlternative alternative : alternatives) {
            sum += Math.exp(beta * alternative.getValue());
        }

        if (sum != 0) {
            for (RouteAlternative alternative : alternatives) {
                num = Math.exp(beta * alternative.getValue());
                probability = num / sum;
                alternative.setProbability(probability);
            }
        }
    }

    @Override
    public void timeStep(double dt, double simulationTime, long iterationCount) {
        valueAlternative();
        if (fileOutput != null) {
            fileOutput.timeStep(dt, simulationTime, iterationCount);
        }
    }
}
