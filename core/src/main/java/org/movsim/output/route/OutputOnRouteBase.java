package org.movsim.output.route;

import com.google.common.base.Preconditions;
import org.movsim.simulator.SimulationTimeStep;
import org.movsim.simulator.roadnetwork.RoadNetwork;
import org.movsim.simulator.roadnetwork.routing.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class OutputOnRouteBase implements SimulationTimeStep {

    // logger for all sub-classes
    public final Logger LOG = LoggerFactory.getLogger(this.getClass());

    protected final Route route;

    protected final RoadNetwork roadNetwork;

    OutputOnRouteBase(RoadNetwork roadNetwork, Route route) {
        this.roadNetwork = Preconditions.checkNotNull(roadNetwork);
        this.route = Preconditions.checkNotNull(route);
    }

}
