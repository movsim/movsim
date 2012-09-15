package org.movsim.output.route;

import org.movsim.output.fileoutput.FileOutputBase;
import org.movsim.simulator.MovsimConstants;
import org.movsim.simulator.roadnetwork.Route;

/**
 * calculates and writes fuel collective fuel consumption on a route over all vehicles.
 */
public class FileConsumptionOnRoute extends FileOutputBase {

    private static final String extensionFormat = ".consumption.route_%s.csv";

    private static final String outputHeading = String.format("%s%9s, %10s, %10s, %10s, %10s %n", COMMENT_CHAR, "t[s]",
            "instConsumptionRate[l/s]", "instConsumptionEMA[l/s]", "cumulatedConsumption[l]", "numberVehicles");
    private static final String outputFormat = "%10.2f, %10.6f, %10.6f, %10.4f, %8d %n";

    private final double dtOutput;

    private double lastUpdateTime;

    public FileConsumptionOnRoute(double dtOut, Route route) {
        this.dtOutput = dtOut;
        lastUpdateTime = 0;
        writer = createWriter(String.format(extensionFormat, route.getName()));
        writer.printf(outputHeading);
        writer.flush();
    }

    public void write(double simulationTime, ConsumptionOnRoute consumption) {
        if (simulationTime - lastUpdateTime + MovsimConstants.SMALL_VALUE >= dtOutput || simulationTime==0) {
            lastUpdateTime = simulationTime;
            writer.printf(outputFormat, simulationTime, consumption.getInstantaneousConsumptionRate(),
                    consumption.getInstantaneousConsumptionEMA(), consumption.getTotalConsumption(),
                    consumption.getNumberOfVehicles());
            writer.flush();
        }
    }
}
