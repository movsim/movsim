package org.movsim.simulator.observer;

import org.movsim.simulator.roadnetwork.routing.Route;

import com.google.common.base.Preconditions;

public class RouteAlternative {

    private final Route route;

    /** disutility corresponds to traveltime is seconds (plus additional noise errors). Is updated regularly by service provider */
    private double disutility = 0.0;

    private double probability = 0.0;

    /** for logging purposes */
    private double travelTimeError = 0.0;

    public RouteAlternative(Route route) {
        Preconditions.checkArgument(route != null);
        this.route = route;
    }

    /** copy-constructur for immutable object copy */
    public RouteAlternative(RouteAlternative routeAlternative) {
        Preconditions.checkNotNull(routeAlternative);
        this.route = routeAlternative.getRoute();
        this.disutility = routeAlternative.getDisutility();
        this.probability = routeAlternative.getProbability();
        this.travelTimeError = routeAlternative.getTravelTimeError();
    }

    public Route getRoute() {
        return route;
    }

    public double getDisutility() {
        return disutility;
    }

    public void setDisutility(double value) {
        this.disutility = value;
    }

    public double getProbability() {
        return probability;
    }

    public void setProbability(double probability) {
        this.probability = probability;
    }

    public void setTravelTimeError(double travelTimeError) {
        this.travelTimeError = travelTimeError;
    }

    public double getTravelTimeError() {
        return travelTimeError;
    }

    @Override
    public String toString() {
        return "RouteAlternative [routeName=" + route.getName() + ", value=" + disutility + ", probability="
                + probability + "]";
    }
}
