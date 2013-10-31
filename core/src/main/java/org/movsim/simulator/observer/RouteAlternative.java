package org.movsim.simulator.observer;

import com.google.common.base.Preconditions;

class RouteAlternative {

    private final String route;

    private double value = 0.0;

    private double probability = 0.0;

    public RouteAlternative(String route) {
        Preconditions.checkArgument(route != null && !route.isEmpty());
        this.route = route;
    }

    public String getRouteLabel() {
        return route;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public double getProbability() {
        return probability;
    }

    public void setProbability(double probability) {
        this.probability = probability;
    }

}
