package org.movsim.output.route;

import org.movsim.autogen.ConsumptionCalculation;
import org.movsim.input.ProjectMetaData;
import org.movsim.io.FileOutputBase;
import org.movsim.simulator.MovsimConstants;
import org.movsim.simulator.roadnetwork.routing.Route;

import com.google.common.base.Preconditions;

/**
 * Calculates and writes fuel collective fuel consumption on a route over all vehicles.
 */
public class FileConsumptionOnRoute extends FileOutputBase {

    private static final String EXTENSION_FORMAT = ".consumption.route_%s.csv";
    private static final String OUTPUT_HEADING = String.format("%s%9s, %10s, %10s, %10s, %10s %n", COMMENT_CHAR, "t[s]",
            "instConsumptionRate[l/s]", "instConsumptionEMA[l/s]", "cumulatedConsumption[l]", "numberVehicles");
    private static final String OUTPUT_FORMAT = "%10.2f, %10.6f, %10.6f, %10.4f, %8d %n";

    private double lastUpdateTime;

    private final ConsumptionCalculation consumptionConfig;

    public FileConsumptionOnRoute(ConsumptionCalculation fuelRouteInput, Route route) {
        super(ProjectMetaData.getInstance().getOutputPath(), ProjectMetaData.getInstance().getProjectName());
        this.consumptionConfig = Preconditions.checkNotNull(fuelRouteInput);
        lastUpdateTime = 0;
        writer = createWriter(String.format(EXTENSION_FORMAT, route.getName()));
        writer.printf(OUTPUT_HEADING);
        writer.flush();
    }

    public void write(double simulationTime, ConsumptionOnRoute consumption) {
        if (isLargerThanStartTimeInterval(simulationTime) && isSmallerThanEndTimeInterval(simulationTime)) {
            if (simulationTime - lastUpdateTime + MovsimConstants.SMALL_VALUE >= consumptionConfig.getDt()
                    || simulationTime == 0) {
                lastUpdateTime = simulationTime;
                write(OUTPUT_FORMAT, simulationTime, consumption.getInstantaneousConsumptionRate(),
                        consumption.getInstantaneousConsumptionEMA(), consumption.getTotalConsumption(),
                        consumption.getNumberOfVehicles());
            }
        }
    }

    private boolean isLargerThanStartTimeInterval(double time) {
        if (!consumptionConfig.isSetStartTime()) {
            return true;
        }
        return time >= consumptionConfig.getStartTime();
    }

    private boolean isSmallerThanEndTimeInterval(double time) {
        if (!consumptionConfig.isSetEndTime()) {
            return true;
        }
        return time <= consumptionConfig.getEndTime();
    }

}
