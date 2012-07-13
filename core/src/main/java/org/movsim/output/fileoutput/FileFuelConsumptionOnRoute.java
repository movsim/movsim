package org.movsim.output.fileoutput;

import org.movsim.input.model.output.FuelConsumptionOnRouteInput;
import org.movsim.simulator.roadnetwork.Route;

/**
 * calculates and writes fuel collective fuel consumption on a route over all vehicles.
 */
public class FileFuelConsumptionOnRoute extends FileOutputBase {

    public FileFuelConsumptionOnRoute(FuelConsumptionOnRouteInput fuel, Route route) {
        // TODO
        System.out.println("FileFuelConsumptionOnRoute not yet implemented. label="+route.getName());
    }

    public void timeStep(double dt, double simulationTime, long iterationCount) {
        // TODO
    }

}
