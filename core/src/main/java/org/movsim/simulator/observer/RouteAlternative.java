package org.movsim.simulator.observer;

import com.google.common.base.Preconditions;

class RouteAlternative {

    private final String routeLabel;

    private double value = 0.0;

    private double probability = 0.0;

    private double travelTimeError = 0.0;

    public RouteAlternative(String route) {
        Preconditions.checkArgument(route != null && !route.isEmpty());
        this.routeLabel = route;
    }

    public String getRouteLabel() {
        return routeLabel;
    }

    public double getDisutility() {
        return value;
    }

    /** disutility corresponds to traveltime is seconds. */
    public void setDisutility(double value) {
        this.value = value;
    }

    public double getProbability() {
        return probability;
    }

    public void setProbability(double probability) {
        this.probability = probability;
    }

    @Override
    public String toString() {
        return "RouteAlternative [route=" + routeLabel + ", value=" + value + ", probability=" + probability + "]";
    }

    public void setTravelTimeError(double travelTimeError) {
        this.travelTimeError = travelTimeError;
    }

    public double getTravelTimeError() {
        return travelTimeError;
    }

}
