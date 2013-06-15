package org.movsim.simulator.roadnetwork.controller;

import org.movsim.autogen.ControllerGroup;

public class TrafficLightControllerExternal extends TrafficLightController {

    TrafficLightControllerExternal(ControllerGroup controllerGroup) {
        super(controllerGroup);
    }

    @Override
    public void timeStep(double dt, double simulationTime, long iterationCount) {
        // do nothing
        super.timeStep(dt, simulationTime, iterationCount);
    }

}
