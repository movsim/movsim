package org.movsim.simulator.roadnetwork.controller;

import org.movsim.autogen.ControllerGroup;

public final class TrafficLightControllerFactory {

    static TrafficLightController create(ControllerGroup controllerGroup) {
        return new TrafficLightControllerInternal(controllerGroup);
    }

}
