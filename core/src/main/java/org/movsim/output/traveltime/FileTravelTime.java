package org.movsim.output.traveltime;

import org.movsim.output.fileoutput.FileOutputBase;
import org.movsim.simulator.MovsimConstants;
import org.movsim.simulator.roadnetwork.Route;

public class FileTravelTime extends FileOutputBase {
    private static final String extensionFormat = ".tt.route_%s.csv";

    private static final String outputHeading = String.format("%s%9s, %10s, %10s, %10s%n", COMMENT_CHAR, "t[s]",
            "instTraveltime[s]", "instTravelTime EMA[s]", "meanSpeed[m/s]");
    private static final String outputFormat = "%10.2f, %10.2f, %10.2f, %10.2f %n";

    private final double dtOutput;

    private double lastUpdateTime;

    public FileTravelTime(double dtOut, Route route) {
        this.dtOutput = dtOut;
        lastUpdateTime = 0;
        writer = createWriter(String.format(extensionFormat, route.getName()));
        writer.printf(outputHeading);
        writer.flush();
    }

    public void write(double simulationTime, TravelTimeOnRoute travelTime) {
        if (simulationTime - lastUpdateTime + MovsimConstants.SMALL_VALUE >= dtOutput) {
            lastUpdateTime = simulationTime;
            writer.printf(outputFormat, simulationTime, travelTime.getInstantaneousTravelTime(),
                    travelTime.getInstantaneousTravelTimeEMA(), travelTime.getMeanSpeed());
            writer.flush();
        }
    }

}
