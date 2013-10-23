package org.movsim.simulator.observer;

import org.movsim.autogen.ServiceProviderType;
import org.movsim.simulator.SimulationTimeStep;
import org.movsim.simulator.roadnetwork.RoadNetwork;
import org.movsim.simulator.roadnetwork.routing.Routing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

public class ServiceProvider implements SimulationTimeStep {

    /** The Constant LOG. */
    final static Logger logger = LoggerFactory.getLogger(ServiceProvider.class);

    private final String label;

    private final RoadNetwork roadNetwork;

    private final Routing routing;

    private final ServiceProviderLogging fileOutput;

    public ServiceProvider(ServiceProviderType configuration, Routing routing, RoadNetwork roadNetwork) {
	Preconditions.checkNotNull(configuration);
	this.routing = Preconditions.checkNotNull(routing);
	this.label = configuration.getLabel();
	this.roadNetwork = Preconditions.checkNotNull(roadNetwork);
	this.fileOutput = configuration.isLogging() ? new ServiceProviderLogging(this) : null;
	// configuration ...
    }

    public String getLabel() {
	return label;
    }

    @Override
    public void timeStep(double dt, double simulationTime, long iterationCount) {
	if (fileOutput != null) {
	    fileOutput.timeStep(dt, simulationTime, iterationCount);
	}

    }
}
