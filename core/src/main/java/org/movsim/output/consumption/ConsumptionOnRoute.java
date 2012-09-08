package org.movsim.output.consumption;

import org.movsim.input.model.output.FuelConsumptionOnRouteInput;
import org.movsim.simulator.roadnetwork.Route;

public class ConsumptionOnRoute {
    
    private final Route route;
    
    private final FileConsumptionOnRoute fileWriter;
    
    public ConsumptionOnRoute(FuelConsumptionOnRouteInput fuelRouteInput, Route route, boolean writeOutput){
      this.route = route;
      fileWriter = (writeOutput) ? new FileConsumptionOnRoute() : null;
    }
}
