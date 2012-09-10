package org.movsim.output.traveltime;

import org.movsim.output.fileoutput.FileOutputBase;
import org.movsim.simulator.roadnetwork.Route;

public class FileTravelTime extends  FileOutputBase {
    private static final String extensionFormat = ".tt.route_%s.csv";

    private static final String outputHeading = String.format("%s%10s,%10s%n", COMMENT_CHAR, "t[s]", "instTraveltime[s]");
    private static final String outputFormat = "%10.2f, %10.2f, %10.2f%n";
    
    public FileTravelTime(Route route){
        writer = createWriter(String.format(extensionFormat, route.getName()));
        writer.printf(outputHeading);
        writer.flush();
    }

    public void write(double simulationTime, double instantaneousTravelTime, double meanSpeed) {
        writer.printf(outputFormat, simulationTime, instantaneousTravelTime, meanSpeed);
        writer.flush();
    }

}
