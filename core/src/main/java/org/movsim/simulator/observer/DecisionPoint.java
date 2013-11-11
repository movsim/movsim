package org.movsim.simulator.observer;

import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

import org.movsim.autogen.DecisionPointType;
import org.movsim.autogen.RouteAlternativeType;

import com.google.common.base.Preconditions;

public class DecisionPoint implements Iterable<RouteAlternative> {

    private final String roadId;

    /** sorted according to routeLabel for assuring a consistent */
    private final SortedMap<String, RouteAlternative> routeAlternatives = new TreeMap<>();

    public DecisionPoint(DecisionPointType configuration) {
        Preconditions.checkNotNull(configuration);
        if (!configuration.isSetRouteAlternative() || configuration.getRouteAlternative().isEmpty()) {
            throw new IllegalArgumentException("at least one alternative must be defined.");
        }

        this.roadId = configuration.getRoadId();
        for (RouteAlternativeType routeAlternative : configuration.getRouteAlternative()) {
            String routeLabel = routeAlternative.getRoute();
            RouteAlternative alternative = new RouteAlternative(routeLabel);
            routeAlternatives.put(alternative.getRouteLabel(), alternative);
        }
    }

    public String getRoadId() {
        return roadId;
    }

    @Override
    public Iterator<RouteAlternative> iterator() {
        return routeAlternatives.values().iterator();
    }

    public Iterable<RouteAlternative> getAlternatives() {
        return routeAlternatives.values();
    }

}
