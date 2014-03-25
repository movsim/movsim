package org.movsim.simulator.observer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.movsim.autogen.DecisionPointType;
import org.movsim.autogen.RouteAlternativeType;
import org.movsim.simulator.roadnetwork.routing.Route;
import org.movsim.simulator.roadnetwork.routing.Routing;

import com.google.common.base.Preconditions;

public class DecisionPoint implements Iterable<RouteAlternative> {

    private final String roadId;

    /** sorted according to routeLabel for assuring a consistent */
    private final SortedMap<String, RouteAlternative> routeAlternatives = new TreeMap<>();

    public DecisionPoint(DecisionPointType configuration, Routing routing) {
        Preconditions.checkNotNull(configuration);
        if (!configuration.isSetRouteAlternative() || configuration.getRouteAlternative().isEmpty()) {
            throw new IllegalArgumentException("at least one alternative must be defined.");
        }

        this.roadId = configuration.getRoadId();
        for (RouteAlternativeType routeAlternative : configuration.getRouteAlternative()) {
            String routeLabel = routeAlternative.getRoute();
            Route route = Preconditions.checkNotNull(routing.get(routeLabel), "route with label=" + routeLabel
                    + " not configured in <Routes> input!");
            RouteAlternative alternative = new RouteAlternative(route);
            routeAlternatives.put(alternative.getRoute().getName(), alternative);
        }
    }

    public String getRoadId() {
        return roadId;
    }

    @Override
    public Iterator<RouteAlternative> iterator() {
        return routeAlternatives.values().iterator();
    }

    public List<RouteAlternative> createRouteAlternatives() {
        List<RouteAlternative> alternatives = new ArrayList<>(routeAlternatives.size());
        for(RouteAlternative routeAlternative : routeAlternatives.values()){
            alternatives.add(new RouteAlternative(routeAlternative));
        }
        return alternatives;
    }

}
