package org.movsim.output;

import org.movsim.input.ProjectMetaData;
import org.movsim.output.fileoutput.FileOutputBase;
import org.movsim.simulator.vehicles.Vehicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileTrafficSinkData extends FileOutputBase implements
        org.movsim.simulator.roadnetwork.boundaries.TrafficSink.RecordDataCallback {

    private static final Logger LOG = LoggerFactory.getLogger(FileTrafficSourceData.class);

    private static final String extensionFormat = ".sink.road_%s.csv";
    private static final String outputHeading = COMMENT_CHAR
            + "     t[s], timeFormatted, totalVehiclesRemoved, lane, route, vehicleId, vehicleLabel, vehicleUserData ...\n";
    private static final String outputFormat = "%10.2f, %s, %6d, %2d, %s, %s, %s, %s %n";

    public FileTrafficSinkData(String roadId) {
        super(ProjectMetaData.getInstance().getOutputPath(), ProjectMetaData.getInstance().getProjectName());
        writer = createWriter(String.format(extensionFormat, roadId));
        writer.printf(outputHeading);
    }

    @Override
    public void recordData(double simulationTime, int totalVehiclesRemoved, Vehicle vehicle) {
        String formattedTime = ProjectMetaData.getInstance().getFormatedTimeWithOffset(simulationTime);
        writer.printf(outputFormat, simulationTime, formattedTime, totalVehiclesRemoved, vehicle.lane(),
                vehicle.getRouteName(), vehicle.getId(), vehicle.getLabel(),
                vehicle.getUserData().getString(SEPARATOR_CHAR));
        writer.flush();
    }
}
