package org.movsim.simulator.vehicles;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.movsim.simulator.roadnetwork.RoadNetwork;
import org.movsim.simulator.roadnetwork.RoadSegment;
import org.movsim.simulator.roadnetwork.routing.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DecisionModel {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(DecisionModel.class);

    public static boolean doDiverge(double uncertainty, RoadSegment roadSegment, List<Route> alternatives) {

        // TODO improve

        double sum = 0;
        double temp = 0;
        double probability = -1;
        double beta = -10;
        if (uncertainty > 0.05) {
            beta = -1 / uncertainty;
        }
        Map<String, Double> probabilities = new HashMap<>();

        for (Route route : alternatives) {
            sum += Math.exp(beta * RoadNetwork.instantaneousTravelTime(route));
        }

        if (sum != 0) {
            for (Route route : alternatives) {
                temp = Math.exp(beta * RoadNetwork.instantaneousTravelTime(route));
                probability = temp / sum;
                probabilities.put(route.getName(), (probability));
            }
        }

        // LOG.debug("inst travel alternativ1={}, alternative2={}", probability, (1-probability));

        // TODO improve
        if (Math.random() > (probability)) {
            return true;
        }

        return false;

    }

}
