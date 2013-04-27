package org.movsim.output.route;

import org.movsim.simulator.SimulationTimeStep;
import org.movsim.simulator.roadnetwork.RoadNetwork;
import org.movsim.simulator.roadnetwork.routing.Route;

import com.google.common.base.Preconditions;

abstract class OutputOnRouteBase implements SimulationTimeStep {

    protected final Route route;

    protected final RoadNetwork roadNetwork;

    OutputOnRouteBase(RoadNetwork roadNetwork, Route route) {
        this.roadNetwork = Preconditions.checkNotNull(roadNetwork);
        this.route = Preconditions.checkNotNull(route);
    }
    
}
