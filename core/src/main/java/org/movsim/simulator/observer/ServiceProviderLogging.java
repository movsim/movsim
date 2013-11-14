package org.movsim.simulator.observer;

import org.movsim.input.ProjectMetaData;
import org.movsim.output.fileoutput.FileOutputBase;
import org.movsim.simulator.SimulationTimeStep;

import com.google.common.base.Preconditions;

/**
 * Writes output for all decision points in single file.
 * 
 */
class ServiceProviderLogging extends FileOutputBase implements SimulationTimeStep {

    private static final String extensionFormat = ".serviceprov_%s.csv";

    // not yet expandable
    private static final String outputHeadingTime = String.format("%s%9s,", COMMENT_CHAR, "t[s]");
    private static final String outputHeading = "%10s, %10s, %10s ";
    private static final String outputInformation = "%s %10s %.2f %n";

    private static final String outputFormatTime = "%10.2f,";
    private static final String outputFormat = "%10.2f, %10.4f, %10.4f,";

    private final ServiceProvider serviceProvider;

    public ServiceProviderLogging(ServiceProvider serviceProvider) {
        super(ProjectMetaData.getInstance().getOutputPath(), ProjectMetaData.getInstance().getProjectName());
        this.serviceProvider = Preconditions.checkNotNull(serviceProvider);
        writer = createWriter(String.format(extensionFormat, serviceProvider.getLabel()));
        writeHeader();
    }

    private void writeHeader() {
        writer.printf(outputInformation, COMMENT_CHAR, "DecisionPointsUncertainty", serviceProvider.getDecisionPoints()
                .getUncertainty());
        writer.printf(outputHeadingTime);

        for (DecisionPoint decisionPoint : serviceProvider.getDecisionPoints()) {
            for (RouteAlternative alternative : decisionPoint) {
                writer.printf(outputHeading,
                        "disutility_RoadId" + decisionPoint.getRoadId() + "_Route" + alternative.getRouteLabel(),
                        "probRoadId" + decisionPoint.getRoadId() + "_Route" + alternative.getRouteLabel(),
                        "traveltimeError_RoadId" + decisionPoint.getRoadId() + "_Route" + alternative.getRouteLabel());
            }
        }
        writer.printf("%n");
        writer.flush();
    }

    @Override
    public void timeStep(double dt, double simulationTime, long iterationCount) {
        writer.printf(outputFormatTime, simulationTime);
        for (DecisionPoint decisionPoint : serviceProvider.getDecisionPoints()) {
            for (RouteAlternative alternative : decisionPoint) {
                writer.printf(outputFormat, alternative.getDisutility(), alternative.getProbability(),
                        alternative.getTravelTimeError());
            }
        }
        writer.printf("%n");
        writer.flush();
    }

}
