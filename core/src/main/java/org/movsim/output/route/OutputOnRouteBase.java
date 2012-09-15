package org.movsim.output.route;

import org.movsim.simulator.SimulationTimeStep;
import org.movsim.simulator.roadnetwork.RoadNetwork;
import org.movsim.simulator.roadnetwork.Route;

public abstract class OutputOnRouteBase implements SimulationTimeStep {

    protected final Route route;

    protected final RoadNetwork roadNetwork;

    public OutputOnRouteBase(RoadNetwork roadNetwork, Route route) {
        this.roadNetwork = roadNetwork;
        this.route = route;
    }
    
}
