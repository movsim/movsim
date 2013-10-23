package org.movsim.simulator.observer;

import org.movsim.input.ProjectMetaData;
import org.movsim.output.fileoutput.FileOutputBase;
import org.movsim.simulator.SimulationTimeStep;

class ServiceProviderLogging extends FileOutputBase implements SimulationTimeStep {

    public ServiceProviderLogging(ServiceProvider serviceProvider) {
	super(ProjectMetaData.getInstance().getOutputPath(), ProjectMetaData.getInstance().getProjectName());
	// user label of serviceProvider
	// serviceProvider.getLabel();
    }

    @Override
    public void timeStep(double dt, double simulationTime, long iterationCount) {
	// write output for all decision points in single file
	// time, metric(A1), metric(A2), A3...., p1(beta), p2(beta), p3...
    }

}
