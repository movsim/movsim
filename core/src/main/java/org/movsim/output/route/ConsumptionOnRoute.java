package org.movsim.output.route;

import org.movsim.input.model.output.ConsumptionOnRouteInput;
import org.movsim.simulator.roadnetwork.RoadNetwork;
import org.movsim.simulator.roadnetwork.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsumptionOnRoute extends OutputOnRouteBase {
    
    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(ConsumptionOnRoute.class);
 
    private static final double TAU_EMA = 30;
    
    private final double beta;

    private final FileConsumptionOnRoute fileWriter;

    private double instantaneousConsumption;

    private double instConsumptionEMA;
    
    public ConsumptionOnRoute(double simulationTimestep, ConsumptionOnRouteInput input, RoadNetwork roadNetwork,
            Route route, boolean writeOutput) {
        super(roadNetwork, route);
        this.beta = Math.exp(-simulationTimestep / TAU_EMA);
        fileWriter = (writeOutput) ? new FileConsumptionOnRoute(input.getDt(), route) : null;
    }
    

    @Override
    public void timeStep(double dt, double simulationTime, long iterationCount) {

        instantaneousConsumption = roadNetwork.instantaneousFuelUsedLiters(route);

        instConsumptionEMA = calcEMA(beta, instantaneousConsumption, instConsumptionEMA);

        if (fileWriter != null) {
            fileWriter.write(simulationTime, this);
        }
        
    }

    public double getInstantaneousConsumption() {
        return instantaneousConsumption;
    }

    public double getInstantaneousConsumptionEMA() {
        return instConsumptionEMA;
    }



}
