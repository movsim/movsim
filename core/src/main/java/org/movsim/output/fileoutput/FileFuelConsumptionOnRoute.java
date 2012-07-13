package org.movsim.output.fileoutput;

import org.movsim.input.model.output.FuelConsumptionOnRouteInput;
import org.movsim.simulator.roadnetwork.Route;

public class FileFuelConsumptionOnRoute extends FileOutputBase {

    public FileFuelConsumptionOnRoute(FuelConsumptionOnRouteInput fuel, Route route) {
        // TODO Auto-generated constructor stub
        System.out.println("FileFuelConsumptionOnRoute not yet implemented. label="+route.getName());
        //System.exit(-1);
    }

    public void timeStep(double dt, double simulationTime, long iterationCount) {
        // TODO Auto-generated method stub
        
    }

}
