package org.movsim.output.route;

import org.movsim.input.ProjectMetaData;
import org.movsim.io.FileOutputBase;
import org.movsim.output.route.TravelTimeOnRoute.TravelTime;
import org.movsim.simulator.MovsimConstants;
import org.movsim.simulator.roadnetwork.routing.Route;
import org.movsim.utilities.Units;

public class FileTravelTimeOnRoute extends FileOutputBase {

    private static final String EXTENSION_FORMAT = ".tt_%s.route_%s.csv";

    private static final String OUTPUT_HEADING = String
            .format("%s%9s, %10s, %10s, %10s, %10s, %10s %n", COMMENT_CHAR, "t[s]", "instTraveltime[s]",
                    "instTravelTimeEMA[s]", "meanSpeed[km/h]", "cumulatedTravelTime[h]", "numberVehicles");
    private static final String OUTPUT_FORMAT = "%10.2f, %10.2f, %10.2f, %10.2f, %10.4f, %8d %n";

    private final double dtOutput;

    private double lastUpdateTime;

    public FileTravelTimeOnRoute(double dtOut, Route route, String extension) {
        super(ProjectMetaData.getInstance().getOutputPath(), ProjectMetaData.getInstance().getProjectName());
        this.dtOutput = dtOut;
        lastUpdateTime = 0;
        writer = createWriter(String.format(EXTENSION_FORMAT, extension, route.getName()));
        writer.printf(OUTPUT_HEADING);
        writer.flush();
    }

    public void write(double simulationTime, TravelTime tt) {
        if (simulationTime - lastUpdateTime + MovsimConstants.SMALL_VALUE >= dtOutput || simulationTime == 0) {
            lastUpdateTime = simulationTime;
            write(OUTPUT_FORMAT, simulationTime, tt.getInstantaneousTravelTime(), tt.getInstantaneousTravelTimeEMA(),
                    tt.getMeanSpeed() * Units.MS_TO_KMH, tt.getTotalTravelTime() * Units.S_TO_H,
                    tt.getNumberOfVehicles());
        }
    }

}
