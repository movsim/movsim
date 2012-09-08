package org.movsim.output.consumption;

import org.movsim.input.model.output.FuelConsumptionOnRouteInput;
import org.movsim.simulator.roadnetwork.Route;

public class ConsumptionOutput {
    
    private final Route route;
    
    private final FileFuelConsumptionOnRoute fileWriter;
    
    public ConsumptionOutput(FuelConsumptionOnRouteInput fuelRouteInput, Route route, boolean writeOutput){
      this.route = route;
      fileWriter = (writeOutput) ? new FileFuelConsumptionOnRoute() : null;
    }
}
