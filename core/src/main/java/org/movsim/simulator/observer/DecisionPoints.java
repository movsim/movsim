package org.movsim.simulator.observer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.movsim.autogen.DecisionPointType;
import org.movsim.autogen.DecisionPointsType;

import com.google.common.base.Preconditions;

public class DecisionPoints implements Iterable<DecisionPoint> {

    private final Map<String, DecisionPoint> decisionPoints = new HashMap<>();

    private final double uncertainty;

    public DecisionPoints(DecisionPointsType configuration) {
        Preconditions.checkNotNull(configuration);
        this.uncertainty = configuration.getUncertainty();
        if (configuration.isSetDecisionPoint()) {
            for (DecisionPointType decisionPointType : configuration.getDecisionPoint()) {
                DecisionPoint decisionPoint = new DecisionPoint(decisionPointType);
                String key = decisionPoint.getRoadId();
                if (decisionPoints.containsKey(key)) {
                    throw new IllegalArgumentException("decision point with roadId=" + key + " already exists.");
                }
                decisionPoints.put(key, decisionPoint);
            }
        }
    }

    public double getUncertainty() {
        return uncertainty;
    }

    public Map<String, DecisionPoint> getDecisionPoints() {
        return decisionPoints;
    }

    @Override
    public Iterator<DecisionPoint> iterator() {
        return decisionPoints.values().iterator();
    }

}
