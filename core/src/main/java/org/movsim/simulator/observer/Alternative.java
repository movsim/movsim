package org.movsim.simulator.observer;

public class Alternative {

    private final String route;
    
    private double value=0.0;
    
    private double probability=0.0;
    
    public Alternative(String route){
        this.route=route;
    }

    public String getRoute() {
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
