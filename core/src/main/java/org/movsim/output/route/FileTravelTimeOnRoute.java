package org.movsim.output.route;

import org.movsim.input.ProjectMetaData;
import org.movsim.output.fileoutput.FileOutputBase;
import org.movsim.simulator.MovsimConstants;
import org.movsim.simulator.roadnetwork.routing.Route;
import org.movsim.utilities.Units;

public class FileTravelTimeOnRoute extends FileOutputBase {

    private static final String extensionFormat = ".tt.route_%s.csv";

    private static final String outputHeading = String.format("%s%9s, %10s, %10s, %10s, %10s, %10s %n", COMMENT_CHAR,
            "t[s]", "instTraveltime[s]", "instTravelTimeEMA[s]", "meanSpeed[km/h]", "cumulatedTravelTime[h]",
            "numberVehicles");
    private static final String outputFormat = "%10.2f, %10.2f, %10.2f, %10.2f, %10.4f, %8d %n";

    private final double dtOutput;

    private double lastUpdateTime;

    public FileTravelTimeOnRoute(double dtOut, Route route) {
        super(ProjectMetaData.getInstance().getOutputPath(), ProjectMetaData.getInstance().getProjectName());
        this.dtOutput = dtOut;
        lastUpdateTime = 0;
        writer = createWriter(String.format(extensionFormat, route.getName()));
        writer.printf(outputHeading);
        writer.flush();
    }

    public void write(double simulationTime, TravelTimeOnRoute travelTime) {
        if (simulationTime - lastUpdateTime + MovsimConstants.SMALL_VALUE >= dtOutput || simulationTime == 0) {
            lastUpdateTime = simulationTime;
            write(outputFormat, simulationTime, travelTime.getInstantaneousTravelTime(),
                    travelTime.getInstantaneousTravelTimeEMA(), travelTime.getMeanSpeed() * Units.MS_TO_KMH,
                    travelTime.getTotalTravelTime() * Units.S_TO_H, travelTime.getNumberOfVehicles());
        }
    }

}
