package org.movsim.simulator.observer;

import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.annotation.CheckForNull;

import org.movsim.autogen.DecisionPointType;
import org.movsim.autogen.DecisionPointsType;
import org.movsim.simulator.roadnetwork.routing.Routing;

import com.google.common.base.Preconditions;

public class DecisionPoints implements Iterable<DecisionPoint> {

    /** Sorting assures consistent ordering of decision points in output */
    private final SortedMap<String, DecisionPoint> decisionPoints = new TreeMap<>();

    private final double uncertainty;

    public DecisionPoints(DecisionPointsType configuration, Routing routing) {
        Preconditions.checkNotNull(configuration);
        this.uncertainty = configuration.getUncertainty();
        if (configuration.isSetDecisionPoint()) {
            for (DecisionPointType decisionPointType : configuration.getDecisionPoint()) {
                DecisionPoint decisionPoint = new DecisionPoint(decisionPointType);
                String roadId = decisionPoint.getRoadId();
                if (decisionPoints.containsKey(roadId)) {
                    throw new IllegalArgumentException("decision point with roadId=" + roadId + " already exists.");
                }
                decisionPoints.put(roadId, decisionPoint);
            }
        }
    }

    public double getUncertainty() {
        return uncertainty;
    }

    @CheckForNull
    public DecisionPoint get(String roadSegmentId) {
        return decisionPoints.get(roadSegmentId);
    }

    @Override
    public Iterator<DecisionPoint> iterator() {
        return decisionPoints.values().iterator();
    }

}
