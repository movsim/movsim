package org.movsim.simulator.observer;

import java.util.HashMap;

import org.movsim.autogen.AlternativeType;
import org.movsim.autogen.DecisionPointType;

import com.google.common.base.Preconditions;

public class DecisionPoint {

    private final String roadId;

    private final HashMap<String, RouteAlternative> routeAlternatives = new HashMap<>();

    public DecisionPoint(DecisionPointType configuration) {
        Preconditions.checkNotNull(configuration);
        this.roadId = configuration.getRoadId();
        if (configuration.isSetAlternative()) {
            for (AlternativeType alternativeType : configuration.getAlternative()) {
                RouteAlternative alternative = new RouteAlternative(alternativeType.getRoute());
                routeAlternatives.put(alternative.getRoute(), alternative);
            }
        }
    }

    public String getRoadId() {
        return roadId;
    }

    public HashMap<String, RouteAlternative> getAlternatives() {
        return routeAlternatives;
    }

}
