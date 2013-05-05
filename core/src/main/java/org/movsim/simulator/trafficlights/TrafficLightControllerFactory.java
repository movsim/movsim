package org.movsim.simulator.trafficlights;

import org.movsim.autogen.ControllerGroup;

public final class TrafficLightControllerFactory {

    static TrafficLightController create(ControllerGroup controllerGroup) {
        if (controllerGroup.isSetExternal()) {
            return new TrafficLightControllerExternal(controllerGroup);
        }
        return new TrafficLightControllerInternal(controllerGroup);
    }

}
