package org.movsim.simulator.observer;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.movsim.autogen.DecisionPointType;
import org.movsim.autogen.RouteAlternativeType;

import com.google.common.base.Preconditions;

public class DecisionPoint implements Iterable<RouteAlternative> {

    private final String roadId;

    private final Set<RouteAlternative> routeAlternatives = new HashSet<>();

    public DecisionPoint(DecisionPointType configuration) {
        Preconditions.checkNotNull(configuration);
        if (!configuration.isSetRouteAlternative() || configuration.getRouteAlternative().isEmpty()) {
            throw new IllegalArgumentException("at least one alternative must be defined.");
        }

        this.roadId = configuration.getRoadId();
        for (RouteAlternativeType routeAlternative : configuration.getRouteAlternative()) {
            String routeLabel = routeAlternative.getRoute();
            RouteAlternative alternative = new RouteAlternative(routeLabel);
            routeAlternatives.add(alternative);
        }
    }

    public String getRoadId() {
        return roadId;
    }

    @Override
    public Iterator<RouteAlternative> iterator() {
        return routeAlternatives.iterator();
    }

    public Iterable<RouteAlternative> getAlternatives() {
        return routeAlternatives;
    }

}
